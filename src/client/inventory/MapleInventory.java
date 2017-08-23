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
package client.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

import tools.Pair;
import client.MapleCharacter;
import client.MapleClient;
import constants.ItemConstants;
import server.MapleItemInformationProvider;
import server.MapleInventoryManipulator;

/**
 *
 * @author Matze, Ronan
 */
public class MapleInventory implements Iterable<Item> {
    private MapleCharacter owner;
    private Map<Short, Item> inventory = new LinkedHashMap<>();
    private byte slotLimit;
    private MapleInventoryType type;
    private boolean checked = false;
    
    public MapleInventory(MapleCharacter mc, MapleInventoryType type, byte slotLimit) {
        this.owner = mc;
        this.inventory = new LinkedHashMap<>();
        this.type = type;
        this.slotLimit = slotLimit;
    }

    public boolean isExtendableInventory() { // not sure about cash, basing this on the previous one.
        return !(type.equals(MapleInventoryType.UNDEFINED) || type.equals(MapleInventoryType.EQUIPPED) || type.equals(MapleInventoryType.CASH));
    }

    public boolean isEquipInventory() {
        return type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED);
    }

    public byte getSlotLimit() {
	return slotLimit;
    }

    public void setSlotLimit(int newLimit) {
	slotLimit = (byte) newLimit;
    }

    public Item findById(int itemId) {
        for (Item item : inventory.values()) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    public Item findByName(String name) {
        for (Item item : inventory.values()) {
            String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
            if(itemName == null) {
                System.out.println("[CRITICAL] Item "  + item.getItemId() + " has no name.");
                continue;
            }
            
            if (name.compareToIgnoreCase(itemName) == 0) {
                return item;
            }
        }
        return null;
    }

    public int countById(int itemId) {
        int qty = 0;
        for (Item item : inventory.values()) {
            if (item.getItemId() == itemId) {
                qty += item.getQuantity();
            }
        }
        return qty;
    }
    
    public int freeSlotCountById(int itemId, int required) {
        List<Item> itemList = listById(itemId);
        int openSlot = 0;
        
        if(!ItemConstants.isRechargable(itemId)) {
            for (Item item : itemList) {
                required -= item.getQuantity();

                if(required >= 0) {
                    openSlot++;
                    if(required == 0) return openSlot;
                } else {
                    return openSlot;
                }
            }
        } else {
            for (Item item : itemList) {
                required -= 1;

                if(required >= 0) {
                    openSlot++;
                    if(required == 0) return openSlot;
                } else {
                    return openSlot;
                }
            }
        }
        
        return -1;
    }

    public List<Item> listById(int itemId) {
        List<Item> ret = new ArrayList<>();
        for (Item item : inventory.values()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public Collection<Item> list() {
        return inventory.values();
    }

    public short addItem(Item item) {
        short slotId = getNextFreeSlot();
        if (slotId < 0 || item == null) {
            return -1;
        }
        addSlot(slotId, item);
        item.setPosition(slotId);
        return slotId;
    }

    public void addFromDB(Item item) {
        if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
            return;
        }
        addSlot(item.getPosition(), item);
    }

    public void move(short sSlot, short dSlot, short slotMax) {
        Item source = (Item) inventory.get(sSlot);
        Item target = (Item) inventory.get(dSlot);
        if (source == null) {
            return;
        }
        if (target == null) {
            source.setPosition(dSlot);
            inventory.put(dSlot, source);
            inventory.remove(sSlot);
        } else if (target.getItemId() == source.getItemId() && !ItemConstants.isRechargable(source.getItemId())) {
            if (type.getType() == MapleInventoryType.EQUIP.getType()) {
                swap(target, source);
            }
            if (source.getQuantity() + target.getQuantity() > slotMax) {
                short rest = (short) ((source.getQuantity() + target.getQuantity()) - slotMax);
                source.setQuantity(rest);
                target.setQuantity(slotMax);
            } else {
                target.setQuantity((short) (source.getQuantity() + target.getQuantity()));
                inventory.remove(sSlot);
            }
        } else {
            swap(target, source);
        }
    }

    private void swap(Item source, Item target) {
        inventory.remove(source.getPosition());
        inventory.remove(target.getPosition());
        short swapPos = source.getPosition();
        source.setPosition(target.getPosition());
        target.setPosition(swapPos);
        inventory.put(source.getPosition(), source);
        inventory.put(target.getPosition(), target);
    }

    public Item getItem(short slot) {
        return inventory.get(slot);
    }

    public void removeItem(short slot) {
        removeItem(slot, (short) 1, false);
    }

    public void removeItem(short slot, short quantity, boolean allowZero) {
        Item item = inventory.get(slot);
        if (item == null) {// TODO is it ok not to throw an exception here?
            return;
        }
        item.setQuantity((short) (item.getQuantity() - quantity));
        if (item.getQuantity() < 0) {
            item.setQuantity((short) 0);
        }
        if (item.getQuantity() == 0 && !allowZero) {
            removeSlot(slot);
        }
    }

    public void addSlot(short slot, Item item) {
        inventory.put(slot, item);
        
        if(ItemConstants.isRateCoupon(item.getItemId())) {
            owner.updateCouponRates();
        }
    }
    
    public void removeSlot(short slot) {
        Item item = inventory.remove(slot);
        
        if(item != null && ItemConstants.isRateCoupon(item.getItemId())) {
            owner.updateCouponRates();
        }
    }

    public boolean isFull() {
        return inventory.size() >= slotLimit;
    }

    public boolean isFull(int margin) {
        return inventory.size() + margin >= slotLimit;
    }
    
    public boolean isFullAfterSomeItems(int margin, int used) {
        return inventory.size() + margin >= slotLimit - used;
    }

    public short getNextFreeSlot() {
        if (isFull()) {
            return -1;
        }
        for (short i = 1; i <= slotLimit; i++) {
            if (!inventory.keySet().contains(i)) {
                return i;
            }
        }
        return -1;
    }

    public short getNumFreeSlot() {
	if (isFull()) {
	    return 0;
	}
	short free = 0;
	for (short i = 1; i <= slotLimit; i++) {
        if (!inventory.keySet().contains(i)) {
        	free++;
	    }
	}
	return free;
    }
    
    public static boolean checkSpot(MapleCharacter chr, Item item) {
    	if (chr.getInventory(MapleInventoryType.getByType(item.getType())).isFull()) return false;
    	return true;
    }
    
    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        List<Integer> zeroedList = new ArrayList<>(5);
        for(byte i = 0; i < 5; i++) zeroedList.add(0);
        
        return checkSpots(chr, items, zeroedList);
    }
    
    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, List<Integer> typesSlotsUsed) {
        // assumption: no "UNDEFINED" or "EQUIPPED" items shall be tested here, all counts are >= 0.
        
        Map<Integer, Short> rcvItems = new LinkedHashMap<>();
        Map<Integer, Byte> rcvTypes = new LinkedHashMap<>();
        
        for (Pair<Item, MapleInventoryType> item : items) {
                Integer itemId = item.left.getItemId();
                Short qty = rcvItems.get(itemId);
            
    		if(qty == null) {
                        rcvItems.put(itemId, item.left.getQuantity());
                        rcvTypes.put(itemId, item.right.getType());
                } else {
                        rcvItems.put(itemId, (short)(qty + item.left.getQuantity()));
                }
    	}
        
        MapleClient c = chr.getClient();
        for(Entry<Integer, Short> it: rcvItems.entrySet()) {
                int itemType = rcvTypes.get(it.getKey()) - 1;
                int usedSlots = typesSlotsUsed.get(itemType);
                
                int result = MapleInventoryManipulator.checkSpaceProgressively(c, it.getKey(), it.getValue(), "", usedSlots);
                boolean hasSpace = ((result % 2) != 0);
                
                if(!hasSpace) return false;
                typesSlotsUsed.set(itemType, (result >> 1));
        }
        
    	return true;
    }
    
    
    public MapleInventoryType getType() {
        return type;
    }

    @Override
    public Iterator<Item> iterator() {
        return Collections.unmodifiableCollection(inventory.values()).iterator();
    }

    public Collection<MapleInventory> allInventories() {
	return Collections.singletonList(this);
    }

    public Item findByCashId(int cashId) {
        boolean isRing = false;
        Equip equip = null;
	for (Item item : inventory.values()) {
            if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId)
                 return item;
            }

	return null;
    }

    public boolean checked() {
        return checked;
    }

    public void checked(boolean yes) {
        checked = yes;
    }
}