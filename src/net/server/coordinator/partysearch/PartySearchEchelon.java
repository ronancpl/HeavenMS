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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;

import java.lang.ref.WeakReference;

/**
 *
 * @author Ronan
 */
public class PartySearchEchelon {
    
    private final MonitoredReentrantReadWriteLock psLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.WORLD_PARTY_SEARCH_ECHELON, true);
    private final MonitoredReadLock psRLock = MonitoredReadLockFactory.createLock(psLock);
    private final MonitoredWriteLock psWLock = MonitoredWriteLockFactory.createLock(psLock);
    
    private Map<Integer, WeakReference<MapleCharacter>> echelon = new HashMap<>(20);
    
    public List<MapleCharacter> exportEchelon() {
        psWLock.lock();     // reversing read/write actually could provide a lax yet sure performance/precision trade-off here
        try {
            List<MapleCharacter> players = new ArrayList<>(echelon.size());
            
            for (WeakReference<MapleCharacter> chrRef : echelon.values()) {
                MapleCharacter chr = chrRef.get();
                if (chr != null) {
                    players.add(chr);
                }
            }
            
            echelon.clear();
            return players;
        } finally {
            psWLock.unlock();
        }
    }
    
    public void attachPlayer(MapleCharacter chr) {
        psRLock.lock();
        try {
            echelon.put(chr.getId(), new WeakReference<>(chr));
        } finally {
            psRLock.unlock();
        }
    }
    
    public boolean detachPlayer(MapleCharacter chr) {
        psRLock.lock();
        try {
            return echelon.remove(chr.getId()) != null;
        } finally {
            psRLock.unlock();
        }
    }
    
}
