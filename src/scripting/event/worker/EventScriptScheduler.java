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
package scripting.event.worker;

import constants.ServerConstants;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;
import net.server.Server;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

/**
 *
 * @author Ronan
 */
public class EventScriptScheduler {
    private boolean disposed = false;
    private int idleProcs = 0;
    private Map<Runnable, Long> registeredEntries = new HashMap<>();
    
    private ScheduledFuture<?> schedulerTask = null;
    private MonitoredReentrantLock schedulerLock;
    private Runnable monitorTask = new Runnable() {
                                        @Override
                                        public void run() {
                                            runBaseSchedule();
                                        }
                                    };
    
    public EventScriptScheduler() {
        schedulerLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EM_SCHDL, true);
    }
    
    private void runBaseSchedule() {
        List<Runnable> toRemove;
        Map<Runnable, Long> registeredEntriesCopy;
        
        schedulerLock.lock();
        try {
            if(registeredEntries.isEmpty()) {
                idleProcs++;
                
                if(idleProcs >= ServerConstants.MOB_STATUS_MONITOR_LIFE) {
                    if(schedulerTask != null) {
                        schedulerTask.cancel(false);
                        schedulerTask = null;
                    }
                }
                
                return;
            }
            
            idleProcs = 0;
            registeredEntriesCopy = new HashMap<>(registeredEntries);
        } finally {
            schedulerLock.unlock();
        }
        
        long timeNow = Server.getInstance().getCurrentTime();
        toRemove = new LinkedList<>();
        for(Entry<Runnable, Long> rmd : registeredEntriesCopy.entrySet()) {
            if(rmd.getValue() < timeNow) {
                Runnable r = rmd.getKey();
                
                r.run();  // runs the scheduled action
                toRemove.add(r);
            }
        }
        
        if(!toRemove.isEmpty()) {
            schedulerLock.lock();
            try {
                for(Runnable r : toRemove) {
                    registeredEntries.remove(r);
                }
            } finally {
                schedulerLock.unlock();
            }
        }
    }
    
    public void registerEntry(final Runnable scheduledAction, final long duration) {
        
        Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            schedulerLock.lock();
                            try {
                                idleProcs = 0;
                                if(schedulerTask == null) {
                                    if(disposed) return;

                                    schedulerTask = TimerManager.getInstance().register(monitorTask, ServerConstants.MOB_STATUS_MONITOR_PROC, ServerConstants.MOB_STATUS_MONITOR_PROC);
                                }

                                registeredEntries.put(scheduledAction, Server.getInstance().getCurrentTime() + duration);
                            } finally {
                                schedulerLock.unlock();
                            }
                        }
                    });
        
        t.start();
    }
    
    public void cancelEntry(final Runnable scheduledAction) {
        
        Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            schedulerLock.lock();
                            try {
                                registeredEntries.remove(scheduledAction);
                            } finally {
                                schedulerLock.unlock();
                            }
                        }
                    });
        
        t.start();
    }
    
    public void dispose() {
        
        Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            schedulerLock.lock();
                            try {
                                if(schedulerTask != null) {
                                    schedulerTask.cancel(false);
                                    schedulerTask = null;
                                }

                                registeredEntries.clear();
                                disposed = true;
                            } finally {
                                schedulerLock.unlock();
                            }
                            
                            disposeLocks();
                        }
                    });
        
        t.start();
    }
    
    private void disposeLocks() {
        LockCollector.getInstance().registerDisposeAction(new Runnable() {
            @Override
            public void run() {
                emptyLocks();
            }
        });
    }
    
    private void emptyLocks() {
        schedulerLock = schedulerLock.dispose();
    }
}
