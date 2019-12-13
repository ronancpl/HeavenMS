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

import java.lang.ref.WeakReference;

/**
 *
 * @author Ronan
 */
public class PartySearchCharacter {
    
    private WeakReference<MapleCharacter> player;
    private int level;
    private boolean queued;
    
    public PartySearchCharacter(MapleCharacter chr) {
        player = new WeakReference(chr);
        level = chr.getLevel();
        queued = true;
    }
    
    @Override
    public String toString() {
        MapleCharacter chr = player.get();
        return chr == null ? "[empty]" : chr.toString();
    }
    
    public MapleCharacter callPlayer(int leaderid, int callerMapid) {
        MapleCharacter chr = player.get();
        if (chr == null || !MaplePartySearchCoordinator.isInVicinity(callerMapid, chr.getMapId())) {
            return null;
        }
        
        if (chr.hasDisabledPartySearchInvite(leaderid)) {
            return null;
        }
        
        queued = false;
        if (chr.isLoggedinWorld() && chr.getParty() == null) {
            return chr;
        } else {
            return null;
        }
    }
    
    public MapleCharacter getPlayer() {
        return player.get();
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isQueued() {
        return queued;
    }
    
}
