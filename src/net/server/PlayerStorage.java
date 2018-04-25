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

import client.MapleCharacter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.locks.MonitoredReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import tools.locks.MonitoredLockType;

public class PlayerStorage {
    private final ReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.PLAYER_STORAGE, true);
    private final ReadLock rlock = locks.readLock();
    private final WriteLock wlock = locks.writeLock();
    private final Map<Integer, MapleCharacter> storage = new LinkedHashMap<>();
    private final Map<String, MapleCharacter> nameStorage = new LinkedHashMap<>();

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
            return storage.values();
        } finally {
            rlock.unlock();
        }
    }

    public final void disconnectAll() {
	wlock.lock();
	try {	    
            final Iterator<MapleCharacter> chrit = storage.values().iterator();
	    while (chrit.hasNext()) {
                chrit.next().getClient().disconnect(true, false);
                chrit.remove();
            }
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