/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server.audit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import constants.ServerConstants;
import net.server.audit.locks.MonitoredLockType;
import server.TimerManager;
import tools.FilePrinter;

/**
 *
 * @author RonanLana
 * 
 * This tool has the main purpose of auditing deadlocks throughout the server and must be used only for debugging. The flag is USE_THREAD_TRACKER.
 */
public class ThreadTracker {
    private static ThreadTracker instance = null;
    
    public static ThreadTracker getInstance() {
        if (instance == null) {
            instance = new ThreadTracker();
        }
        return instance;
    }
    
    private final Lock ttLock = new ReentrantLock(true);
    
    private final Map<Long, List<MonitoredLockType>> threadTracker = new HashMap<>();
    private final Map<Long, Integer> threadUpdate = new HashMap<>();
    private final Map<Long, Thread> threads = new HashMap<>();
    
    private final Map<Long, AtomicInteger> lockCount = new HashMap<>();
    private final Map<Long, MonitoredLockType> lockIds = new HashMap<>();
    private final Map<Long, Long> lockThreads = new HashMap<>();
    private final Map<Long, Integer> lockUpdate = new HashMap<>();
    
    private final Map<MonitoredLockType, Map<Long, Integer>> locks = new HashMap<>();
    ScheduledFuture<?> threadTrackerSchedule;
    
    private String printThreadTrackerState(String dateFormat) {
        
        Map<MonitoredLockType, List<Integer>> lockValues = new HashMap<>();
        Set<Long> executingThreads = new HashSet<>();
        
        for(Map.Entry<Long, AtomicInteger> lc : lockCount.entrySet()) {
            if(lc.getValue().get() != 0) {
                executingThreads.add(lockThreads.get(lc.getKey()));
                
                MonitoredLockType lockId = lockIds.get(lc.getKey());
                List<Integer> list = lockValues.get(lockId);
                
                if(list == null) {
                    list = new ArrayList<>();
                    lockValues.put(lockId, list);
                }
                
                list.add(lc.getValue().get());
            }
        }
        
        
        String s = "----------------------------\r\n" + dateFormat + "\r\n    ";
        s += "Lock-thread usage count:";
        for(Map.Entry<MonitoredLockType, List<Integer>> lock : lockValues.entrySet()) {
            s += ("\r\n  " + lock.getKey().name() + ": ");
            
            for(Integer i : lock.getValue()) {
                s += (i + " ");
            }
        }
        s += "\r\n\r\nThread opened lock path:";
        
        for(Long tid : executingThreads) {
            s += "\r\n";
            for(MonitoredLockType lockid : threadTracker.get(tid)) {
                s += (lockid.name() + " ");
            }
            s += "|";
        }
        
        s += "\r\n\r\n";
        
        return s;
    }
    
    private static String printThreadLog(List<MonitoredLockType> stillLockedPath, String dateFormat) {
        String s = "----------------------------\r\n" + dateFormat + "\r\n    ";
        for(MonitoredLockType lock : stillLockedPath) {
            s += (lock.name() + " ");
        }
        s += "\r\n\r\n";
        
        return s;
    }
    
    private static String printThreadStack(StackTraceElement[] list, String dateFormat) {
        String s = "----------------------------\r\n" + dateFormat + "\r\n";
        for(int i = 0; i < list.length; i++) {
            s += ("    " + list[i].toString() + "\r\n");
        }
        
        return s;
    }
    
