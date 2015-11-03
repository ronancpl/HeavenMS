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
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class StorageHandler extends AbstractMaplePacketHandler {
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		byte mode = slea.readByte();
		final MapleStorage storage = chr.getStorage();

		if (chr.getLevel() < 15){
			chr.message("You may only use this storage once you have reached level 15.");
			return;
		}
		if (mode == 4) { // take out
			byte type = slea.readByte();
			byte slot = slea.readByte();
			if (slot < 0 || slot > storage.getSlots()) { // removal starts at zero
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
				FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to work with storage slot " + slot + "\r\n");
				c.disconnect(true, false);
				return;
			}
			slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
			Item item = storage.getItem(slot);
			if (item != null) {
				if (MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId()) && chr.getItemQuantity(item.getItemId(), true) > 0) {
					c.announce(MaplePacketCreator.getStorageError((byte) 0x0C));    
					return;
				}
				if (chr.getMap().getId() == 910000000) {
					if (chr.getMeso() < 1000) {
						c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
						return;
					} else {
						chr.gainMeso(-1000, false);
					}
				}           
				if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {                
					item = storage.takeOut(slot);//actually the same but idc
					String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
					FilePrinter.printError(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " took out " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")\r\n");			
					if ((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA) {
						item.setFlag((byte) (item.getFlag() ^ ItemConstants.KARMA)); //items with scissors of karma used on them are reset once traded
					} else if (item.getType() == 2 && (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES){
						item.setFlag((byte) (item.getFlag() ^ ItemConstants.SPIKES));
					}
					MapleInventoryManipulator.addFromDrop(c, item, false);
					storage.sendTakenOut(c, ii.getInventoryType(item.getItemId()));
				} else {
					c.announce(MaplePacketCreator.getStorageError((byte) 0x0A));
				}
			}
		} else if (mode == 5) { // store
			short slot = slea.readShort();
			int itemId = slea.readInt();
			short quantity = slea.readShort();
			MapleInventoryType slotType = ii.getInventoryType(itemId);
			MapleInventory Inv = chr.getInventory(slotType);
			if (slot < 1 || slot > Inv.getSlotLimit()) { //player inv starts at one
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
				FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to store item at slot " + slot + "\r\n");
				c.disconnect(true, false);
				return;
			}
			if (quantity < 1 || chr.getItemQuantity(itemId, false) < quantity) {
				return;
			}
			if (storage.isFull()) {
				c.announce(MaplePacketCreator.getStorageError((byte) 0x11));
				return;
			}
			short meso = (short) (chr.getMap().getId() == 910000000 ? -500 : -100);
			if (chr.getMeso() < meso) {
				c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
			} else {
				MapleInventoryType type = ii.getInventoryType(itemId);
				Item item = chr.getInventory(type).getItem(slot).copy();
				if (item.getItemId() == itemId && (item.getQuantity() >= quantity || ItemConstants.isRechargable(itemId))) {
					if (ItemConstants.isRechargable(itemId)) {
						quantity = item.getQuantity();
					}
					chr.gainMeso(meso, false, true, false);
					MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
					item.setQuantity(quantity);
					storage.store(item);
					storage.sendStored(c, ii.getInventoryType(itemId));
					String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
					FilePrinter.printError(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")\r\n");	
				}
			}
		} else if (mode == 7) { // meso
			int meso = slea.readInt();
			int storageMesos = storage.getMeso();
			int playerMesos = chr.getMeso();
			if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
				if (meso < 0 && (storageMesos - meso) < 0) {
					meso = -2147483648 + storageMesos;
					if (meso < playerMesos) {
						return;
					}
				} else if (meso > 0 && (playerMesos + meso) < 0) {
					meso = 2147483647 - playerMesos;
					if (meso > storageMesos) {
						return;
					}
				}
				storage.setMeso(storageMesos - meso);
				chr.gainMeso(meso, false, true, false);
				FilePrinter.printError(FilePrinter.STORAGE + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + (meso > 0 ? " took out " : " stored ") + Math.abs(meso) + " mesos\r\n");					
			} else {
				return;
			}
			storage.sendMeso(c);
		} else if (mode == 8) {// close
			storage.close();
		}
	}
}