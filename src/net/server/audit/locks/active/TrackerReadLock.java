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
package net.server.audit.locks.active;

import constants.ServerConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;

import net.server.audit.ThreadTracker;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.empty.EmptyReadLock;

import tools.FilePrinter;

/**
 *
 * @author RonanLana
 */
public class TrackerReadLock extends ReentrantReadWriteLock.ReadLock implements MonitoredReadLock {
    private ScheduledFuture<?> timeoutSchedule = null;
    private StackTraceElement[] deadlockedState = null;
    private final MonitoredLockType id;
    private final int hashcode;
    private final Lock state = new ReentrantLock(true);
    private final AtomicInteger reentrantCount = new AtomicInteger(0);
    
    public TrackerReadLock(MonitoredReentrantReadWriteLock lock) {
        super(lock);
        this.id = lock.id;
        hashcode = this.hashCode();
    }
    
    @Override
    public void lock() {
        if(ServerConstants.USE_THREAD_TRACKER) {
            if(deadlockedState != null) {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone(ServerConstants.TIMEZONE));

                //FilePrinter.printError(FilePrinter.DEADLOCK_ERROR, "[CRITICAL] " + dateFormat.format(new Date()) + " Deadlock occurred when trying to use the '" + id.name() + "' lock resources:\r\n" + printStackTrace(deadlockedState) + "\r\n\r\n");
                ThreadTracker.getInstance().accessThreadTracker(true, true, id, hashcode);
                deadlockedState = null;
            }

            registerLocking();
        }
        
        super.lock();
    }
    
    @Override
    public void unlock() {
        if(ServerConstants.USE_THREAD_TRACKER) {
            unregisterLocking();
        }
        
        super.unlock();
    }
    
    @Override
    public boolean tryLock() {
        if(super.tryLock()) {
            if(ServerConstants.USE_THREAD_TRACKER) {
                if(deadlockedState != null) {
                    //FilePrinter.printError(FilePrinter.DEADLOCK_ERROR, "Deadlock occurred when trying to use the '" + id.name() + "' lock resources:\r\n" + printStackTrace(deadlockedState) + "\r\n\r\n");
                    ThreadTracker.getInstance().accessThreadTracker(true, true, id, hashcode);
                    deadlockedState = null;
                }

                registerLocking();
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void registerLocking() {
        state.lock();
        try {
            ThreadTracker.getInstance().accessThreadTracker(false, true, id, hashcode);
        
            if(reentrantCount.incrementAndGet() == 1) {
                final Thread t = Thread.currentThread();
                timeoutSchedule = TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        issueDeadlock(t);
                    }
                }, ServerConstants.LOCK_MONITOR_TIME);
            }
        } finally {
            state.unlock();
        }
    }
    
    private void unregisterLocking() {
        state.lock();
        try {
            if(reentrantCount.decrementAndGet() == 0) {
                if(timeoutSchedule != null) {
                    timeoutSchedule.cancel(false);
                    timeoutSchedule = null;
                }
            }
            
            ThreadTracker.getInstance().accessThreadTracker(false, false, id, hashcode);
        } finally {
            state.unlock();
        }
    }
    
    private void issueDeadlock(Thread t) {
        deadlockedState = t.getStackTrace();
        //super.unlock();
    }
    
    private static String printStackTrace(StackTraceElement[] list) {
        String s = "";
        for(int i = 0; i < list.length; i++) {
            s += ("    " + list[i].toString() + "\r\n");
        }
        
        return s;
    }
    
    @Override
    public MonitoredReadLock dispose() {
        state.lock();
        try {
            if(timeoutSchedule != null) {
                timeoutSchedule.cancel(false);
                timeoutSchedule = null;
            }
            
            reentrantCount.set(Integer.MAX_VALUE);
        } finally {
            state.unlock();
        }
        
        //unlock();
        return new EmptyReadLock(id);
    }
}
