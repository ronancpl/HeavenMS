/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

import java.awt.Point;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import client.MapleCharacter;
import client.MapleClient;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan
 */
public class MapleDoorObject extends AbstractMapleMapObject {
    private final int ownerId;
    private int pairOid;
    
    private final MapleMap from;
    private final MapleMap to;
    private int linkedPortalId;
    private Point linkedPos;
    
    private final ReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.PLAYER_DOOR, true);
    private ReentrantReadWriteLock.ReadLock rlock = locks.readLock();
    private ReentrantReadWriteLock.WriteLock wlock = locks.writeLock();
    
    public MapleDoorObject(int owner, MapleMap destination, MapleMap origin, int townPortalId, Point targetPosition, Point toPosition) {
        super();
        setPosition(targetPosition);
        
        ownerId = owner;
        linkedPortalId = townPortalId;
        from = origin;
        to = destination;
        linkedPos = toPosition;
    }
    
    public void update(int townPortalId, Point toPosition) {
        wlock.lock();
        try {
            linkedPortalId = townPortalId;
            linkedPos = toPosition;
        } finally {
            wlock.unlock();
        }
    }
    
    private int getLinkedPortalId() {
        rlock.lock();
        try {
            return linkedPortalId;
        } finally {
            rlock.unlock();
        }
    }
    
    private Point getLinkedPortalPosition() {
        rlock.lock();
        try {
            return linkedPos;
        } finally {
            rlock.unlock();
        }
    }
    
    public void warp(final MapleCharacter chr) {
        boolean onParty = chr.getParty() != null;
        
        if (chr.getId() == ownerId || (onParty && chr.getParty().getMemberById(ownerId) != null)) {
            if(!inTown() && !onParty) {
                chr.changeMap(to, getLinkedPortalId());
            } else {
                chr.changeMap(to, getLinkedPortalPosition());
            }
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
            client.announce(MaplePacketCreator.removeDoor(ownerId, inTown()));
        }
    }
    
    public void sendDestroyData(MapleClient client, boolean partyUpdate) {
        if (client != null && from.getId() == client.getPlayer().getMapId()) {
            client.announce(MaplePacketCreator.partyPortal(999999999, 999999999, new Point(-1, -1)));
            client.announce(MaplePacketCreator.removeDoor(ownerId, inTown()));
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
        return getLinkedPortalId() == -1;
    }
    
    public MapleMap getFrom() {
        return from;
    }
    
    public MapleMap getTo() {
        return to;
    }
    
    public MapleMap getTown() {
        return inTown() ? from : to;
    }
    
    public MapleMap getArea() {
        return !inTown() ? from : to;
    }
    
    public Point getAreaPosition() {
        return !inTown() ? getPosition() : getLinkedPortalPosition();
    }
    
    public Point toPosition() {
        return getLinkedPortalPosition();
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
