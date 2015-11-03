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
package server.quest.actions;

import client.MapleCharacter;
import client.MapleJob;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

/**
 *
 * @author Tyler (Twdtwd)
 */
public class ItemAction extends MapleQuestAction {
	Map<Integer, ItemData> items = new HashMap<>();
	
	public ItemAction(MapleQuest quest, MapleData data) {
		super(MapleQuestActionType.ITEM, quest);
		processData(data);
	}
	
	
	@Override
	public void processData(MapleData data) {
		for (MapleData iEntry : data.getChildren()) {
			int id = MapleDataTool.getInt(iEntry.getChildByPath("id"));
			int count = MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
			
			Integer prop = null;
			MapleData propData = iEntry.getChildByPath("prop");
			if(propData != null)
				prop = MapleDataTool.getInt(propData);
			
			int gender = 2;
			if (iEntry.getChildByPath("gender") != null)
				gender = MapleDataTool.getInt(iEntry.getChildByPath("gender"));
			
			int job = -1;
			if (iEntry.getChildByPath("job") != null)
				job = MapleDataTool.getInt(iEntry.getChildByPath("job"));
			
			items.put(id, new ItemData(id, count, prop, job, gender));
		}
	}
	
	@Override
	public void run(MapleCharacter chr, Integer extSelection) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<Integer, Integer> props = new HashMap<>();
		for(ItemData item : items.values()) {
			if(item.getProp() != null && item.getProp() != -1 && canGetItem(item, chr)) {
				for (int i = 0; i < item.getProp(); i++) {
					props.put(props.size(), item.getId());
				}
			}
		}
		int selection = 0;
		int extNum = 0;
		if (props.size() > 0) {
			selection = props.get(Randomizer.nextInt(props.size()));
		}
		for (ItemData iEntry : items.values()) {
			if (!canGetItem(iEntry, chr)) {
				continue;
			}
			if(iEntry.getProp() != null) {
				if(iEntry.getProp() == -1) {
					if(extSelection != extNum++)
						continue;
				} else if(iEntry.getId() != selection)
					continue;
			}
			
			if(iEntry.getCount() < 0) { // Remove Items
				MapleInventoryType type = ii.getInventoryType(iEntry.getId());
				int quantity = iEntry.getCount() * -1; // Invert
				if(type.equals(MapleInventoryType.EQUIP)) {
					if(chr.getInventory(type).countById(iEntry.getId()) < quantity) {
						// Not enough in the equip inventoty, so check Equipped...
						if(chr.getInventory(MapleInventoryType.EQUIPPED).countById(iEntry.getId()) > quantity) {
							// Found it equipped, so change the type to equipped.
							type = MapleInventoryType.EQUIPPED;
						}
					}
				}
				MapleInventoryManipulator.removeById(chr.getClient(), type, iEntry.getId(), quantity, true, false);
				chr.announce(MaplePacketCreator.getShowItemGain(iEntry.getId(), (short) iEntry.getCount(), true));
			} else {
				if (chr.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(iEntry.getId())).getNextFreeSlot() > -1) {
					MapleInventoryManipulator.addById(chr.getClient(), iEntry.getId(), (short) iEntry.getCount());
					chr.announce(MaplePacketCreator.getShowItemGain(iEntry.getId(), (short) iEntry.getCount(), true));
				} else {
					chr.dropMessage(1, "Inventory Full");
				}
			}
		}
	}
	
	@Override
	public boolean check(MapleCharacter chr, Integer extSelection) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        EnumMap<MapleInventoryType, Integer> props = new EnumMap<>(MapleInventoryType.class);
		List<Pair<Item, MapleInventoryType>> itemList = new ArrayList<>();
		for(ItemData item : items.values()) {
			if (!canGetItem(item, chr)) {
				continue;
			}
			MapleInventoryType type = ii.getInventoryType(item.getId());
			if(item.getProp() != null) {
				if(!props.containsKey(type)) {
					props.put(type, item.getId());
				}
				continue;
			}
			
			if(item.getCount() > 0) {
				// Make sure they can hold the item.
				Item toItem = new Item(item.getId(), (short) 0, (short) item.getCount());
				itemList.add(new Pair<>(toItem, type));
			} else {
				// Make sure they actually have the item.
				int quantity = item.getCount() * -1;
				if(chr.getInventory(type).countById(item.getId()) < quantity) {
					if(type.equals(MapleInventoryType.EQUIP) && chr.getInventory(MapleInventoryType.EQUIPPED).countById(item.getId()) > quantity)
						continue;
					return false;
				}
			}
		}
		for(Integer itemID : props.values()) {
			MapleInventoryType type = ii.getInventoryType(itemID);
			Item toItem = new Item(itemID, (short) 0, (short) 1);
			itemList.add(new Pair<>(toItem, type));
		}
		
		if (!MapleInventory.checkSpots(chr, itemList)) {
			chr.dropMessage(1, "Please check if you have enough space in your inventory.");
			return false;
		}
		return true;
	}
	
	private boolean canGetItem(ItemData item, MapleCharacter chr) {
		if (item.getGender() != 2 && item.getGender() != chr.getGender()) {
			return false;
		}
		
		if(item.getJob() != -1) {
			if (item.getJob() != chr.getJob().getId()) {
					return false;
			} else if (MapleJob.getBy5ByteEncoding(item.getJob()).getId() / 100 != chr.getJob().getId() / 100) {
				return false;
			}
		}
        return true;
    }
	
	private class ItemData {
		private final int id, count, job, gender;
		private final Integer prop;
		
		public ItemData(int id, int count, Integer prop, int job, int gender) {
			this.id = id;
			this.count = count;
			this.prop = prop;
			this.job = job;
			this.gender = gender;
		}
		
		public int getId() {
			return id;
		}
		
		public int getCount() {
			return count;
		}
		
		public Integer getProp() {
			return prop;
		}
		
		public int getJob() {
			return job;
		}
		
		public int getGender() {
			return gender;
		}
	}
} 