    public void accessThreadTracker(boolean update, boolean lock, MonitoredLockType lockId, long lockOid) {
        ttLock.lock();
        try {
            if(update) {
                if(!lock) { // update tracker
                    List<Long> toRemove = new ArrayList<>();

                    for(Long l : threadUpdate.keySet()) {
                        int next = threadUpdate.get(l) + 1;
                        if(next == 4) {
                            List<MonitoredLockType> tt = threadTracker.get(l);

                            if(tt.isEmpty()) {
                                toRemove.add(l);
                            } else {
                                StackTraceElement[] ste = threads.get(l).getStackTrace();
                                if(ste.length > 0) {
                                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone(ServerConstants.TIMEZONE));
                                    String df = dateFormat.format(new Date());
                                    
                                    FilePrinter.print(FilePrinter.DEADLOCK_LOCKS, printThreadLog(tt, df));
                                    FilePrinter.print(FilePrinter.DEADLOCK_STACK, printThreadStack(ste, df));
                                }
                            }
                        }

                        threadUpdate.put(l, next);
                    }

                    for(Long l : toRemove) {
                        threadTracker.remove(l);
                        threadUpdate.remove(l);
                        threads.remove(l);

                        for(Map<Long, Integer> threadLock : locks.values()) {
                            threadLock.remove(l);
                        }
                    }

                    toRemove.clear();

                    for(Entry<Long, Integer> it : lockUpdate.entrySet()) {
                        int val = it.getValue() + 1;

                        if(val < 60) {
                            lockUpdate.put(it.getKey(), val);
                        } else {
                            toRemove.add(it.getKey());  // free the structure after 60 silent updates
                        }
                    }

                    for(Long l : toRemove) {
                        lockCount.remove(l);
                        lockIds.remove(l);
                        lockThreads.remove(l);
                        lockUpdate.remove(l);
                    }
                } else {    // print status
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone(ServerConstants.TIMEZONE));

                    FilePrinter.printError(FilePrinter.DEADLOCK_STATE, printThreadTrackerState(dateFormat.format(new Date())));
                    //FilePrinter.printError(FilePrinter.DEADLOCK_STATE, "[" + dateFormat.format(new Date()) + "] Presenting current lock path for lockid " + lockId.name() + ".\r\n" + printLockStatus(lockId) + "\r\n-------------------------------\r\n");
                }
            } else {
                long tid = Thread.currentThread().getId();

                if(lock) {
                    AtomicInteger c = lockCount.get(lockOid);
                    if(c == null) {
                        c = new AtomicInteger(0);
                        lockCount.put(lockOid, c);
                        lockIds.put(lockOid, lockId);
                        lockThreads.put(lockOid, tid);
                        lockUpdate.put(lockOid, 0);
                    }
                    c.incrementAndGet();

                    List<MonitoredLockType> list = threadTracker.get(tid);
                    if(list == null) {
                        list = new ArrayList<>(5);
                        threadTracker.put(tid, list);
                        threadUpdate.put(tid, 0);
                        threads.put(tid, Thread.currentThread());
                    } else if(list.isEmpty()) {
                        threadUpdate.put(tid, 0);
                    }
                    list.add(lockId);

                    Map<Long, Integer> threadLock = locks.get(lockId);
                    if(threadLock == null) {
                        threadLock = new HashMap<>(5);
                        locks.put(lockId, threadLock);
                    }

                    Integer lc = threadLock.get(tid);
                    if(lc != null) {
                        threadLock.put(tid, lc + 1);
                    } else {
                        threadLock.put(tid, 1);
                    }
                }
                else {
                    AtomicInteger c = lockCount.get(lockOid);
                    c.decrementAndGet();
                    lockUpdate.put(lockOid, 0);

                    List<MonitoredLockType> list = threadTracker.get(tid);
                    for(int i = list.size() - 1; i >= 0; i--) {
                        if(lockId.equals(list.get(i))) {
                            list.remove(i);
                            break;
                        }
                    }

                    Map<Long, Integer> threadLock = locks.get(lockId);
                    threadLock.put(tid, threadLock.get(tid) - 1);
                }
            }
        } finally {
            ttLock.unlock();
        }
    }
    
    private String printLockStatus(MonitoredLockType lockId) {
        String s = "";
        
        for(Long threadid : locks.get(lockId).keySet()) {
            for(MonitoredLockType lockid : threadTracker.get(threadid)) {
                s += ("  " + lockid.name());
            }
            
            s += " |\r\n";
        }
        
        return s;
    }
    
    public void registerThreadTrackerTask() {
        threadTrackerSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                accessThreadTracker(true, false, MonitoredLockType.UNDEFINED, -1);
            }
        }, 10000, 10000);
    }
    
    public void cancelThreadTrackerTask() {
        threadTrackerSchedule.cancel(false);
    }
}
