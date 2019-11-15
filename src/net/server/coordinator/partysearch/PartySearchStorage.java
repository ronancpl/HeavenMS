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
package net.server.coordinator.partysearch;

import client.MapleCharacter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;

import tools.IntervalBuilder;

/**
 *
 * @author Ronan
 */
public class PartySearchStorage {
    
    private List<PartySearchCharacter> storage = new ArrayList<>(20);
    private IntervalBuilder emptyIntervals = new IntervalBuilder();
    
    private final MonitoredReentrantReadWriteLock psLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.WORLD_PARTY_SEARCH_STORAGE, true);
    private final MonitoredReadLock psRLock = MonitoredReadLockFactory.createLock(psLock);
    private final MonitoredWriteLock psWLock = MonitoredWriteLockFactory.createLock(psLock);
    
    public List<PartySearchCharacter> getStorageList() {
        psRLock.lock();
        try {
            return new ArrayList<>(storage);
        } finally {
            psRLock.unlock();
        }
    }
    
    private Map<Integer, MapleCharacter> fetchRemainingPlayers() {
        List<PartySearchCharacter> players = getStorageList();
        Map<Integer, MapleCharacter> remainingPlayers = new HashMap<>(players.size());
        
        for (PartySearchCharacter psc : players) {
            if (psc.isQueued()) {
                MapleCharacter chr = psc.getPlayer();
                if (chr != null) {
                    remainingPlayers.put(chr.getId(), chr);
                }
            }
        }
        
        return remainingPlayers;
    }
    
    public void updateStorage(Collection<MapleCharacter> echelon) {
        Map<Integer, MapleCharacter> newcomers = new HashMap<>();
        for (MapleCharacter chr : echelon) {
            newcomers.put(chr.getId(), chr);
        }
        
        Map<Integer, MapleCharacter> curStorage = fetchRemainingPlayers();
        curStorage.putAll(newcomers);
        
        List<PartySearchCharacter> pscList = new ArrayList<>(curStorage.size());
        for (MapleCharacter chr : curStorage.values()) {
            pscList.add(new PartySearchCharacter(chr));
        }

        Collections.sort(pscList, new Comparator<PartySearchCharacter>() {
            @Override
            public int compare(PartySearchCharacter c1, PartySearchCharacter c2)
            {
                int levelP1 = c1.getLevel(), levelP2 = c2.getLevel();
                return levelP1 > levelP2 ? 1 : (levelP1 == levelP2 ? 0 : -1);
            }
        });
        
        psWLock.lock();
        try {
            storage.clear();
            storage.addAll(pscList);
        } finally {
            psWLock.unlock();
        }
        
        emptyIntervals.clear();
    }
    
    private static int bsearchStorage(List<PartySearchCharacter> storage, int level) {
        int st = 0, en = storage.size() - 1;
        
        int mid, idx;
        while (en >= st) {
            idx = (st + en) / 2;
            mid = storage.get(idx).getLevel();
            
            if (mid == level) {
                return idx;
            } else if (mid < level) {
                st = idx + 1;
            } else {
                en = idx - 1;
            }
        }
        
        return en;
    }
    
    public MapleCharacter callPlayer(int callerCid, int callerMapid, int minLevel, int maxLevel) {
        if (emptyIntervals.inInterval(minLevel, maxLevel)) {
            return null;
        }
        
        List<PartySearchCharacter> pscList = getStorageList();
        
        int idx = bsearchStorage(pscList, maxLevel);
        for (int i = idx; i >= 0; i--) {
            PartySearchCharacter psc = pscList.get(i);
            if (!psc.isQueued()) {
                continue;
            }
            
            if (psc.getLevel() < minLevel) {
                break;
            }
            
            MapleCharacter chr = psc.callPlayer(callerCid, callerMapid);
            if (chr != null) {
                return chr;
            }
        }
        
        emptyIntervals.addInterval(minLevel, maxLevel);
        return null;
    }
    
    public void detachPlayer(MapleCharacter chr) {
        PartySearchCharacter toRemove = null;
        for (PartySearchCharacter psc : getStorageList()) {
            MapleCharacter player = psc.getPlayer();
            
            if (player != null && player.getId() == chr.getId()) {
                toRemove = psc;
                break;
            }
        }
        
        if (toRemove != null) {
            psWLock.lock();
            try {
                storage.remove(toRemove);
            } finally {
                psWLock.unlock();
            }
        }
    }
    
}
