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

import client.status.MonsterStatusEffect;
import constants.ServerConstants;
import java.util.HashMap;
import java.util.ArrayList;
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
public class MobStatusScheduler {
    private int idleProcs = 0;
    private Map<MonsterStatusEffect, Pair<Runnable, Long>> registeredMobStatus = new HashMap<>();
    private Map<MonsterStatusEffect, MobStatusOvertimeEntry> registeredMobStatusOvertime = new HashMap<>();
    
    private class MobStatusOvertimeEntry {
        private int procCount;
        private int procLimit;
        private Runnable r;
        
        protected MobStatusOvertimeEntry(int delay, Runnable run) {
            procCount = 0;
            procLimit = (int)Math.ceil((float) delay / ServerConstants.MOB_STATUS_MONITOR_PROC);
            r = run;
        }
        
        protected void update() {
            procCount++;
            if(procCount >= procLimit) {
                procCount = 0;
                r.run();
            }
        }
    }
    
    private ScheduledFuture<?> mobStatusSchedule = null;
    private Lock mobStatusLock = new MonitoredReentrantLock(MonitoredLockType.CHANNEL_MOBSTATUS, true);
    private Runnable monitorTask = new Runnable() {
                                        @Override
                                        public void run() {
                                            runMobStatusSchedule();
                                        }
                                    };
    
    private void runMobStatusSchedule() {
        mobStatusLock.lock();
        try {
            if(registeredMobStatus.isEmpty()) {
                idleProcs++;
                
                if(idleProcs >= ServerConstants.MOB_STATUS_MONITOR_LIFE) {
                    if(mobStatusSchedule != null) {
                        mobStatusSchedule.cancel(false);
                        mobStatusSchedule = null;
                    }
                }
                
                return;
            }
            idleProcs = 0;
            
            long timeNow = System.currentTimeMillis();
            List<MonsterStatusEffect> toRemove = new LinkedList<>();
            for(Entry<MonsterStatusEffect, Pair<Runnable, Long>> rmd : registeredMobStatus.entrySet()) {
                Pair<Runnable, Long> r = rmd.getValue();
                
                if(r.getRight() < timeNow) {
                    r.getLeft().run();  // runs the cancel action
                    toRemove.add(rmd.getKey());
                }
            }
            
            for(MonsterStatusEffect mse : toRemove) {
                registeredMobStatus.remove(mse);
                registeredMobStatusOvertime.remove(mse);
            }
            
            // it's probably ok to use one thread for both management & overtime actions
            List<MobStatusOvertimeEntry> mdoeList = new ArrayList<>(registeredMobStatusOvertime.values());
            for(MobStatusOvertimeEntry mdoe : mdoeList) {
                mdoe.update();
            }
        } finally {
            mobStatusLock.unlock();
        }
    }
    
    public void registerMobStatus(MonsterStatusEffect mse, Runnable cancelStatus, long duration, Runnable overtimeStatus, int overtimeDelay) {
        mobStatusLock.lock();
        try {
            idleProcs = 0;
            if(mobStatusSchedule == null) {
                mobStatusSchedule = TimerManager.getInstance().register(monitorTask, ServerConstants.MOB_STATUS_MONITOR_PROC, ServerConstants.MOB_STATUS_MONITOR_PROC);
            }
            
            registeredMobStatus.put(mse, new Pair<>(cancelStatus, System.currentTimeMillis() + duration));
            
            if(overtimeStatus != null) {
                MobStatusOvertimeEntry mdoe = new MobStatusOvertimeEntry(overtimeDelay, overtimeStatus);
                registeredMobStatusOvertime.put(mse, mdoe);
            }
        } finally {
            mobStatusLock.unlock();
        }
    }
    
    public void interruptMobStatus(MonsterStatusEffect mse) {
        mobStatusLock.lock();
        try {
            Pair<Runnable, Long> rmd = registeredMobStatus.remove(mse);
            if(rmd != null) rmd.getLeft().run();
            
            registeredMobStatusOvertime.remove(mse);
        } finally {
            mobStatusLock.unlock();
        }
    }
}
