/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import java.awt.Point;
import java.util.concurrent.locks.Lock;
import tools.MaplePacketCreator;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

public class MapleMapItem extends AbstractMapleMapObject {
    protected MapleClient ownerClient;
    protected Item item;
    protected MapleMapObject dropper;
    protected int character_ownerid, party_ownerid, meso, questid = -1;
    protected byte type;
    protected boolean pickedUp = false, playerDrop, partyDrop;
    protected long dropTime;
    private Lock itemLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_ITEM);

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, MapleClient ownerClient, byte type, boolean playerDrop) {
	setPosition(position);
	this.item = item;
	this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.party_ownerid = owner.getPartyId();
        this.partyDrop = this.party_ownerid != -1;
        this.ownerClient = owner.getClient();
	this.meso = 0;
	this.type = type;
	this.playerDrop = playerDrop;
    }

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, MapleClient ownerClient, byte type, boolean playerDrop, int questid) {
	setPosition(position);
	this.item = item;
	this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.party_ownerid = owner.getPartyId();
        this.partyDrop = this.party_ownerid != -1;
	this.ownerClient = owner.getClient();
        this.meso = 0;
	this.type = type;
	this.playerDrop = playerDrop;
	this.questid = questid;
    }

    public MapleMapItem(int meso, Point position, MapleMapObject dropper, MapleCharacter owner, MapleClient ownerClient, byte type, boolean playerDrop) {
	setPosition(position);
	this.item = null;
	this.dropper = dropper;
	this.character_ownerid = owner.getId();
        this.party_ownerid = owner.getPartyId();
        this.partyDrop = this.party_ownerid != -1;
        this.ownerClient = owner.getClient();
        this.meso = meso;
	this.type = type;
	this.playerDrop = playerDrop;
    }

    public final Item getItem() {
	return item;
    }

    public final int getQuest() {
	return questid;
    }

    public final int getItemId() {
	if (meso > 0) return meso;
	return item.getItemId();
    }

    public final MapleMapObject getDropper() {
	return dropper;
    }

    public final int getOwnerId() {
	return character_ownerid;
    }
    
    public final int getPartyOwnerId() {
        return party_ownerid;
    }
    
    public final void setPartyOwnerId(int partyid) {
        party_ownerid = partyid;
    }
    
    public final int getClientsideOwnerId() {   // thanks nozphex (RedHat) for noting an issue with collecting party items
        if (this.party_ownerid == -1) {
            return this.character_ownerid;
        } else {
            return this.party_ownerid;
        }
    }
    
    public final boolean hasClientsideOwnership(MapleCharacter player) {
        return this.character_ownerid == player.getId() || this.party_ownerid == player.getPartyId() || hasExpiredOwnershipTime();
    }
    
    public final boolean isFFADrop() {
        return type == 2 || type == 3 || hasExpiredOwnershipTime();
    }
    
    public final boolean hasExpiredOwnershipTime() {
        return System.currentTimeMillis() - dropTime >= 15 * 1000;
    }
    
    public final boolean canBePickedBy(MapleCharacter chr) {
        if (character_ownerid <= 0 || isFFADrop()) return true;
        
        if (party_ownerid == -1) {
            if (chr.getId() == character_ownerid) {
                return true;
            } else if (chr.isPartyMember(character_ownerid)) {
                party_ownerid = chr.getPartyId();
                return true;
            }
        } else {
            if (chr.getPartyId() == party_ownerid) {
                return true;
            } else if (chr.getId() == character_ownerid) {
                party_ownerid = chr.getPartyId();
                return true;
            }
        }
        
        return hasExpiredOwnershipTime();
    }
    
    public final MapleClient getOwnerClient() {
	return (ownerClient.isLoggedIn() && !ownerClient.getPlayer().isAwayFromWorld()) ? ownerClient : null;
    }

    public final int getMeso() {
	return meso;
    }

    public final boolean isPlayerDrop() {
	return playerDrop;
    }

    public final boolean isPickedUp() {
	return pickedUp;
    }

    public void setPickedUp(final boolean pickedUp) {
	this.pickedUp = pickedUp;
    }
	
    public long getDropTime() {
        return dropTime;
    }

    public void setDropTime(long time) {
        this.dropTime = time;
    }

    public byte getDropType() {
	return type;
    }
    
    public void lockItem() {
        itemLock.lock();
    }
    
    public void unlockItem() {
        itemLock.unlock();
    }

    @Override
    public final MapleMapObjectType getType() {
	return MapleMapObjectType.ITEM;
    }

    @Override
    public void sendSpawnData(final MapleClient client) {
        MapleCharacter chr = client.getPlayer();
        
	if (chr.needQuestItem(questid, getItemId())) {
	    this.lockItem();
            try {
                client.announce(MaplePacketCreator.dropItemFromMapObject(chr, this, null, getPosition(), (byte) 2));
            } finally {
                this.unlockItem();
            }
	}
    }

    @Override
    public void sendDestroyData(final MapleClient client) {
	client.announce(MaplePacketCreator.removeItemFromMap(getObjectId(), 1, 0));
    }
}