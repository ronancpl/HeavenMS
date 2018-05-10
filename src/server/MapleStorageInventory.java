/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import constants.ItemConstants;
import constants.ServerConstants;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import java.util.ArrayList;

/**
 *
 * @author RonanLana
 */

class PairedQuicksort {
    private int i = 0;
    private int j = 0;
    private final ArrayList<Integer> intersect;
    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    
    private void PartitionByItemId(int Esq, int Dir, ArrayList<Item> A) {
        Item x, w;

        i = Esq;
        j = Dir;
        
        x = A.get((i + j) / 2);
        do {
            while (x.getItemId() > A.get(i).getItemId()) i++;
            while (x.getItemId() < A.get(j).getItemId()) j--;
            
            if (i <= j) {
                w = A.get(i);
                A.set(i, A.get(j));
                A.set(j, w);

                i++;
                j--;
            }
        } while (i <= j);
    }
    
    private void PartitionByName(int Esq, int Dir, ArrayList<Item> A) {
        Item x, w;

        i = Esq;
        j = Dir;
        
        x = A.get((i + j) / 2);
        do {
            while (ii.getName(x.getItemId()).compareTo(ii.getName(A.get(i).getItemId())) > 0) i++;
            while (ii.getName(x.getItemId()).compareTo(ii.getName(A.get(j).getItemId())) < 0) j--;
            
            if (i <= j) {
                w = A.get(i);
                A.set(i, A.get(j));
                A.set(j, w);

                i++;
                j--;
            }
        } while (i <= j);
    }
    
    private void PartitionByQuantity(int Esq, int Dir, ArrayList<Item> A) {
        Item x, w;

        i = Esq;
        j = Dir;
        
        x = A.get((i + j) / 2);
        do {
            while (x.getQuantity() > A.get(i).getQuantity()) i++;
            while (x.getQuantity() < A.get(j).getQuantity()) j--;
            
            if (i <= j) {
                w = A.get(i);
                A.set(i, A.get(j));
                A.set(j, w);

                i++;
                j--;
            }
        } while (i <= j);
    }
    
    private void PartitionByLevel(int Esq, int Dir, ArrayList<Item> A) {
        Equip x, w, eqpI, eqpJ;

        i = Esq;
        j = Dir;
        
        x = (Equip)(A.get((i + j) / 2));
        
        do {
            eqpI = (Equip)A.get(i);
            eqpJ = (Equip)A.get(j);
            
            while (x.getLevel() > eqpI.getLevel()) i++;
            while (x.getLevel() < eqpJ.getLevel()) j--;
            
            if (i <= j) {
                w = (Equip)A.get(i);
                A.set(i, A.get(j));
                A.set(j, (Item)w);

                i++;
                j--;
            }
        } while (i <= j);
    }

    void MapleQuicksort(int Esq, int Dir, ArrayList<Item> A, int sort) {
        switch(sort) {
            case 3:
                PartitionByLevel(Esq, Dir, A);
                break;
            
            case 2:
                PartitionByName(Esq, Dir, A);
                break;
                
            case 1:
                PartitionByQuantity(Esq, Dir, A);
                break;
                    
            default:
                PartitionByItemId(Esq, Dir, A);
        }
        
        
        if (Esq < j) MapleQuicksort(Esq, j, A, sort);
        if (i < Dir) MapleQuicksort(i, Dir, A, sort);
    }
    
    public PairedQuicksort(ArrayList<Item> A, int primarySort, int secondarySort) {
        intersect = new ArrayList<>();
        
        if(A.size() > 0) MapleQuicksort(0, A.size() - 1, A, primarySort);
        
        intersect.add(0);
        for(int ind = 1; ind < A.size(); ind++) {
            if(A.get(ind - 1).getItemId() != A.get(ind).getItemId()) {
                intersect.add(ind);
            }
        }
        intersect.add(A.size());
        
        for(int ind = 0; ind < intersect.size() - 1; ind++) {
            if(intersect.get(ind + 1) > intersect.get(ind)) MapleQuicksort(intersect.get(ind), intersect.get(ind + 1) - 1, A, secondarySort);
        }
    }
}

public class MapleStorageInventory {
    private MapleClient c;
    private Map<Short, Item> inventory = new LinkedHashMap<>();
    private byte slotLimit;
    
