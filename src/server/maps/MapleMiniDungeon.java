/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import server.TimerManager;
import client.MapleCharacter;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan
 */
public class MapleMiniDungeon {
    List<MapleCharacter> players = new ArrayList<>();
    ScheduledFuture<?> timeoutTask = null;
    Lock lock = new ReentrantLock();
    
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
