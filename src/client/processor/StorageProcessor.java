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
package client.processor;

import client.MapleClient;
import client.MapleCharacter;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.ItemConstants;
import constants.ServerConstants;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class StorageProcessor {
    
        public static void storageAction(SeekableLittleEndianAccessor slea, MapleClient c) {
                MapleCharacter chr = c.getPlayer();
                MapleStorage storage = chr.getStorage();
                byte mode = slea.readByte();

                if (chr.getLevel() < 15){
                        chr.dropMessage(1, "You may only use the storage once you have reached level 15.");
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                }
                
                if (c.tryacquireClient()) {
                        try {
                                if (mode == 4) { // take out
                                        byte type = slea.readByte();
                                        byte slot = slea.readByte();
                                        if (slot < 0 || slot > storage.getSlots()) { // removal starts at zero
                                                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
                                                FilePrinter.print(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to work with storage slot " + slot + "\r\n");
                                                c.disconnect(true, false);
                                                return;
                                        }
                                        slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
                                        Item item = storage.getItem(slot);
                                        if (item != null) {
                                                if (MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId()) && chr.haveItemWithId(item.getItemId(), true)) {
                                                        c.announce(MaplePacketCreator.getStorageError((byte) 0x0C));
                                                        return;
                                                }

                                                int takeoutFee = storage.getTakeOutFee();
                                                if (chr.getMeso() < takeoutFee) {
                                                        c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
                                                        return;
                                                } else {
                                                        chr.gainMeso(-takeoutFee, false);
                                                }

                                                if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {                
                                                        item = storage.takeOut(slot);//actually the same but idc
                                                        String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
                                                        FilePrinter.print(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " took out " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")\r\n");
                                                        chr.setUsedStorage();
                                                        MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
                                                        MapleInventoryManipulator.addFromDrop(c, item, false);
                                                        storage.sendTakenOut(c, item.getInventoryType());
                                                } else {
                                                        c.announce(MaplePacketCreator.getStorageError((byte) 0x0A));
                                                }
                                        }
                                } else if (mode == 5) { // store
                                        short slot = slea.readShort();
                                        int itemId = slea.readInt();
                                        short quantity = slea.readShort();
                                        MapleInventoryType slotType = ItemConstants.getInventoryType(itemId);
                                        MapleInventory Inv = chr.getInventory(slotType);
                                        if (slot < 1 || slot > Inv.getSlotLimit()) { //player inv starts at one
                                                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
                                                FilePrinter.print(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to store item at slot " + slot + "\r\n");
                                                c.disconnect(true, false);
                                                return;
                                        }
                                        if (quantity < 1 || chr.getItemQuantity(itemId, false) < quantity) {
                                                c.announce(MaplePacketCreator.enableActions());
                                                return;
                                        }
                                        if (storage.isFull()) {
                                                c.announce(MaplePacketCreator.getStorageError((byte) 0x11));
                                                return;
                                        }

                                        int storeFee = storage.getStoreFee();
                                        if (chr.getMeso() < storeFee) {
                                                c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
                                        } else {
                                                MapleInventoryType invType = ItemConstants.getInventoryType(itemId);
                                                Item item = chr.getInventory(invType).getItem(slot).copy();
                                                if (item != null && item.getItemId() == itemId && (item.getQuantity() >= quantity || ItemConstants.isRechargeable(itemId))) {
                                                        if (ItemConstants.isRechargeable(itemId)) {
                                                                quantity = item.getQuantity();
                                                        }

                                                        chr.gainMeso(-storeFee, false, true, false);
                                                        MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
                                                        MapleInventoryManipulator.removeFromSlot(c, invType, slot, quantity, false);
                                                        item.setQuantity(quantity);
                                                        storage.store(item);
                                                        storage.sendStored(c, ItemConstants.getInventoryType(itemId));
                                                        String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
                                                        FilePrinter.print(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")\r\n");
                                                        chr.setUsedStorage();
                                                }
                                        }
                                } else if (mode == 6) { // arrange items
                                        if(ServerConstants.USE_STORAGE_ITEM_SORT) storage.arrangeItems(c);
                                        c.announce(MaplePacketCreator.enableActions());
                                } else if (mode == 7) { // meso
                                        int meso = slea.readInt();
                                        int storageMesos = storage.getMeso();
                                        int playerMesos = chr.getMeso();
                                        if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                                                if (meso < 0 && (storageMesos - meso) < 0) {
                                                        meso = Integer.MIN_VALUE + storageMesos;
                                                        if (meso < playerMesos) {
                                                                c.announce(MaplePacketCreator.enableActions());
                                                                return;
                                                        }
                                                } else if (meso > 0 && (playerMesos + meso) < 0) {
                                                        meso = Integer.MAX_VALUE - playerMesos;
                                                        if (meso > storageMesos) {
                                                                c.announce(MaplePacketCreator.enableActions());
                                                                return;
                                                        }
                                                }
                                                storage.setMeso(storageMesos - meso);
                                                chr.gainMeso(meso, false, true, false);
                                                FilePrinter.print(FilePrinter.STORAGE + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + (meso > 0 ? " took out " : " stored ") + Math.abs(meso) + " mesos\r\n");
                                                chr.setUsedStorage();
                                        } else {
                                                c.announce(MaplePacketCreator.enableActions());
                                                return;
                                        }
                                        storage.sendMeso(c);
                                } else if (mode == 8) {// close
                                        storage.close();
                                }
                        } finally {
                                c.releaseClient();
                        }
                }
        }
}
