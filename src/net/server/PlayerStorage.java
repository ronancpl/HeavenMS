/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
package net.server;

import client.MapleClient;
import client.MapleCharacter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;

public class PlayerStorage {
    private final MonitoredReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.PLAYER_STORAGE, true);
    private final Map<Integer, MapleCharacter> storage = new LinkedHashMap<>();
    private final Map<String, MapleCharacter> nameStorage = new LinkedHashMap<>();
    private MonitoredReadLock rlock = MonitoredReadLockFactory.createLock(locks);
    private MonitoredWriteLock wlock = MonitoredWriteLockFactory.createLock(locks);

    public void addPlayer(MapleCharacter chr) {
        wlock.lock();
        try {
            storage.put(chr.getId(), chr);
            nameStorage.put(chr.getName().toLowerCase(), chr);
        } finally {
	    wlock.unlock();
	}
    }

    public MapleCharacter removePlayer(int chr) {
        wlock.lock();
        try {
            MapleCharacter mc = storage.remove(chr);
            if(mc != null) nameStorage.remove(mc.getName().toLowerCase());
            
            return mc;
        } finally {
            wlock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        rlock.lock();    
        try {
            return nameStorage.get(name.toLowerCase());
        } finally {
            rlock.unlock();
        }
    }

    public MapleCharacter getCharacterById(int id) { 
        rlock.lock();    
        try {
            return storage.get(id);
        } finally {
            rlock.unlock();
        }
    }

    public Collection<MapleCharacter> getAllCharacters() {
        rlock.lock();
        try {
            return new ArrayList<>(storage.values());
        } finally {
            rlock.unlock();
        }
    }

    public final void disconnectAll() {
        List<MapleCharacter> chrList;
	rlock.lock();
	try {
            chrList = new ArrayList<>(storage.values());
	} finally {
	    rlock.unlock();
	}
        
        for(MapleCharacter mc : chrList) {
            MapleClient client = mc.getClient();
            if(client != null) {
                client.forceDisconnect();
            }
        }
        
        wlock.lock();
	try {
            storage.clear();
	} finally {
	    wlock.unlock();
	}
    }
    
    public int getSize() {
        rlock.lock();
        try {
            return storage.size();
        } finally {
            rlock.unlock();
        }
    }
}