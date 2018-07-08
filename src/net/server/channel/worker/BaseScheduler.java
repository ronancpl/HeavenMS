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
import tools.locks.MonitoredLockType;
import tools.locks.MonitoredReentrantLock;

/**
 *
 * @author Ronan
 */
public abstract class BaseScheduler {
    private int idleProcs = 0;
    private List<SchedulerListener> listeners = new LinkedList<>();
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
    }
    
    protected void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }
    
    private void runBaseSchedule() {
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
            
            long timeNow = System.currentTimeMillis();
            List<Object> toRemove = new LinkedList<>();
            for(Entry<Object, Pair<Runnable, Long>> rmd : registeredEntries.entrySet()) {
                Pair<Runnable, Long> r = rmd.getValue();
                
                if(r.getRight() < timeNow) {
                    r.getLeft().run();  // runs the cancel action
                    toRemove.add(rmd.getKey());
                }
            }
            
            for(Object mse : toRemove) {
                registeredEntries.remove(mse);
            }
            
            dispatchRemovedEntries(toRemove, true);
        } finally {
            schedulerLock.unlock();
        }
    }
    
    private void dispatchRemovedEntries(List<Object> toRemove, boolean fromUpdate) {
        for (SchedulerListener listener : listeners.toArray(new SchedulerListener[listeners.size()])) {
            listener.removedScheduledEntries(toRemove, fromUpdate);
        }
    }
    
    protected void registerEntry(Object key, Runnable removalAction, long duration) {
        schedulerLock.lock();
        try {
            idleProcs = 0;
            if(schedulerTask == null) {
                schedulerTask = TimerManager.getInstance().register(monitorTask, ServerConstants.MOB_STATUS_MONITOR_PROC, ServerConstants.MOB_STATUS_MONITOR_PROC);
            }
            
            registeredEntries.put(key, new Pair<>(removalAction, System.currentTimeMillis() + duration));
        } finally {
            schedulerLock.unlock();
        }
    }
    
    protected void interruptEntry(Object key) {
        schedulerLock.lock();
        try {
            Pair<Runnable, Long> rm = registeredEntries.remove(key);
            if(rm != null) rm.getLeft().run();
            
            dispatchRemovedEntries(Collections.singletonList(key), false);
        } finally {
            schedulerLock.unlock();
        }
    }
}
