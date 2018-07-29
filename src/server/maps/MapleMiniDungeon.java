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
package server.maps;

import server.TimerManager;
import client.MapleCharacter;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import tools.MaplePacketCreator;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

/**
 *
 * @author Ronan
 */
public class MapleMiniDungeon {
    List<MapleCharacter> players = new ArrayList<>();
    ScheduledFuture<?> timeoutTask = null;
    Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MINIDUNGEON, true);
    
    int baseMap;
    long expireTime;
    
    public MapleMiniDungeon(int base, int durationMin) {
        baseMap = base;
        expireTime = durationMin * 60 * 1000;
        
        timeoutTask = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    List<MapleCharacter> lchr = new ArrayList<>(players);
                    
                    for(MapleCharacter chr : lchr) {
                        chr.changeMap(baseMap);
                    }
                    
                    dispose();
                    timeoutTask = null;
                } finally {
                    lock.unlock();
                }
            }
        }, expireTime);
        
        expireTime += System.currentTimeMillis();
    }
    
    public boolean registerPlayer(MapleCharacter chr) {
        int time = (int)((expireTime - System.currentTimeMillis()) / 1000);
        if(time > 0) chr.getClient().announce(MaplePacketCreator.getClock(time));
        
        lock.lock();
        try {
            if(timeoutTask == null) return false;
            
            players.add(chr);
        } finally {
            lock.unlock();
        }
        
        return true;
    }
    
    public boolean unregisterPlayer(MapleCharacter chr) {
        chr.getClient().announce(MaplePacketCreator.removeClock());
        
        lock.lock();
        try {
            players.remove(chr);
            
            if(players.isEmpty()) {
                dispose();
                return false;
            }
            
            return true;
        } finally {
            lock.unlock();
        }
    }
    
    public void dispose() {
        lock.lock();
        try {
            players.clear();
            
            if(timeoutTask != null) {
                timeoutTask.cancel(false);
                timeoutTask = null;
            }
        } finally {
            lock.unlock();
        }
    }
}
