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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MapleMessenger {
	
    private int id;
    private List<MapleMessengerCharacter> members = new ArrayList<MapleMessengerCharacter>(3);
    private boolean[] pos = new boolean[3];

    public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
        this.id = id;  
    	for (int i = 0; i < 3; i++){
    		pos[i] = false;
    	}
        addMember(chrfor, chrfor.getPosition());
    }
    
    public int getId() {
        return id;
    }
    
    public Collection<MapleMessengerCharacter> getMembers() {
        return Collections.unmodifiableList(members);
    }
    
    public void addMember(MapleMessengerCharacter member, int position) {
        members.add(member);
        member.setPosition(position);
        pos[position] = true;
    }

    public void removeMember(MapleMessengerCharacter member) {
    	int position = member.getPosition();
        pos[position] = false;
        members.remove(member);
    }

    public int getLowestPosition() {
        for (byte i = 0; i < 3; i++) {
            if (!pos[i]) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByName(String name) {
        for (MapleMessengerCharacter messengerchar : members) {
            if (messengerchar.getName().equals(name)) {
                return messengerchar.getPosition();
            }
        }
        return -1;
    }
}

