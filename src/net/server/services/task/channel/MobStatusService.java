/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package net.server.services.task.channel;

import net.server.services.BaseService;
import client.status.MonsterStatusEffect;
import config.YamlConfig;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.server.services.BaseScheduler;
import net.server.services.SchedulerListener;

/**
 *
 * @author Ronan
 */
public class MobStatusService extends BaseService {
    
    private MobStatusScheduler mobStatusSchedulers[] = new MobStatusScheduler[YamlConfig.config.server.CHANNEL_LOCKS];
    
    public MobStatusService() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            mobStatusSchedulers[i] = new MobStatusScheduler();
        }
    }
    
    @Override
    public void dispose() {
        for(int i = 0; i < YamlConfig.config.server.CHANNEL_LOCKS; i++) {
            if(mobStatusSchedulers[i] != null) {
                mobStatusSchedulers[i].dispose();
                mobStatusSchedulers[i] = null;
            }
        }
    }
    
    public void registerMobStatus(int mapid, MonsterStatusEffect mse, Runnable cancelAction, long duration) {
        registerMobStatus(mapid, mse, cancelAction, duration, null, -1);
    }
    
    public void registerMobStatus(int mapid, MonsterStatusEffect mse, Runnable cancelAction, long duration, Runnable overtimeAction, int overtimeDelay) {
        mobStatusSchedulers[getChannelSchedulerIndex(mapid)].registerMobStatus(mse, cancelAction, duration, overtimeAction, overtimeDelay);
    }
    
    public void interruptMobStatus(int mapid, MonsterStatusEffect mse) {
        mobStatusSchedulers[getChannelSchedulerIndex(mapid)].interruptMobStatus(mse);
    }
    
    private class MobStatusScheduler extends BaseScheduler {

        private Map<MonsterStatusEffect, MobStatusOvertimeEntry> registeredMobStatusOvertime = new HashMap<>();
        private MonitoredReentrantLock overtimeStatusLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHANNEL_OVTSTATUS, true);

        private class MobStatusOvertimeEntry {
            private int procCount;
            private int procLimit;
            private Runnable r;

            protected MobStatusOvertimeEntry(int delay, Runnable run) {
                procCount = 0;
                procLimit = (int)Math.ceil((float) delay / YamlConfig.config.server.MOB_STATUS_MONITOR_PROC);
                r = run;
            }

            protected void update(List<Runnable> toRun) {
                procCount++;
                if(procCount >= procLimit) {
                    procCount = 0;
                    toRun.add(r);
                }
            }
        }

        public MobStatusScheduler() {
            super(MonitoredLockType.CHANNEL_MOBSTATUS);

            super.addListener(new SchedulerListener() {
                @Override
                public void removedScheduledEntries(List<Object> toRemove, boolean update) {
                    List<Runnable> toRun = new ArrayList<>();

                    overtimeStatusLock.lock();
                    try {
                        for(Object mseo : toRemove) {
                            MonsterStatusEffect mse = (MonsterStatusEffect) mseo;
                            registeredMobStatusOvertime.remove(mse);
                        }

                        if(update) {
                            // it's probably ok to use one thread for both management & overtime actions
                            List<MobStatusOvertimeEntry> mdoeList = new ArrayList<>(registeredMobStatusOvertime.values());
                            for(MobStatusOvertimeEntry mdoe : mdoeList) {
                                mdoe.update(toRun);
                            }
                        }
                    } finally {
                        overtimeStatusLock.unlock();
                    }

                    for(Runnable r : toRun) {
                        r.run();
                    }
                }
            });
        }

        public void registerMobStatus(MonsterStatusEffect mse, Runnable cancelStatus, long duration, Runnable overtimeStatus, int overtimeDelay) {
            if(overtimeStatus != null) {
                MobStatusOvertimeEntry mdoe = new MobStatusOvertimeEntry(overtimeDelay, overtimeStatus);

                overtimeStatusLock.lock();
                try {
                    registeredMobStatusOvertime.put(mse, mdoe);
                } finally {
                    overtimeStatusLock.unlock();
                }
            }

            registerEntry(mse, cancelStatus, duration);
        }

        public void interruptMobStatus(MonsterStatusEffect mse) {
            interruptEntry(mse);
        }

        @Override
        public void dispose() {
            disposeLocks();
            super.dispose();
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
            overtimeStatusLock = overtimeStatusLock.dispose();
        }
        
    }
    
}
