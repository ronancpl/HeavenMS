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
package server.quest.requirements;

import java.util.HashMap;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;

/**
 *
 * @author Tyler (Twdtwd)
 */
public class ItemRequirement extends MapleQuestRequirement {
	Map<Integer, Integer> items = new HashMap<>();
	
	
	public ItemRequirement(MapleQuest quest, MapleData data) {
		super(MapleQuestRequirementType.ITEM);
		processData(data);
	}
	
	@Override
	public void processData(MapleData data) {
		for (MapleData itemEntry : data.getChildren()) {
			int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
			int count = MapleDataTool.getInt(itemEntry.getChildByPath("count"), 0);
			
			items.put(itemId, count);
		}
	}
	
	
	@Override
	public boolean check(MapleCharacter chr, Integer npcid) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		for(Integer itemId : items.keySet()) {
			int countNeeded = items.get(itemId);
			int count = 0;
			
			MapleInventoryType iType = ItemConstants.getInventoryType(itemId);
			
			if (iType.equals(MapleInventoryType.UNDEFINED)) {
				return false;
			}
			for (Item item : chr.getInventory(iType).listById(itemId)) {
				count += item.getQuantity();
			}
			//Weird stuff, nexon made some quests only available when wearing gm clothes. This enables us to accept it ><
			if (iType.equals(MapleInventoryType.EQUIP) && !ItemConstants.isMedal(itemId)) {
                                if(chr.isGM()) {
                                        for (Item item : chr.getInventory(MapleInventoryType.EQUIPPED).listById(itemId)) {
                                                count += item.getQuantity();
                                        }
                                } else {
                                        if(count < countNeeded) {
                                                if(chr.getInventory(MapleInventoryType.EQUIPPED).countById(itemId) + count >= countNeeded) {
                                                        chr.dropMessage(5, "Unequip the required " + ii.getName(itemId) + " before trying this quest operation.");
                                                        return false;
                                                }
                                        }
                                }
			}
			
			if(count < countNeeded || countNeeded <= 0 && count > 0) {
				return false;
			}
		}
		return true;
	}
	
	public int getItemAmountNeeded(int itemid) {
		if(items.containsKey(itemid)) {
			return items.get(itemid);
		}
		
		return 0;
	}
}
