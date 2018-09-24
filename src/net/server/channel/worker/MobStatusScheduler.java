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

import client.status.MonsterStatusEffect;
import constants.ServerConstants;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

/**
 *
 * @author Ronan
 */
public class MobStatusScheduler extends BaseScheduler {
    private Map<MonsterStatusEffect, MobStatusOvertimeEntry> registeredMobStatusOvertime = new HashMap<>();
    private MonitoredReentrantLock overtimeStatusLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHANNEL_OVTSTATUS, true);
    
    private class MobStatusOvertimeEntry {
        private int procCount;
        private int procLimit;
        private Runnable r;
        
        protected MobStatusOvertimeEntry(int delay, Runnable run) {
            procCount = 0;
            procLimit = (int)Math.ceil((float) delay / ServerConstants.MOB_STATUS_MONITOR_PROC);
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
