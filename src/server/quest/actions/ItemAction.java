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
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataTool;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

/**
 *
 * @author Tyler (Twdtwd)
 * @author Ronan
 */
public class ItemAction extends MapleQuestAction {
	List<ItemData> items = new ArrayList<>();
	
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
			
			items.add(new ItemData(Integer.parseInt(iEntry.getName()), id, count, prop, job, gender));
		}
                
                Collections.sort(items, new Comparator<ItemData>()
                {
                    @Override
                    public int compare( ItemData o1, ItemData o2 )
                    {
                        return o1.map - o2.map;
                    }
                });
	}
	
	@Override
	public void run(MapleCharacter chr, Integer extSelection) {
                List<Pair<Integer, Integer>> takeItem = new LinkedList<>();
                List<Pair<Integer, Integer>> giveItem = new LinkedList<>();
            
                int props = 0, rndProps = 0, accProps = 0;
		for(ItemData item : items) {
			if(item.getProp() != null && item.getProp() != -1 && canGetItem(item, chr)) {
                                props += item.getProp();
			}
		}
		
		int extNum = 0;
		if (props > 0) {
			rndProps = Randomizer.nextInt(props);
		}
		for (ItemData iEntry : items) {
			if (!canGetItem(iEntry, chr)) {
				continue;
			}
			if(iEntry.getProp() != null) {
				if(iEntry.getProp() == -1) {
					if(extSelection != extNum++)
						continue;
				} else {
                                        accProps += iEntry.getProp();
                                        
                                        if(accProps <= rndProps) {
                                                continue;
                                        } else {
                                                accProps = Integer.MIN_VALUE;
                                        }
                                }
			}
			
			if(iEntry.getCount() < 0) { // Remove Item
				takeItem.add(new Pair<>(iEntry.getId(), iEntry.getCount()));
			} else {                    // Give Item
                                giveItem.add(new Pair<>(iEntry.getId(), iEntry.getCount()));
			}
		}
                
                // must take all needed items before giving others
                
                for(Pair<Integer, Integer> iPair: takeItem) {
                        MapleInventoryType type = ItemConstants.getInventoryType(iPair.getLeft());
                        int quantity = iPair.getRight() * -1; // Invert
                        if(type.equals(MapleInventoryType.EQUIP)) {
                                if(chr.getInventory(type).countById(iPair.getLeft()) < quantity) {
                                        // Not enough in the equip inventoty, so check Equipped...
                                        if(chr.getInventory(MapleInventoryType.EQUIPPED).countById(iPair.getLeft()) > quantity) {
                                                // Found it equipped, so change the type to equipped.
                                                type = MapleInventoryType.EQUIPPED;
                                        }
                                }
                        }

                        MapleInventoryManipulator.removeById(chr.getClient(), type, iPair.getLeft(), quantity, true, false);
                        chr.announce(MaplePacketCreator.getShowItemGain(iPair.getLeft(), (short) iPair.getRight().shortValue(), true));
                }
                
                for(Pair<Integer, Integer> iPair: giveItem) {
                        MapleInventoryManipulator.addById(chr.getClient(), iPair.getLeft(), (short) iPair.getRight().shortValue(), "", -1);
                        chr.announce(MaplePacketCreator.getShowItemGain(iPair.getLeft(), (short) iPair.getRight().shortValue(), true));
                }
	}
	
	@Override
	public boolean check(MapleCharacter chr, Integer extSelection) {
		List<Pair<Item, MapleInventoryType>> gainList = new LinkedList<>();
                List<Pair<Item, MapleInventoryType>> selectList = new LinkedList<>();
                List<Pair<Item, MapleInventoryType>> randomList = new LinkedList<>();
                
                List<Integer> allSlotUsed = new ArrayList(5);
                for(byte i = 0; i < 5; i++) allSlotUsed.add(0);
                
                for(ItemData item : items) {
                        if (!canGetItem(item, chr)) {
				continue;
			}
                        
			MapleInventoryType type = ItemConstants.getInventoryType(item.getId());
			if(item.getProp() != null) {
                                Item toItem = new Item(item.getId(), (short) 0, (short) item.getCount());
                            
                                if(item.getProp() < 0) {
                                        selectList.add(new Pair<>(toItem, type));
                                } else {
                                        randomList.add(new Pair<>(toItem, type));
                                }
				
			} else {
                                if(item.getCount() > 0) {
                                        // Make sure they can hold the item.
                                        Item toItem = new Item(item.getId(), (short) 0, (short) item.getCount());
                                        gainList.add(new Pair<>(toItem, type));
                                } else {
                                        // Make sure they actually have the item.
                                        int quantity = item.getCount() * -1;
                                        
                                        int freeSlotCount = chr.getInventory(type).freeSlotCountById(item.getId(), quantity);
                                        if(freeSlotCount == -1) {
                                                if(type.equals(MapleInventoryType.EQUIP) && chr.getInventory(MapleInventoryType.EQUIPPED).countById(item.getId()) > quantity)
                                                        continue;
                                                
                                                chr.dropMessage(1, "Please check if you have enough items in your inventory.");
                                                return false;
                                        } else {
                                                int idx = type.getType() - 1;   // more slots available from the given items!
                                                allSlotUsed.set(idx, allSlotUsed.get(idx) - freeSlotCount);
                                        }
                                }
                        }
		}
                
                if(!randomList.isEmpty()) {
                        int result;
                        MapleClient c = chr.getClient();
                        
                        List<Integer> rndUsed = new ArrayList(5);
                        for(byte i = 0; i < 5; i++) rndUsed.add(allSlotUsed.get(i));
                    
                        for(Pair<Item, MapleInventoryType> it: randomList) {
                                int idx = it.getRight().getType() - 1;
                            
                                result = MapleInventoryManipulator.checkSpaceProgressively(c, it.getLeft().getItemId(), it.getLeft().getQuantity(), "", rndUsed.get(idx), false);
                                if(result % 2 == 0) {
                                    chr.dropMessage(1, "Please check if you have enough space in your inventory.");
                                    return false;
                                }
                                
                                allSlotUsed.set(idx, Math.max(allSlotUsed.get(idx), result >> 1));
                        }
                }
                
                if(!selectList.isEmpty()) {
                        Pair<Item, MapleInventoryType> selected = selectList.get(extSelection);
                        gainList.add(selected);
                }
                
		if (!MapleInventory.checkSpots(chr, gainList, allSlotUsed, false)) {
			chr.dropMessage(1, "Please check if you have enough space in your inventory.");
			return false;
		}
		return true;
	}
	
	private boolean canGetItem(ItemData item, MapleCharacter chr) {
		if (item.getGender() != 2 && item.getGender() != chr.getGender()) {
			return false;
		}
                
                if (item.job > 0) {
                    final List<Integer> code = getJobBy5ByteEncoding(item.getJob());
                    boolean jobFound = false;
                    for (int codec : code) {
                        if (codec / 100 == chr.getJob().getId() / 100) {
                            jobFound = true;
                            break;
                        }
                    }
                    return jobFound;
                }
        return true;
    }
	
	private class ItemData {
		private final int map, id, count, job, gender;
		private final Integer prop;
		
		public ItemData(int map, int id, int count, Integer prop, int job, int gender) {
			this.map = map;
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
