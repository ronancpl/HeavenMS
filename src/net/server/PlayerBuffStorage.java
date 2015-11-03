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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Danny//changed to map :3
 */
public class PlayerBuffStorage {
    private int id = (int) (Math.random() * 100);
    private final Lock mutex = new ReentrantLock();    
    private Map<Integer, List<PlayerBuffValueHolder>> buffs = new HashMap<Integer, List<PlayerBuffValueHolder>>();

    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
        mutex.lock();
        try {
            buffs.put(chrid, toStore);//Old one will be replace if it's in here.
        } finally {
            mutex.unlock();
        }
    }

    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
        mutex.lock();
        try {
            return buffs.remove(chrid);
        } finally {
            mutex.unlock();
        }        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerBuffStorage other = (PlayerBuffStorage) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
