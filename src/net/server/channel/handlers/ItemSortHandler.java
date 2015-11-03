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

import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;

public final class ItemSortHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	MapleCharacter chr = c.getPlayer();
		chr.getAutobanManager().setTimestamp(2, slea.readInt(), 3);
		MapleInventoryType inventoryType = MapleInventoryType.getByType(slea.readByte());
		if (inventoryType.equals(MapleInventoryType.UNDEFINED) || c.getPlayer().getInventory(inventoryType).isFull()) {
		    c.getSession().write(MaplePacketCreator.enableActions());
		    return;
		}
		if(ServerConstants.USE_ITEM_SORT == false) {
			c.announce(MaplePacketCreator.enableActions());
			return;
		}
		
		MapleInventory inventory = c.getPlayer().getInventory(inventoryType);
		boolean sorted = false;
		
		while (!sorted) {
			short freeSlot = inventory.getNextFreeSlot();
		    if (freeSlot != -1) {
		        short itemSlot = -1;
		        for (short i = (short) (freeSlot + 1); i <= inventory.getSlotLimit(); i = (short) (i + 1)) {
		            if (inventory.getItem(i) != null) {
		                itemSlot = i;
		                break;
		            }
		        }
		        if (itemSlot > 0) {
		            MapleInventoryManipulator.move(c, inventoryType,  itemSlot, freeSlot);
		        } else {
		            sorted = true;
		        }
		    } else {
		        sorted = true;
		    }
		}
		c.getSession().write(MaplePacketCreator.finishedSort(inventoryType.getType()));
		c.getSession().write(MaplePacketCreator.enableActions());
    }
}