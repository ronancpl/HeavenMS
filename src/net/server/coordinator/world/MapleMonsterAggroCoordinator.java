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
package net.server.coordinator.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import config.YamlConfig;
import client.MapleCharacter;
import java.util.concurrent.ScheduledFuture;
import net.server.Server;
import net.server.audit.LockCollector;
import server.life.MapleMonster;
import server.maps.MapleMap;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.TimerManager;
import tools.Pair;

/**
 *
 * @author Ronan
 */
public class MapleMonsterAggroCoordinator {
    
    private MonitoredReentrantLock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_AGGRO);
    private MonitoredReentrantLock idleLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_AGGRO_IDLE, true);
    private long lastStopTime = Server.getInstance().getCurrentTime();
    
    private ScheduledFuture<?> aggroMonitor = null;
    
    private Map<MapleMonster, Map<Integer, PlayerAggroEntry>> mobAggroEntries = new HashMap<>();
    private Map<MapleMonster, List<PlayerAggroEntry>> mobSortedAggros = new HashMap<>();
    
    private Set<Integer> mapPuppetEntries = new HashSet<>();
    
    private class PlayerAggroEntry {
        protected int cid;
        protected int averageDamage = 0;
        protected int currentDamageInstances = 0;
        protected long accumulatedDamage = 0;
        
        protected int expireStreak = 0;
        protected int updateStreak = 0;
        protected int toNextUpdate = 0;
        protected int entryRank = -1;
        
        protected PlayerAggroEntry(int cid) {
            this.cid = cid;
        }
    }
    
    public void stopAggroCoordinator() {
        idleLock.lock();
        try {
            if (aggroMonitor == null) return;
            
            aggroMonitor.cancel(false);
            aggroMonitor = null;
        } finally {
            idleLock.unlock();
        }
        
        lastStopTime = Server.getInstance().getCurrentTime();
    }
    
    public void startAggroCoordinator() {
        idleLock.lock();
        try {
            if (aggroMonitor != null) return;
            
            aggroMonitor = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    runAggroUpdate(1);
                    runSortLeadingCharactersAggro();
                }
            }, YamlConfig.config.server.MOB_STATUS_AGGRO_INTERVAL, YamlConfig.config.server.MOB_STATUS_AGGRO_INTERVAL);
        } finally {
            idleLock.unlock();
        }
        
        int timeDelta = (int) Math.ceil((Server.getInstance().getCurrentTime() - lastStopTime) / YamlConfig.config.server.MOB_STATUS_AGGRO_INTERVAL);
        if (timeDelta > 0) {
            runAggroUpdate(timeDelta);
        }
    }
    
    private static void updateEntryExpiration(PlayerAggroEntry pae) {
        pae.toNextUpdate = (int) Math.ceil((120000L / YamlConfig.config.server.MOB_STATUS_AGGRO_INTERVAL) / Math.pow(2, pae.expireStreak + pae.currentDamageInstances));
    }
    
    private static void insertEntryDamage(PlayerAggroEntry pae, int damage) {
        synchronized (pae) {
            long totalDamage = pae.averageDamage;
            totalDamage *= pae.currentDamageInstances;
            totalDamage += damage;

            pae.expireStreak = 0;
            pae.updateStreak = 0;
            updateEntryExpiration(pae);

            pae.currentDamageInstances += 1;
            pae.averageDamage = (int)(totalDamage / pae.currentDamageInstances);
            pae.accumulatedDamage = totalDamage;
        }
    }
    
    private static boolean expiredAfterUpdateEntryDamage(PlayerAggroEntry pae, int deltaTime) {
        synchronized (pae) {
            pae.updateStreak += 1;
            pae.toNextUpdate -= deltaTime;
            
            if (pae.toNextUpdate <= 0) {    // reached dmg instance expire time
                pae.expireStreak += 1;
                updateEntryExpiration(pae);

                pae.currentDamageInstances -= 1;
                if (pae.currentDamageInstances < 1) {   // expired aggro for this player
                    return true;
                }
                pae.accumulatedDamage = pae.averageDamage * pae.currentDamageInstances;
            }
            
            return false;
        }
    }
    
    public void addAggroDamage(MapleMonster mob, int cid, int damage) { // assumption: should not trigger after dispose()
        if (!mob.isAlive()) return;
        
        List<PlayerAggroEntry> sortedAggro = mobSortedAggros.get(mob);
        Map<Integer, PlayerAggroEntry> mobAggro = mobAggroEntries.get(mob);
        if (mobAggro == null) {
            if (lock.tryLock()) {   // can run unreliably, as fast as possible... try lock that is!
                try {
                    mobAggro = mobAggroEntries.get(mob);
                    if (mobAggro == null) {
                        mobAggro = new HashMap<>();
                        mobAggroEntries.put(mob, mobAggro);
                        
                        sortedAggro = new LinkedList<>();
                        mobSortedAggros.put(mob, sortedAggro);
                    } else {
                        sortedAggro = mobSortedAggros.get(mob);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                return;
            }
        }
        
        PlayerAggroEntry aggroEntry = mobAggro.get(cid);
        if (aggroEntry == null) {
            aggroEntry = new PlayerAggroEntry(cid);
            
            synchronized (mobAggro) {
                synchronized (sortedAggro) {
                    PlayerAggroEntry mappedEntry = mobAggro.get(cid);
                    
                    if (mappedEntry == null) {
                        mobAggro.put(aggroEntry.cid, aggroEntry);
                        sortedAggro.add(aggroEntry);
                    } else {
                        aggroEntry = mappedEntry;
                    }
                }
            }
        } else if (damage < 1) {
            return;
        }
        
        insertEntryDamage(aggroEntry, damage);
    }
    
    private void runAggroUpdate(int deltaTime) {
        List<Pair<MapleMonster, Map<Integer, PlayerAggroEntry>>> aggroMobs = new LinkedList<>();
        lock.lock();
        try {
            for (Entry<MapleMonster, Map<Integer, PlayerAggroEntry>> e : mobAggroEntries.entrySet()) {
                aggroMobs.add(new Pair<>(e.getKey(), e.getValue()));
            }
        } finally {
            lock.unlock();
        }
        
        for (Pair<MapleMonster, Map<Integer, PlayerAggroEntry>> am : aggroMobs) {
            Map<Integer, PlayerAggroEntry> mobAggro = am.getRight();
            List<PlayerAggroEntry> sortedAggro = mobSortedAggros.get(am.getLeft());
                    
            if (sortedAggro != null) {
                List<Integer> toRemove = new LinkedList<>();
                List<Integer> toRemoveIdx = new ArrayList<>(mobAggro.size());
                List<Integer> toRemoveByFetch = new LinkedList<>();

                synchronized (mobAggro) {
                    synchronized (sortedAggro) {
                        for (PlayerAggroEntry pae : mobAggro.values()) {
                            if (expiredAfterUpdateEntryDamage(pae, deltaTime)) {
                                toRemove.add(pae.cid);
                                if (pae.entryRank > -1) toRemoveIdx.add(pae.entryRank);
                                else toRemoveByFetch.add(pae.cid);
                            }
                        }

                        if (!toRemove.isEmpty()) {
                            for (Integer cid : toRemove) {
                                mobAggro.remove(cid);
                            }
                            
                            if (mobAggro.isEmpty()) {   // all aggro on this mob expired
                                if (!am.getLeft().isBoss()) {
                                    am.getLeft().aggroResetAggro();
                                }
                            }
                        }

                        if (!toRemoveIdx.isEmpty()) {
                            Collections.sort(toRemoveIdx, new Comparator<Integer>() {   // last to first indexes
                                @Override
                                public int compare(Integer p1, Integer p2) {
                                    return p1 < p2 ? 1 : p1.equals(p2) ? 0 : -1;
                                }
                            });

                            for (int idx : toRemoveIdx) {
                                sortedAggro.remove(idx);
                            }
                        }
                        
                        if (!toRemoveByFetch.isEmpty()) {
                            for (Integer cid : toRemoveByFetch) {
                                for (int i = 0; i < sortedAggro.size(); i++) {
                                    if (cid.equals(sortedAggro.get(i).cid)) {
                                        sortedAggro.remove(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void insertionSortAggroList(List<PlayerAggroEntry> paeList) {
        for (int i = 1; i < paeList.size(); i++) {
            PlayerAggroEntry pae = paeList.get(i);
            long curAccDmg = pae.accumulatedDamage;

            int j = i - 1;
            while (j >= 0 && curAccDmg > paeList.get(j).accumulatedDamage) {
                j -= 1;
            }

            j += 1;
            if (j != i) {
                paeList.remove(i);
                paeList.add(j, pae);
            }
        }

        int i = 0;
        for (PlayerAggroEntry pae : paeList) {
            pae.entryRank = i;
            i += 1;
        }
    }
    
    public boolean isLeadingCharacterAggro(MapleMonster mob, MapleCharacter player) {
        if (mob.isLeadingPuppetInVicinity()) {
            return false;
        } else if (mob.isCharacterPuppetInVicinity(player)) {
            return true;
        }
        
        // by assuming the quasi-sorted nature of "mobAggroList", this method
        // returns whether the player given as parameter can be elected as next aggro leader
        
        List<PlayerAggroEntry> mobAggroList = mobSortedAggros.get(mob);
        if (mobAggroList != null) {
            synchronized (mobAggroList) {
                mobAggroList = new ArrayList<>(mobAggroList.subList(0, Math.min(mobAggroList.size(), 5)));
            }
            
            MapleMap map = mob.getMap();
            for (PlayerAggroEntry pae : mobAggroList) {
                MapleCharacter chr = map.getCharacterById(pae.cid);
                if (chr != null) {
                    if (player.getId() == pae.cid) {
                        return true;
                    } else if (pae.updateStreak < YamlConfig.config.server.MOB_STATUS_AGGRO_PERSISTENCE && chr.isAlive()) {  // verifies currently leading players activity
                        return false;
                    }
                }
            }
        }
        
        return false;
    }
    
    public void runSortLeadingCharactersAggro() {
        List<List<PlayerAggroEntry>> aggroList;
        lock.lock();
        try {
            aggroList = new ArrayList<>(mobSortedAggros.values());
        } finally {
            lock.unlock();
        }
        
        for (List<PlayerAggroEntry> mobAggroList : aggroList) {
            synchronized (mobAggroList) {
                insertionSortAggroList(mobAggroList);
            }
        }
    }
    
    public void removeAggroEntries(MapleMonster mob) {
        lock.lock();
        try {
            mobAggroEntries.remove(mob);
            mobSortedAggros.remove(mob);
        } finally {
            lock.unlock();
        }
    }
    
    public void addPuppetAggro(MapleCharacter player) {
        synchronized (mapPuppetEntries) {
            mapPuppetEntries.add(player.getId());
        }
    }
    
    public void removePuppetAggro(Integer cid) {
        synchronized (mapPuppetEntries) {
            mapPuppetEntries.remove(cid);
        }
    }
    
    public List<Integer> getPuppetAggroList() {
        synchronized (mapPuppetEntries) {
            return new ArrayList<>(mapPuppetEntries);
        }
    }
    
    public void dispose() {
        stopAggroCoordinator();
        
        lock.lock();
        try {
            mobAggroEntries.clear();
            mobSortedAggros.clear();
        } finally {
            lock.unlock();
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
        lock = lock.dispose();
    }
}
