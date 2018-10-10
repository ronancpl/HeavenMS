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
import client.MapleRing;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import net.AbstractMaplePacketHandler;
import server.CashShop;
import server.CashShop.CashItem;
import server.CashShop.CashItemFactory;
import client.inventory.manipulator.MapleInventoryManipulator;
import constants.ServerConstants;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CashOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        CashShop cs = chr.getCashShop();
        
        if (!cs.isOpened()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        final int action = slea.readByte();
        if (action == 0x03 || action == 0x1E) {
            slea.readByte();
            final int useNX = slea.readInt();
            final int snCS = slea.readInt();
            CashItem cItem = CashItemFactory.getItem(snCS);
            if (!canBuy(cItem, cs.getCash(useNX))) {
                FilePrinter.printError(FilePrinter.ITEM, "Denied to sell cash item with SN " + cItem.getSN());
                c.enableCSActions();
                return;
            }
            
            if (action == 0x03) { // Item
                if (ItemConstants.isCashStore(cItem.getItemId()) && chr.getLevel() < 16) {
                    c.enableCSActions();
                    return;
                } else if (ItemConstants.isRateCoupon(cItem.getItemId()) && !ServerConstants.USE_SUPPLY_RATE_COUPONS) {
                    chr.dropMessage(1, "Rate coupons are currently unavailable to purchase.");
                    c.enableCSActions();
                    return;
                } else if (ItemConstants.isMapleLife(cItem.getItemId()) && chr.getLevel() < 30) {
                    c.enableCSActions();
                    return;
                }
                
                Item item = cItem.toItem();
                cs.addToInventory(item);
                c.announce(MaplePacketCreator.showBoughtCashItem(item, c.getAccID()));
            } else { // Package
                List<Item> cashPackage = CashItemFactory.getPackage(cItem.getItemId());
                for (Item item : cashPackage) {
                    cs.addToInventory(item);
                }
                c.announce(MaplePacketCreator.showBoughtCashPackage(cashPackage, c.getAccID()));
            }
            cs.gainCash(useNX, cItem, chr.getWorld());
            c.announce(MaplePacketCreator.showCash(chr));
        } else if (action == 0x04) {//TODO check for gender
            int birthday = slea.readInt();
            CashItem cItem = CashItemFactory.getItem(slea.readInt());
            Map<String, String> recipient = MapleCharacter.getCharacterFromDatabase(slea.readMapleAsciiString());
            String message = slea.readMapleAsciiString();
            if (!canBuy(cItem, cs.getCash(4)) || message.length() < 1 || message.length() > 73) {
                c.enableCSActions();
                return;
            }
            if (!checkBirthday(c, birthday)) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
                return;
            } else if (recipient == null) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xA9));
                return;
            } else if (recipient.get("accountid").equals(String.valueOf(c.getAccID()))) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xA8));
                return;
            }
            cs.gift(Integer.parseInt(recipient.get("id")), chr.getName(), message, cItem.getSN());
            c.announce(MaplePacketCreator.showGiftSucceed(recipient.get("name"), cItem));
            cs.gainCash(4, cItem, chr.getWorld());
            c.announce(MaplePacketCreator.showCash(chr));
            try {
                chr.sendNote(recipient.get("name"), chr.getName() + " has sent you a gift! Go check out the Cash Shop.", (byte) 0); //fame or not
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            MapleCharacter receiver = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient.get("name"));
            if (receiver != null) receiver.showNote();
        } else if (action == 0x05) { // Modify wish list
            cs.clearWishList();
            for (byte i = 0; i < 10; i++) {
                int sn = slea.readInt();
                CashItem cItem = CashItemFactory.getItem(sn);
                if (cItem != null && cItem.isOnSale() && sn != 0) {
                    cs.addToWishList(sn);
                }
            }
            c.announce(MaplePacketCreator.showWishList(chr, true));
        } else if (action == 0x06) { // Increase Inventory Slots
            slea.skip(1);
            int cash = slea.readInt();
            byte mode = slea.readByte();
            if (mode == 0) {
                byte type = slea.readByte();
                if (cs.getCash(cash) < 4000) {
                    c.enableCSActions();
                    return;
                }
                if (chr.gainSlots(type, 4, false)) {
                    c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
                    cs.gainCash(cash, -4000);
                    c.announce(MaplePacketCreator.showCash(chr));
                }
            } else {
                CashItem cItem = CashItemFactory.getItem(slea.readInt());
                int type = (cItem.getItemId() - 9110000) / 1000;
                if (!canBuy(cItem, cs.getCash(cash))) {
                    c.enableCSActions();
                    return;
                }
                if (chr.gainSlots(type, 8, false)) {
                    c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
                    cs.gainCash(cash, cItem, chr.getWorld());
                    c.announce(MaplePacketCreator.showCash(chr));
                }
            }
        } else if (action == 0x07) { // Increase Storage Slots
            slea.skip(1);
            int cash = slea.readInt();
            byte mode = slea.readByte();
            if (mode == 0) {
                if (cs.getCash(cash) < 4000) {
                    c.enableCSActions();
                    return;
                }
                if (chr.getStorage().gainSlots(4)) {
                    FilePrinter.print(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " bought 4 slots to their account storage.\r\n");
                    chr.setUsedStorage();
                    
                    c.announce(MaplePacketCreator.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    cs.gainCash(cash, -4000);
                    c.announce(MaplePacketCreator.showCash(chr));
                }
            } else {
                CashItem cItem = CashItemFactory.getItem(slea.readInt());

                if (!canBuy(cItem, cs.getCash(cash))) {
                    c.enableCSActions();
                    return;
                }
                if (chr.getStorage().gainSlots(8)) {
                    c.announce(MaplePacketCreator.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    cs.gainCash(cash, cItem, chr.getWorld());
                    c.announce(MaplePacketCreator.showCash(chr));
                }
            }
        } else if (action == 0x08) { // Increase Character Slots
            slea.skip(1); 
            int cash = slea.readInt();
            CashItem cItem = CashItemFactory.getItem(slea.readInt());

            if (!canBuy(cItem, cs.getCash(cash))) {
                c.enableCSActions();
                return;
            }

            if (c.gainCharacterSlot()) {
                c.announce(MaplePacketCreator.showBoughtCharacterSlot(c.getCharacterSlots()));
                cs.gainCash(cash, cItem, chr.getWorld());
                c.announce(MaplePacketCreator.showCash(chr));
            } else {
                chr.dropMessage(1, "You have already used up all 12 extra character slots.");
                c.enableCSActions();
                return;
            }
        } else if (action == 0x0D) { // Take from Cash Inventory
            Item item = cs.findByCashId(slea.readInt());
            if (item == null) {
                c.enableCSActions();
                return;
            }
            if (chr.getInventory(item.getInventoryType()).addItem(item) != -1) {
                cs.removeFromInventory(item);
                c.announce(MaplePacketCreator.takeFromCashInventory(item));
                
                if(item instanceof Equip) {
                    Equip equip = (Equip) item;
                    if(equip.getRingId() >= 0) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        chr.addPlayerRing(ring);
                    }
                }
            }
        } else if (action == 0x0E) { // Put into Cash Inventory
            int cashId = slea.readInt();
            slea.skip(4);
            
            byte invType = slea.readByte();
            if (invType < 1 || invType > 5) {
                c.disconnect(false, false);
                return;
            }
            
            MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(invType));
            Item item = mi.findByCashId(cashId);
            if (item == null) {
                c.enableCSActions();
                return;
            } else if(c.getPlayer().getPetIndex(item.getPetId()) > -1) {
                chr.getClient().announce(MaplePacketCreator.serverNotice(1, "You cannot put the pet you currently equip into the Cash Shop inventory."));
                c.enableCSActions();
                return;
            }
            cs.addToInventory(item);
            mi.removeSlot(item.getPosition());
            c.announce(MaplePacketCreator.putIntoCashInventory(item, c.getAccID()));
        } else if (action == 0x1D) { //crush ring (action 28)
            int birthday = slea.readInt();
            if (checkBirthday(c, birthday)) {
                int toCharge = slea.readInt();
                int SN = slea.readInt();
                String recipientName = slea.readMapleAsciiString();
                String text = slea.readMapleAsciiString();
                CashItem itemRing = CashItemFactory.getItem(SN);
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(recipientName);
                if (partner == null) {
                    chr.getClient().announce(MaplePacketCreator.serverNotice(1, "The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel."));
                } else {
                	
                  /*  if (partner.getGender() == chr.getGender()) {
                        chr.dropMessage("You and your partner are the same gender, please buy a friendship ring.");
                        c.enableCSActions();
                        return;
                    }*/ //Gotta let them faggots marry too, hence why this is commented out <3 
                	
                    if(itemRing.toItem() instanceof Equip) {
                        Equip eqp = (Equip) itemRing.toItem();
                        Pair<Integer, Integer> rings = MapleRing.createRing(itemRing.getItemId(), chr, partner);
                        eqp.setRingId(rings.getLeft());
                        cs.addToInventory(eqp);
                        c.announce(MaplePacketCreator.showBoughtCashItem(eqp, c.getAccID()));
                        cs.gift(partner.getId(), chr.getName(), text, eqp.getSN(), rings.getRight());
                        cs.gainCash(toCharge, itemRing, chr.getWorld());
                        chr.addCrushRing(MapleRing.loadFromDb(rings.getLeft()));
                        try {
                            chr.sendNote(partner.getName(), text, (byte) 1);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        partner.showNote();
                    }   
                }
            } else {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
            }
                
            c.announce(MaplePacketCreator.showCash(c.getPlayer()));
        } else if (action == 0x20) {
            int serialNumber = slea.readInt();  // thanks GabrielSin for detecting a potential exploit with 1 meso cash items.
            if (serialNumber / 10000000 != 8) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC0));
                return;
            }
            
            CashItem item = CashItemFactory.getItem(serialNumber);
            if (item == null || !item.isOnSale()) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC0));
                return;
            }
            
            int itemId = item.getItemId();
            int itemPrice = item.getPrice();
            if (itemPrice <= 0) {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC0));
                return;
            }
            
            if (chr.getMeso() >= itemPrice) {
                if (chr.canHold(itemId)) {
                    chr.gainMeso(-itemPrice, false);
                    MapleInventoryManipulator.addById(c, itemId, (short) 1, "", -1);
                    c.announce(MaplePacketCreator.showBoughtQuestItem(itemId));
                }
            }
            c.announce(MaplePacketCreator.showCash(c.getPlayer()));
        } else if (action == 0x23) { //Friendship :3
            int birthday = slea.readInt();
            if (checkBirthday(c, birthday)) {
                int payment = slea.readByte();
                slea.skip(3); //0s
                int snID = slea.readInt();
                CashItem itemRing = CashItemFactory.getItem(snID);
                String sentTo = slea.readMapleAsciiString();
                int available = slea.readShort() - 1;
                String text = slea.readAsciiString(available);
                slea.readByte();
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
                if (partner == null) {
                    chr.dropMessage("The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel.");
                } else {
                    // Need to check to make sure its actually an equip and the right SN...
                    if(itemRing.toItem() instanceof Equip) {
                        Equip eqp = (Equip) itemRing.toItem();
                        Pair<Integer, Integer> rings = MapleRing.createRing(itemRing.getItemId(), chr, partner);
                        eqp.setRingId(rings.getLeft());
                        cs.addToInventory(eqp);
                        c.announce(MaplePacketCreator.showBoughtCashItem(eqp, c.getAccID()));
                        cs.gift(partner.getId(), chr.getName(), text, eqp.getSN(), rings.getRight());
                        cs.gainCash(payment, -itemRing.getPrice());
                        chr.addFriendshipRing(MapleRing.loadFromDb(rings.getLeft()));
                        try {
                            chr.sendNote(partner.getName(), text, (byte) 1);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        partner.showNote();
                    }
                }
            } else {
                c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
            }
                
            c.announce(MaplePacketCreator.showCash(c.getPlayer()));
        } else {
            System.out.println("Unhandled action: " + action + "\n" + slea);
        }
    }

    public static boolean checkBirthday(MapleClient c, int idate) {
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        return c.checkBirthDate(cal);
    }
    
    private static boolean canBuy(CashItem item, int cash) {
        return item != null && item.isOnSale() && item.getPrice() <= cash;
    }
}
