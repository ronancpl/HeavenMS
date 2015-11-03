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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerStorage {
    private final ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
    private final Lock rlock = locks.readLock();
    private final Lock wlock = locks.writeLock();
    private final Map<Integer, MapleCharacter> storage = new LinkedHashMap<>();

    public void addPlayer(MapleCharacter chr) {
        wlock.lock();
        try {
            storage.put(chr.getId(), chr);
        } finally {
	    wlock.unlock();
	}
    }

    public MapleCharacter removePlayer(int chr) {
        wlock.lock();
        try {
            return storage.remove(chr);
        } finally {
            wlock.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        rlock.lock();    
        try {
            for (MapleCharacter chr : storage.values()) {            
                if (chr.getName().toLowerCase().equals(name.toLowerCase()))
                    return chr;
            }
            return null;
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
    
    public int getSize(){
    	return storage.size();
    }
}