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
package server.maps;

import client.MapleCharacter;
import java.awt.Point;

import tools.MaplePacketCreator;
import client.MapleClient;

/**
 *
 * @author Ronan
 */
public class MapleDoorObject extends AbstractMapleMapObject {
    private int ownerId;
    private int pairOid;
    
    private boolean isTown;
    private MapleMap from;
    private MapleMap to;
    private Point toPos;
    
    public MapleDoorObject(int owner, MapleMap destination, MapleMap origin, boolean town, Point targetPosition, Point toPosition) {
        super();
        setPosition(targetPosition);
        
        ownerId = owner;
        isTown = town;
        from = origin;
        to = destination;
        toPos = toPosition;
    }
    
    public void warp(final MapleCharacter chr, boolean toTown) {
        if (chr.getId() == ownerId || (chr.getParty() != null && chr.getParty().getMemberById(ownerId) != null)) {
            if(chr.getParty() == null && (to.isLastDoorOwner(chr.getId()) || toTown)) chr.changeMap(to, toPos);
            else chr.changeMap(to, to.findClosestPlayerSpawnpoint(toPos));    // weird issues happens with party, relocating players elsewhere....
        } else {
            chr.getClient().announce(MaplePacketCreator.blockedMessage(6));
            chr.getClient().announce(MaplePacketCreator.enableActions());
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (from.getId() == client.getPlayer().getMapId()) {
            if (client.getPlayer().getParty() != null && (ownerId == client.getPlayer().getId() || client.getPlayer().getParty().getMemberById(ownerId) != null)) {
                client.announce(MaplePacketCreator.partyPortal(this.getFrom().getId(), this.getTo().getId(), this.toPosition()));
            }
            
            client.announce(MaplePacketCreator.spawnPortal(this.getFrom().getId(), this.getTo().getId(), this.toPosition()));
            if(!this.inTown()) client.announce(MaplePacketCreator.spawnDoor(this.getOwnerId(), this.getPosition(), true));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if (from.getId() == client.getPlayer().getMapId()) {
            if (client.getPlayer().getParty() != null && (ownerId == client.getPlayer().getId() || client.getPlayer().getParty().getMemberById(ownerId) != null)) {
                client.announce(MaplePacketCreator.partyPortal(999999999, 999999999, new Point(-1, -1)));
            }
            client.announce(MaplePacketCreator.removeDoor(ownerId, isTown));
        }
    }
    
    public int getOwnerId() {
        return ownerId;
    }
    
    public void setPairOid(int oid) {
        this.pairOid = oid;
    }
    
    public int getPairOid() {
        return pairOid;
    }
    
    public boolean inTown() {
        return isTown;
    }
    
    public MapleMap getFrom() {
        return from;
    }
    
    public MapleMap getTo() {
        return to;
    }
    
    public MapleMap getTown() {
        return isTown ? from : to;
    }
    
    public MapleMap getArea() {
        return !isTown ? from : to;
    }
    
    public Point getAreaPosition() {
        return !isTown ? getPosition() : toPos;
    }
    
    public Point toPosition() {
        return toPos;
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
