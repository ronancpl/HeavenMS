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
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public final class UseItemHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        
        if (!chr.isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt();
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId) {
            if (itemId == 2022178 || itemId == 2050004) {
                chr.dispelDebuffs();
                remove(c, slot);
                return;
            } else if (itemId == 2050001) {
		chr.dispelDebuff(MapleDisease.DARKNESS);
                remove(c, slot);
                return;
	    } else if (itemId == 2050002) {
		chr.dispelDebuff(MapleDisease.WEAKEN);
                chr.dispelDebuff(MapleDisease.SLOW);
                remove(c, slot);
                return;
            } else if (itemId == 2050003) {
                chr.dispelDebuff(MapleDisease.SEAL);
		chr.dispelDebuff(MapleDisease.CURSE);
                remove(c, slot);
                return;
            } else if (ItemConstants.isTownScroll(itemId)) {
                int banMap = chr.getMapId();
                int banSp = chr.getMap().findClosestPlayerSpawnpoint(chr.getPosition()).getId();
                long banTime = currentServerTime();
                
                if (ii.getItemEffect(toUse.getItemId()).applyTo(chr)) {
                    if(ServerConstants.USE_BANISHABLE_TOWN_SCROLL) {
                        chr.setBanishPlayerData(banMap, banSp, banTime);
                    }
                    
                    remove(c, slot);
                }
                return;
            } else if (ItemConstants.isAntibanishScroll(itemId)) {
                if (ii.getItemEffect(toUse.getItemId()).applyTo(chr)) {
                    remove(c, slot);
                } else {
                    chr.dropMessage(5, "You cannot recover from a banish state at the moment.");
                }
                return;
            }
            
            remove(c, slot);
            
            if(toUse.getItemId() != 2022153) {
                ii.getItemEffect(toUse.getItemId()).applyTo(chr);
            } else {
                MapleStatEffect mse = ii.getItemEffect(toUse.getItemId());
                for(MapleCharacter player : chr.getMap().getCharacters()) {
                    mse.applyTo(player);
                }
            }
        }
    }

    private void remove(MapleClient c, short slot) {
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        c.announce(MaplePacketCreator.enableActions());
    }
}
