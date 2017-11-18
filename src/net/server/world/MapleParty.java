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
package net.server.world;

import client.MapleClient;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Comparator;
import tools.locks.MonitoredReentrantLock;
import java.util.concurrent.locks.Lock;
import tools.locks.MonitoredLockType;

public class MapleParty {
    private int id;
    
    private int leaderId;
    private List<MaplePartyCharacter> members = new LinkedList<>();
    private List<MaplePartyCharacter> pqMembers = null;
    
    private Map<Integer, Integer> histMembers = new HashMap<>();
    private int nextEntry = 0;
    
    private Lock lock = new MonitoredReentrantLock(MonitoredLockType.PARTY, true);
    
    public MapleParty(int id, MaplePartyCharacter chrfor) {
        this.leaderId = chrfor.getId();
        this.members.add(chrfor);
        this.id = id;
    }

    public boolean containsMembers(MaplePartyCharacter member) {
        lock.lock();
        try {
            return members.contains(member);
        } finally {
            lock.unlock();
        }
    }

    public void addMember(MaplePartyCharacter member) {
        lock.lock();
        try {
            histMembers.put(member.getId(), nextEntry);
            nextEntry++;

            members.add(member);
        } finally {
            lock.unlock();
        }
    }

    public void removeMember(MaplePartyCharacter member) {
        lock.lock();
        try {
            histMembers.remove(member.getId());

            members.remove(member);
        } finally {
            lock.unlock();
        }
    }

    public void setLeader(MaplePartyCharacter victim) {
        this.leaderId = victim.getId();
    }

    public void updateMember(MaplePartyCharacter member) {
        lock.lock();
        try {
            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).getId() == member.getId()) {
                    members.set(i, member);
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public MaplePartyCharacter getMemberById(int id) {
        lock.lock();
        try {
            for (MaplePartyCharacter chr : members) {
                if (chr.getId() == id) {
                    return chr;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public Collection<MaplePartyCharacter> getMembers() {
        lock.lock();
        try {
            return Collections.unmodifiableList(members);
        } finally {
            lock.unlock();
        }
    }
    
    public List<MaplePartyCharacter> getPartyMembers() {
        lock.lock();
        try {
            return Collections.unmodifiableList(members);
        } finally {
            lock.unlock();
        }
    }
    
    // used whenever entering PQs: will draw every party member that can attempt a target PQ while ingnoring those unfit.
    public Collection<MaplePartyCharacter> getEligibleMembers() {
        return Collections.unmodifiableList(pqMembers);
    }
    
    public void setEligibleMembers(List<MaplePartyCharacter> eliParty) {
        pqMembers = eliParty;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getLeaderId() {
        return leaderId;
    }

    public MaplePartyCharacter getLeader() {
        lock.lock();
        try {
            for(MaplePartyCharacter mpc: members) {
                if(mpc.getId() == leaderId) {
                    return mpc;
                }
            }

            return null;
        } finally {
            lock.unlock();
        }
    }
    
    public byte getPartyDoor(int cid) {
        List<Entry<Integer, Integer>> histList;
        
        lock.lock();
        try {
            histList = new LinkedList<>(histMembers.entrySet());
        } finally {
            lock.unlock();
        }
        
        Collections.sort(histList, new Comparator<Entry<Integer, Integer>>()
            {
                @Override
                public int compare( Entry<Integer, Integer> o1, Entry<Integer, Integer> o2 )
                {
                    return ( o1.getValue() ).compareTo( o2.getValue() );
                }
            });

        byte slot = 0;
        for(Entry<Integer, Integer> e: histList) {
            if(e.getKey() == cid) break;
            slot++;
        }

        return slot;
    }
    
    public void assignNewLeader(MapleClient c) {
        World world = c.getWorldServer();
        MaplePartyCharacter newLeadr = null;
        
        lock.lock();
        try {
            for(MaplePartyCharacter mpc : members) {
                if(mpc.getId() != leaderId && (newLeadr == null || newLeadr.getLevel() < mpc.getLevel())) {
                    newLeadr = mpc;
                }
            }
        } finally {
            lock.unlock();
        }

        if(newLeadr != null) world.updateParty(this.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
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
        final MapleParty other = (MapleParty) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
