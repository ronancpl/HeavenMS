/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft 2016 - 2018 RonanLana

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
import java.util.concurrent.locks.Lock;
import server.TimerManager;
import tools.Pair;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;

/**
 *
 * @author Ronan
 */
public abstract class BaseScheduler {
    private int idleProcs = 0;
    private List<SchedulerListener> listeners = new LinkedList<>();
    private final List<Lock> externalLocks;
    private Map<Object, Pair<Runnable, Long>> registeredEntries = new HashMap<>();
    
    private ScheduledFuture<?> schedulerTask = null;
    private Lock schedulerLock;
    private Runnable monitorTask = new Runnable() {
                                        @Override
                                        public void run() {
                                            runBaseSchedule();
                                        }
                                    };
    
    protected BaseScheduler(MonitoredLockType lockType) {
        schedulerLock = new MonitoredReentrantLock(lockType, true);
        externalLocks = new LinkedList<>();
    }
    
    // NOTE: practice EXTREME caution when adding external locks to the scheduler system, if you don't know what you're doing DON'T USE THIS.
    protected BaseScheduler(MonitoredLockType lockType, List<Lock> extLocks) {
        schedulerLock = new MonitoredReentrantLock(lockType, true);
        externalLocks = extLocks;
    }
    
    protected void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }
    
    private void lockScheduler() {
        if(!externalLocks.isEmpty()) {
            for(Lock l : externalLocks) {
                l.lock();
            }
        }
        
        schedulerLock.lock();
    }
    
    private void unlockScheduler() {
        if(!externalLocks.isEmpty()) {
            for(Lock l : externalLocks) {
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
        
        long timeNow = System.currentTimeMillis();
        toRemove = new LinkedList<>();
        for(Entry<Object, Pair<Runnable, Long>> rmd : registeredEntriesCopy.entrySet()) {
            Pair<Runnable, Long> r = rmd.getValue();

            if(r.getRight() < timeNow) {
                r.getLeft().run();  // runs the cancel action
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
            
            registeredEntries.put(key, new Pair<>(removalAction, System.currentTimeMillis() + duration));
        } finally {
            unlockScheduler();
        }
    }
    
    protected void interruptEntry(Object key) {
        lockScheduler();
        try {
            Pair<Runnable, Long> rm = registeredEntries.remove(key);
            if(rm != null) rm.getLeft().run();
        } finally {
            unlockScheduler();
        }
        
        dispatchRemovedEntries(Collections.singletonList(key), false);
    }
    
    private void dispatchRemovedEntries(List<Object> toRemove, boolean fromUpdate) {
        for (SchedulerListener listener : listeners.toArray(new SchedulerListener[listeners.size()])) {
            listener.removedScheduledEntries(toRemove, fromUpdate);
        }
    }
}
