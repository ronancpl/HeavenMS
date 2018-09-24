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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

import tools.Pair;
import client.MapleCharacter;
import client.MapleClient;
import constants.ItemConstants;
import server.MapleItemInformationProvider;
import client.inventory.manipulator.MapleInventoryManipulator;
import tools.FilePrinter;
import net.server.audit.locks.MonitoredLockType;

/**
 *
 * @author Matze, Ronan
 */
public class MapleInventory implements Iterable<Item> {
    protected MapleCharacter owner;
    protected Map<Short, Item> inventory = new LinkedHashMap<>();
    protected byte slotLimit;
    protected MapleInventoryType type;
    protected boolean checked = false;
    protected Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.INVENTORY, true);
    
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
        lock.lock();
        try {
            return slotLimit;
        } finally {
            lock.unlock();
        }
    }

    public void setSlotLimit(int newLimit) {
        lock.lock();
        try {
            slotLimit = (byte) newLimit;
        } finally {
            lock.unlock();
        }
    }

    public Collection<Item> list() {
        lock.lock();
        try {
            return Collections.unmodifiableCollection(inventory.values());
        } finally {
            lock.unlock();
        }
    }
    
    public Item findById(int itemId) {
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    public Item findByName(String name) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Item item : list()) {
            String itemName = ii.getName(item.getItemId());
            if(itemName == null) {
                FilePrinter.printError(FilePrinter.EXCEPTION, "[CRITICAL] Item "  + item.getItemId() + " has no name.");
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
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                qty += item.getQuantity();
            }
        }
        return qty;
    }
    
    public int countNotOwnedById(int itemId) {
        int qty = 0;
        for (Item item : list()) {
            if (item.getItemId() == itemId && item.getOwner().equals("")) {
                qty += item.getQuantity();
            }
        }
        return qty;
    }
    
    public int freeSlotCountById(int itemId, int required) {
        List<Item> itemList = listById(itemId);
        int openSlot = 0;
        
        if(!ItemConstants.isRechargeable(itemId)) {
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
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }
        
        if (ret.size() > 1) {
            Collections.sort(ret, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    return i1.getPosition() - i2.getPosition();
                }
            });
        }
        
        return ret;
    }
    
    public List<Item> linkedListById(int itemId) {
        List<Item> ret = new LinkedList<>();
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }
        
        if (ret.size() > 1) {
            Collections.sort(ret, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    return i1.getPosition() - i2.getPosition();
                }
            });
        }
        
        return ret;
    }

    public short addItem(Item item) {
        short slotId = addSlot(item);
        if (slotId == -1) {
            return -1;
        }
        item.setPosition(slotId);
        return slotId;
    }

    public void addItemFromDB(Item item) {
        if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
            return;
        }
        addSlotFromDB(item.getPosition(), item);
    }

    private static boolean isSameOwner(Item source, Item target) {
        return source.getOwner().equals(target.getOwner());
    }
    
    public void move(short sSlot, short dSlot, short slotMax) {
        lock.lock();
        try {
            Item source = (Item) inventory.get(sSlot);
            Item target = (Item) inventory.get(dSlot);
            if (source == null) {
                return;
            }
            if (target == null) {
                source.setPosition(dSlot);
                inventory.put(dSlot, source);
                inventory.remove(sSlot);
            } else if (target.getItemId() == source.getItemId() && !ItemConstants.isRechargeable(source.getItemId()) && isSameOwner(source, target)) {
                if (type.getType() == MapleInventoryType.EQUIP.getType() || type.getType() == MapleInventoryType.CASH.getType()) {
                    swap(target, source);
                } else if (source.getQuantity() + target.getQuantity() > slotMax) {
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
        } finally {
            lock.unlock();
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
        lock.lock();
        try {
            return inventory.get(slot);
        } finally {
            lock.unlock();
        }
    }

    public void removeItem(short slot) {
        removeItem(slot, (short) 1, false);
    }

    public void removeItem(short slot, short quantity, boolean allowZero) {
        Item item = getItem(slot);
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

    protected short addSlot(Item item) {
        if(item == null) {
            return -1;
        }
        
        short slotId;
        lock.lock();
        try {
            slotId = getNextFreeSlot();
            if (slotId < 0) {
                return -1;
            }
            
            inventory.put(slotId, item);
        } finally {
            lock.unlock();
        }
        
        if (ItemConstants.isRateCoupon(item.getItemId())) {
            owner.updateCouponRates();
        }

        return slotId;
    }
    
    protected void addSlotFromDB(short slot, Item item) {
        lock.lock();
        try {
            inventory.put(slot, item);
        } finally {
            lock.unlock();
        }
        
        if (ItemConstants.isRateCoupon(item.getItemId())) {
            owner.updateCouponRates();
        }
    }
    
    public void removeSlot(short slot) {
        Item item;
        lock.lock();
        try {
            item = inventory.remove(slot);
        } finally {
            lock.unlock();
        }
        
        if (item != null && ItemConstants.isRateCoupon(item.getItemId())) {
            owner.updateCouponRates();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return inventory.size() >= slotLimit;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull(int margin) {
        lock.lock();
        try {
            //System.out.print("(" + inventory.size() + " " + margin + " <> " + slotLimit + ")");
            return inventory.size() + margin >= slotLimit;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isFullAfterSomeItems(int margin, int used) {
        lock.lock();
        try {
            //System.out.print("(" + inventory.size() + " " + margin + " <> " + slotLimit + " -" + used + ")");
            return inventory.size() + margin >= slotLimit - used;
        } finally {
            lock.unlock();
        }
    }

    public short getNextFreeSlot() {
        if (isFull()) {
            return -1;
        }
        
        lock.lock();
        try {
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    return i;
                }
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public short getNumFreeSlot() {
	if (isFull()) {
	    return 0;
	}
        
        lock.lock();
        try {
            short free = 0;
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    free++;
                }
            }
            return free;
        } finally {
            lock.unlock();
        }
    }
    
    public static boolean checkSpot(MapleCharacter chr, Item item) {
        return !chr.getInventory(item.getInventoryType()).isFull();
    }
    
    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        return checkSpots(chr, items, false);
    }
    
    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, boolean useProofInv) {
        int invTypesSize = MapleInventoryType.values().length;
        List<Integer> zeroedList = new ArrayList<>(invTypesSize);
        for(byte i = 0; i < invTypesSize; i++) zeroedList.add(0);
        
        return checkSpots(chr, items, zeroedList, useProofInv);
    }
    
    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, List<Integer> typesSlotsUsed, boolean useProofInv) {
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
                
                int result = MapleInventoryManipulator.checkSpaceProgressively(c, it.getKey(), it.getValue(), "", usedSlots, useProofInv);
                boolean hasSpace = ((result % 2) != 0);
                
                if(!hasSpace) return false;
                typesSlotsUsed.set(itemType, (result >> 1));
        }
        
    	return true;
    }
    
    private static long fnvHash32(final String k) {
        final int FNV_32_INIT = 0x811c9dc5;
        final int FNV_32_PRIME = 0x01000193;

        int rv = FNV_32_INIT;
        final int len = k.length();
        for(int i = 0; i < len; i++) {
            rv ^= k.charAt(i);
            rv *= FNV_32_PRIME;
        }
        
        return rv >= 0 ? rv : (2L * Integer.MAX_VALUE) + rv;
    }
    
    private static Long hashKey(Integer itemId, String owner) {
        return (itemId.longValue() << 32L) + fnvHash32(owner);
    }
    
    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        return checkSpotsAndOwnership(chr, items, false);
    }
    
    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, boolean useProofInv) {
        List<Integer> zeroedList = new ArrayList<>(5);
        for(byte i = 0; i < 5; i++) zeroedList.add(0);
        
        return checkSpotsAndOwnership(chr, items, zeroedList, useProofInv);
    }
    
    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, List<Integer> typesSlotsUsed, boolean useProofInv) {
        //assumption: no "UNDEFINED" or "EQUIPPED" items shall be tested here, all counts are >= 0 and item list to be checked is a legal one.
        
        Map<Long, Short> rcvItems = new LinkedHashMap<>();
        Map<Long, Byte> rcvTypes = new LinkedHashMap<>();
        Map<Long, String> rcvOwners = new LinkedHashMap<>();
        
        for (Pair<Item, MapleInventoryType> item : items) {
                Long itemHash = hashKey(item.left.getItemId(), item.left.getOwner());
                Short qty = rcvItems.get(itemHash);
            
    		if(qty == null) {
                        rcvItems.put(itemHash, item.left.getQuantity());
                        rcvTypes.put(itemHash, item.right.getType());
                        rcvOwners.put(itemHash, item.left.getOwner());
                } else {
                        rcvItems.put(itemHash, (short)(qty + item.left.getQuantity()));
                }
    	}
        
        MapleClient c = chr.getClient();
        for(Entry<Long, Short> it: rcvItems.entrySet()) {
                int itemType = rcvTypes.get(it.getKey()) - 1;
                int usedSlots = typesSlotsUsed.get(itemType);
                
                Long itemId = it.getKey() >> 32L;
                
                //System.out.print("inserting " + itemId.intValue() + " with type " + itemType + " qty " + it.getValue() + " owner '" + rcvOwners.get(it.getKey()) + "' current usedSlots:");
                //for(Integer i : typesSlotsUsed) System.out.print(" " + i);
                int result = MapleInventoryManipulator.checkSpaceProgressively(c, itemId.intValue(), it.getValue(), rcvOwners.get(it.getKey()), usedSlots, useProofInv);
                boolean hasSpace = ((result % 2) != 0);
                //System.out.print(" -> hasSpace: " + hasSpace + " RESULT : " + result + "\n");
                
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
        return Collections.unmodifiableCollection(list()).iterator();
    }

    public Collection<MapleInventory> allInventories() {
	return Collections.singletonList(this);
    }

    public Item findByCashId(int cashId) {
        boolean isRing = false;
        Equip equip = null;
	for (Item item : list()) {
            if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId)
                 return item;
            }

	return null;
    }

    public boolean checked() {
        lock.lock();
        try {
            return checked;
        } finally {
            lock.unlock();
        }
    }

    public void checked(boolean yes) {
        lock.lock();
        try {
            checked = yes;
        } finally {
            lock.unlock();
        }
    }
    
    public void lockInventory() {
        lock.lock();
    }
    
    public void unlockInventory() {
        lock.unlock();
    }
}