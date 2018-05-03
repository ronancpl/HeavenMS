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
package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import net.server.PlayerStorage;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public class BuddyList {
    public enum BuddyOperation {
        ADDED, DELETED
    }

    public enum BuddyAddResult {
        BUDDYLIST_FULL, ALREADY_ON_LIST, OK
    }
    private Map<Integer, BuddylistEntry> buddies = new LinkedHashMap<>();
    private int capacity;
    private Deque<CharacterNameAndId> pendingRequests = new LinkedList<>();

    public BuddyList(int capacity) {
        this.capacity = capacity;
    }

    public boolean contains(int characterId) {
        synchronized(buddies) {
            return buddies.containsKey(Integer.valueOf(characterId));
        }
    }

    public boolean containsVisible(int characterId) {
        BuddylistEntry ble;
        synchronized(buddies) {
            ble = buddies.get(characterId);
        }
        
        if (ble == null) {
            return false;
        }
        return ble.isVisible();
        
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BuddylistEntry get(int characterId) {
        synchronized(buddies) {
            return buddies.get(Integer.valueOf(characterId));
        }
    }

    public BuddylistEntry get(String characterName) {
        String lowerCaseName = characterName.toLowerCase();
        for (BuddylistEntry ble : getBuddies()) {
            if (ble.getName().toLowerCase().equals(lowerCaseName)) {
                return ble;
            }
        }
        
        return null;
    }

    public void put(BuddylistEntry entry) {
        synchronized(buddies) {
            buddies.put(Integer.valueOf(entry.getCharacterId()), entry);
        }
    }

    public void remove(int characterId) {
        synchronized(buddies) {
            buddies.remove(Integer.valueOf(characterId));
        }
    }

    public Collection<BuddylistEntry> getBuddies() {
        synchronized(buddies) {
            return Collections.unmodifiableCollection(buddies.values());
        }
    }

    public boolean isFull() {
        synchronized(buddies) {
            return buddies.size() >= capacity;
        }
    }

    public int[] getBuddyIds() {
        synchronized(buddies) {
            int buddyIds[] = new int[buddies.size()];
            int i = 0;
            for (BuddylistEntry ble : buddies.values()) {
                buddyIds[i++] = ble.getCharacterId();
            }
            return buddyIds;
        }
    }
    
    public void broadcast(byte[] packet, PlayerStorage pstorage) {
        for(int bid : getBuddyIds()) {
            MapleCharacter chr = pstorage.getCharacterById(bid);
            
            if(chr != null && chr.isLoggedinWorld()) {
                chr.announce(packet);
            }
        }
    }

    public void loadFromDb(int characterId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("SELECT b.buddyid, b.pending, b.group, c.name as buddyname FROM buddies as b, characters as c WHERE c.id = b.buddyid AND b.characterid = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("pending") == 1) {
                    pendingRequests.push(new CharacterNameAndId(rs.getInt("buddyid"), rs.getString("buddyname")));
                } else {
                    put(new BuddylistEntry(rs.getString("buddyname"), rs.getString("group"), rs.getInt("buddyid"), (byte) -1, true));
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM buddies WHERE pending = 1 AND characterid = ?");
            ps.setInt(1, characterId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public CharacterNameAndId pollPendingRequest() {
        return pendingRequests.pollLast();
    }

    public void addBuddyRequest(MapleClient c, int cidFrom, String nameFrom, int channelFrom) {
        put(new BuddylistEntry(nameFrom, "Default Group", cidFrom, channelFrom, false));
        if (pendingRequests.isEmpty()) {
            c.announce(MaplePacketCreator.requestBuddylistAdd(cidFrom, c.getPlayer().getId(), nameFrom));
        } else {
            pendingRequests.push(new CharacterNameAndId(cidFrom, nameFrom));
        }
    }
}
