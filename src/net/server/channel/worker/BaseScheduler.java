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
package net.server.channel.worker;

import constants.ServerConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import net.server.Server;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.TimerManager;
import tools.Pair;

/**
 *
 * @author Ronan
 */
public abstract class BaseScheduler {
    private int idleProcs = 0;
    private List<SchedulerListener> listeners = new LinkedList<>();
    private final List<MonitoredReentrantLock> externalLocks = new LinkedList<>();
    private Map<Object, Pair<Runnable, Long>> registeredEntries = new HashMap<>();
    
    private ScheduledFuture<?> schedulerTask = null;
    private MonitoredReentrantLock schedulerLock;
    private Runnable monitorTask = new Runnable() {
                                        @Override
                                        public void run() {
                                            runBaseSchedule();
                                        }
                                    };
    
    protected BaseScheduler(MonitoredLockType lockType) {
        schedulerLock = MonitoredReentrantLockFactory.createLock(lockType, true);
    }
    
    // NOTE: practice EXTREME caution when adding external locks to the scheduler system, if you don't know what you're doing DON'T USE THIS.
    protected BaseScheduler(MonitoredLockType lockType, List<MonitoredReentrantLock> extLocks) {
        schedulerLock = MonitoredReentrantLockFactory.createLock(lockType, true);
        
        for(MonitoredReentrantLock lock : extLocks) {
            externalLocks.add(lock);
        }
    }
    
    protected void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }
    
    private void lockScheduler() {
        if(!externalLocks.isEmpty()) {
            for(MonitoredReentrantLock l : externalLocks) {
                l.lock();
            }
        }
        
        schedulerLock.lock();
    }
    
    private void unlockScheduler() {
        if(!externalLocks.isEmpty()) {
            for(MonitoredReentrantLock l : externalLocks) {
                l.unlock();
            }
        }
        
        schedulerLock.unlock();
    }
    
    private void runBaseSchedule() {
        List<Object> toRemove;
        Map<Object, Pair<Runnable, Long>> registeredEntriesCopy;
        
        lockScheduler();
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
            unlockScheduler();
        }
        
        long timeNow = Server.getInstance().getCurrentTime();
        toRemove = new LinkedList<>();
        for(Entry<Object, Pair<Runnable, Long>> rmd : registeredEntriesCopy.entrySet()) {
            Pair<Runnable, Long> r = rmd.getValue();

            if(r.getRight() < timeNow) {
                r.getLeft().run();  // runs the scheduled action
                toRemove.add(rmd.getKey());
            }
        }
        
        if(!toRemove.isEmpty()) {
            lockScheduler();
            try {
                for(Object o : toRemove) {
                    registeredEntries.remove(o);
                }
            } finally {
                unlockScheduler();
            }
        }
        
        dispatchRemovedEntries(toRemove, true);
    }
    
    protected void registerEntry(Object key, Runnable removalAction, long duration) {
        lockScheduler();
        try {
            idleProcs = 0;
            if(schedulerTask == null) {
                schedulerTask = TimerManager.getInstance().register(monitorTask, ServerConstants.MOB_STATUS_MONITOR_PROC, ServerConstants.MOB_STATUS_MONITOR_PROC);
            }
            
            registeredEntries.put(key, new Pair<>(removalAction, Server.getInstance().getCurrentTime() + duration));
        } finally {
            unlockScheduler();
        }
    }
    
    protected void interruptEntry(Object key) {
        Runnable toRun = null;
        
        lockScheduler();
        try {
            Pair<Runnable, Long> rm = registeredEntries.remove(key);
            if(rm != null) {
                toRun = rm.getLeft();
            }
        } finally {
            unlockScheduler();
        }
        
        if(toRun != null) {
            toRun.run();
        }
        
        dispatchRemovedEntries(Collections.singletonList(key), false);
    }
    
    private void dispatchRemovedEntries(List<Object> toRemove, boolean fromUpdate) {
        for (SchedulerListener listener : listeners.toArray(new SchedulerListener[listeners.size()])) {
            listener.removedScheduledEntries(toRemove, fromUpdate);
        }
    }
    
    public void dispose() {
        lockScheduler();
        try {
            if(schedulerTask != null) {
                schedulerTask.cancel(false);
                schedulerTask = null;
            }
            
            listeners.clear();
            registeredEntries.clear();
        } finally {
            unlockScheduler();
            externalLocks.clear();
        }
        
        disposeLocks();
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
