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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;

/**
 *
 * @author BubblesDev
 */
public final class ItemIdSortHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	MapleCharacter chr = c.getPlayer();
        chr.getAutobanManager().setTimestamp(4, slea.readInt(), 3);
        byte inventoryType = slea.readByte();
        
        if(!chr.isGM() || !ServerConstants.USE_ITEM_SORT) {
			c.announce(MaplePacketCreator.enableActions());
			return;
		}
		
		if (inventoryType < 1 || inventoryType > 5) {
            c.disconnect(false, false);
            return;
        }
		
        MapleInventory inventory = chr.getInventory(MapleInventoryType.getByType(inventoryType));
        ArrayList<Item> itemarray = new ArrayList<>();
        List<ModifyInventory> mods = new ArrayList<>();
        for (short i = 1; i <= inventory.getSlotLimit(); i++) {
            Item item = inventory.getItem(i);
            if (item != null) {
            	itemarray.add((Item) item.copy());
            }
        }
        
        Collections.sort(itemarray);
        for (Item item : itemarray) {
        	inventory.removeItem(item.getPosition());
        }
        
        for (Item item : itemarray) {
        	//short position = item.getPosition();
            inventory.addItem(item);
            if (inventory.getType().equals(MapleInventoryType.EQUIP)) {
	            mods.add(new ModifyInventory(3, item));
	            mods.add(new ModifyInventory(0, item.copy()));//to prevent crashes
	            //mods.add(new ModifyInventory(2, item.copy(), position));
            }
        }
        itemarray.clear();
        c.announce(MaplePacketCreator.modifyInventory(true, mods));
        c.announce(MaplePacketCreator.finishedSort2(inventoryType));
        c.announce(MaplePacketCreator.enableActions());
    }
}
