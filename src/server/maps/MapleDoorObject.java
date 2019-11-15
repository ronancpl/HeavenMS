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
package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReadLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.MonitoredWriteLock;
import net.server.audit.locks.factory.MonitoredReadLockFactory;
import net.server.audit.locks.factory.MonitoredWriteLockFactory;
import net.server.world.MapleParty;
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
    
    private final MonitoredReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.PLAYER_DOOR, true);
    private MonitoredReadLock rlock = MonitoredReadLockFactory.createLock(locks);
    private MonitoredWriteLock wlock = MonitoredWriteLockFactory.createLock(locks);
    
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
        MapleParty party = chr.getParty();
        if (chr.getId() == ownerId || (party != null && party.getMemberById(ownerId) != null)) {
            chr.announce(MaplePacketCreator.playPortalSound());
            
            if(!inTown() && party == null) {
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
        sendSpawnData(client, true);
    }
    
    public void sendSpawnData(MapleClient client, boolean launched) {
        MapleCharacter chr = client.getPlayer();
        if (this.getFrom().getId() == chr.getMapId()) {
            if (chr.getParty() != null && (this.getOwnerId() == chr.getId() || chr.getParty().getMemberById(this.getOwnerId()) != null)) {
                chr.announce(MaplePacketCreator.partyPortal(this.getFrom().getId(), this.getTo().getId(), this.toPosition()));
            }

            chr.announce(MaplePacketCreator.spawnPortal(this.getFrom().getId(), this.getTo().getId(), this.toPosition()));
            if (!this.inTown()) {
                chr.announce(MaplePacketCreator.spawnDoor(this.getOwnerId(), this.getPosition(), launched));
            }
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        MapleCharacter chr = client.getPlayer();
        if (from.getId() == chr.getMapId()) {
            MapleParty party = chr.getParty();
            if (party != null && (ownerId == chr.getId() || party.getMemberById(ownerId) != null)) {
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
