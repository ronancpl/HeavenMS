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

import net.server.audit.locks.MonitoredLockType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

/**
 *
 * @author Ronan
 */
public class MobAnimationScheduler extends BaseScheduler {
    Set<Integer> onAnimationMobs = new HashSet<>(1000);
    private MonitoredReentrantLock animationLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHANNEL_MOBANIMAT, true);
    
    private static Runnable r = new Runnable() {
        @Override
        public void run() {}    // do nothing
    };
    
    public MobAnimationScheduler() {
        super(MonitoredLockType.CHANNEL_MOBACTION);
        
        super.addListener(new SchedulerListener() {
            @Override
            public void removedScheduledEntries(List<Object> toRemove, boolean update) {
                animationLock.lock();
                try {
                    for(Object hashObj : toRemove) {
                        Integer mobHash = (Integer) hashObj;
                        onAnimationMobs.remove(mobHash);
                    }
                } finally {
                    animationLock.unlock();
                }
            }
        });
    }
    
    public boolean registerAnimationMode(Integer mobHash, long animationTime) {
        animationLock.lock();
        try {
            if(onAnimationMobs.contains(mobHash)) {
                return false;
            }
            
            registerEntry(mobHash, r, animationTime);
            onAnimationMobs.add(mobHash);
            return true;
        } finally {
            animationLock.unlock();
        }
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
        animationLock = animationLock.dispose();
    }
}
