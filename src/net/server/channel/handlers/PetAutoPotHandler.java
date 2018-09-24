/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
package net.server.channel.handlers;

import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.ServerConstants;
import java.util.List;

/**
 *
 * @author Ronan (multi-pot consumption feature)
 */
public final class PetAutoPotHandler extends AbstractMaplePacketHandler {
    short slot;
    int itemId;
    Item toUse;
    List<Item> toUseList;
    
    boolean hasHpGain, hasMpGain;
    int maxHp, maxMp, curHp, curMp;
    double incHp, incMp;
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        slea.readByte();
        slea.readLong();
        slea.readInt();
        slot = slea.readShort();
        itemId = slea.readInt();
        
        MapleCharacter chr = c.getPlayer();
        MapleInventory useInv = chr.getInventory(MapleInventoryType.USE);
        
        int useCount = 0, qtyCount = 0;
        MapleStatEffect stat = null;
        
        useInv.lockInventory();
        try {
            toUse = useInv.getItem(slot);
            
            if (toUse != null) {
                if (toUse.getItemId() != itemId) {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                
                toUseList = null;
                
                // from now on, toUse becomes the "cursor" for the current pot being used
                if (toUse.getQuantity() <= 0) {
                    if (!cursorOnNextAvailablePot(chr)) {
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                }
                
                stat = MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId());
                hasHpGain = stat.getHp() > 0 || stat.getHpRate() > 0.0;
                hasMpGain = stat.getMp() > 0 || stat.getMpRate() > 0.0;
                
                maxHp = chr.getCurrentMaxHp();
                maxMp = chr.getCurrentMaxMp();
                
                curHp = chr.getHp();
                curMp = chr.getMp();
                
                incHp = stat.getHp();
                if(incHp <= 0 && hasHpGain) incHp = Math.ceil(maxHp * stat.getHpRate());
                
                incMp = stat.getMp();
                if(incMp <= 0 && hasMpGain) incMp = Math.ceil(maxMp * stat.getMpRate());
                
                if (hasHpGain) {
                    qtyCount = (int) Math.ceil(((ServerConstants.PET_AUTOHP_RATIO * maxHp) - curHp) / incHp);
                }
                
                if (hasMpGain) {
                    qtyCount = Math.max(qtyCount, (int) Math.ceil(((ServerConstants.PET_AUTOMP_RATIO * maxMp) - curMp) / incMp));
                }

                while (true) {
                    short qtyToUse = (short) Math.min(qtyCount, toUse.getQuantity());
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, qtyToUse, false);
                    
                    curHp += (incHp * qtyToUse);
                    curMp += (incMp * qtyToUse);
                    
                    useCount += qtyToUse;
                    qtyCount -= qtyToUse;
                    
                    if(toUse.getQuantity() == 0 && qtyCount > 0) {
                        // depleted out the current slot, fetch for more

                        if(!cursorOnNextAvailablePot(chr)) {
                            break;    // no more pots available
                        }
                    } else {
                        break;    // gracefully finished it's job, quit the loop
                    }
                }
            }
        } finally {
            useInv.unlockInventory();
        }
        
        for (int i = 0; i < useCount; i++) {
            stat.applyTo(chr);
        }
        
        chr.announce(MaplePacketCreator.enableActions());
    }
    
    private boolean cursorOnNextAvailablePot(MapleCharacter chr) {
        if(toUseList == null) {
            toUseList = chr.getInventory(MapleInventoryType.USE).linkedListById(itemId);
        }

        toUse = null;
        while(!toUseList.isEmpty()) {
            Item it = toUseList.remove(0);

            if(it.getQuantity() > 0) {
                toUse = it;
                slot = it.getPosition();

                return true;
            }
        }
        
        return false;
    } 
}