    public MapleStorageInventory(MapleClient c, List<Item> toSort) {
        this.inventory = new LinkedHashMap<>();
        this.slotLimit = (byte)toSort.size();
        this.c = c;
        
        for(Item item : toSort) {
            this.addItem(item);
        }
    }

    private byte getSlotLimit() {
        return slotLimit;
    }
    
    private Collection<Item> list() {
        return Collections.unmodifiableCollection(inventory.values());
    }

    private short addItem(Item item) {
        short slotId = getNextFreeSlot();
        if (slotId < 0 || item == null) {
            return -1;
        }
        addSlot(slotId, item);
        item.setPosition(slotId);
        return slotId;
    }

    private static boolean isEquipOrCash(Item item) {
        int type = item.getItemId() / 1000000;
        return type == 1 || type == 5;
    }
    
    private static boolean isSameOwner(Item source, Item target) {
        return source.getOwner().equals(target.getOwner());
    }
    
    private void move(short sSlot, short dSlot, short slotMax) {
        Item source = (Item) inventory.get(sSlot);
        Item target = (Item) inventory.get(dSlot);
        if (source == null) {
            return;
        }
        if (target == null) {
            source.setPosition(dSlot);
            inventory.put(dSlot, source);
            inventory.remove(sSlot);
        } else if (target.getItemId() == source.getItemId() && !ItemConstants.isRechargeable(source.getItemId()) && !MapleItemInformationProvider.getInstance().isPickupRestricted(source.getItemId()) && isSameOwner(source, target)) {
            if (isEquipOrCash(source)) {
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
    }
    
    private void moveItem(short src, short dst) {
        if (src < 0 || dst < 0) {
            return;
        }
        if(dst > this.getSlotLimit()) {
            return;
        }
        
        Item source = this.getItem(src);
        if (source == null) {
            return;
        }
        short slotMax = MapleItemInformationProvider.getInstance().getSlotMax(c, source.getItemId());
        this.move(src, dst, slotMax);
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

    private Item getItem(short slot) {
        return inventory.get(slot);
    }

    private void addSlot(short slot, Item item) {
        inventory.put(slot, item);
    }
    
    private void removeSlot(short slot) {
        inventory.remove(slot);
    }

    private boolean isFull() {
        return inventory.size() >= slotLimit;
    }

    private short getNextFreeSlot() {
        if (isFull()) {
            return -1;
        }
        
        for (short i = 1; i <= slotLimit; i++) {
            if (!inventory.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    public void mergeItems() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item srcItem, dstItem;

        for(short dst = 1; dst <= this.getSlotLimit(); dst++) {
            dstItem = this.getItem(dst);
            if(dstItem == null) continue;

            for(short src = (short)(dst + 1); src <= this.getSlotLimit(); src++) {
                srcItem = this.getItem(src);
                if(srcItem == null) continue;

                if(dstItem.getItemId() != srcItem.getItemId()) continue;
                if(dstItem.getQuantity() == ii.getSlotMax(c, this.getItem(dst).getItemId())) break;

                moveItem(src, dst);
            }
        }
                
        boolean sorted = false;

        while (!sorted) {
            short freeSlot = this.getNextFreeSlot();

            if (freeSlot != -1) {
                short itemSlot = -1;
                for (short i = (short) (freeSlot + 1); i <= this.getSlotLimit(); i = (short) (i + 1)) {
                    if (this.getItem(i) != null) {
                        itemSlot = i;
                        break;
                    }
                }
                if (itemSlot > 0) {
                    moveItem(itemSlot, freeSlot);
                } else {
                    sorted = true;
                }
            } else {
                sorted = true;
            }
        }
    }
    
    public List<Item> sortItems() {
        ArrayList<Item> itemarray = new ArrayList<>();
        
        for (short i = 1; i <= this.getSlotLimit(); i++) {
            Item item = this.getItem(i);
            if (item != null) {
            	itemarray.add((Item) item.copy());
            }
        }
        
        for (Item item : itemarray) {
            this.removeSlot(item.getPosition());
        }
        
        int invTypeCriteria = 1;
        int sortCriteria = (ServerConstants.USE_ITEM_SORT_BY_NAME == true) ? 2 : 0;
        PairedQuicksort pq = new PairedQuicksort(itemarray, sortCriteria, invTypeCriteria);
        
        inventory.clear();
        return itemarray;
    }
}
