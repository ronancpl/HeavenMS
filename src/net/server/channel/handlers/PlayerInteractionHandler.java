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

import java.util.Arrays;

import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleTrade;
import server.maps.FieldLimit;
import server.maps.HiredMerchant;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class PlayerInteractionHandler extends AbstractMaplePacketHandler {
    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5),
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN(0xB),
        TRADE_BIRTHDAY(0x0E),
        SET_ITEMS(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_STORE(0x1E),
        PUT_ITEM(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        BLACKLIST(0x2F),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);
        final byte code;

        private Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        MapleCharacter chr = c.getPlayer();
        if (mode == Action.CREATE.getCode()) {
            byte createType = slea.readByte();
            if (createType == 3) {// trade
                MapleTrade.startTrade(chr);
            } else if (createType == 1) { // omok mini game
                if (chr.getChalkboard() != null || FieldLimit.CANNOTMINIGAME.check(chr.getMap().getFieldLimit())) {
                    return;
                }
                String desc = slea.readMapleAsciiString();
                slea.readByte(); // 20 6E 4E
                int type = slea.readByte(); // 20 6E 4E
                MapleMiniGame game = new MapleMiniGame(chr, desc);
                chr.setMiniGame(game);
                game.setPieceType(type);
                game.setGameType("omok");
                chr.getMap().addMapObject(game);
                chr.getMap().broadcastMessage(MaplePacketCreator.addOmokBox(chr, 1, 0));
                game.sendOmok(c, type);
            } else if (createType == 2) { // matchcard
                if (chr.getChalkboard() != null) {
                    return;
                }
                String desc = slea.readMapleAsciiString();
                slea.readByte(); // 20 6E 4E
                int type = slea.readByte(); // 20 6E 4E
                MapleMiniGame game = new MapleMiniGame(chr, desc);
                game.setPieceType(type);
                if (type == 0) {
                    game.setMatchesToWin(6);
                } else if (type == 1) {
                    game.setMatchesToWin(10);
                } else if (type == 2) {
                    game.setMatchesToWin(15);
                }
                game.setGameType("matchcard");
                chr.setMiniGame(game);
                chr.getMap().addMapObject(game);
                chr.getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(chr, 1, 0));
                game.sendMatchCard(c, type);
            } else if (createType == 4 || createType == 5) { // shop
                if (!chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).isEmpty()) {
                    return;
                }
                String desc = slea.readMapleAsciiString();
                slea.skip(3);
                int itemId = slea.readInt();
                if (chr.getInventory(MapleInventoryType.CASH).countById(itemId) < 1) {
                    return;
                }

                if (chr.getMapId() > 910000000 && chr.getMapId() < 910000023 || itemId > 5030000 && itemId < 5030012 || itemId > 5140000 && itemId < 5140006) {
                    if (createType == 4) {
                        MaplePlayerShop shop = new MaplePlayerShop(c.getPlayer(), desc);
                        chr.setPlayerShop(shop);
                        chr.getMap().addMapObject(shop);
                        shop.sendShop(c);
                        c.announce(MaplePacketCreator.getPlayerShopRemoveVisitor(1));
                    } else {
                        HiredMerchant merchant = new HiredMerchant(chr, itemId, desc);
                        chr.setHiredMerchant(merchant);
                        chr.getClient().getChannelServer().addHiredMerchant(chr.getId(), merchant);
                        chr.announce(MaplePacketCreator.getHiredMerchant(chr, merchant, true));
                    }
                }
            }
        } else if (mode == Action.INVITE.getCode()) {
            int otherPlayer = slea.readInt();
            if (chr.getId() == chr.getMap().getCharacterById(otherPlayer).getId()) {
            	return;
            }
            MapleTrade.inviteTrade(chr, chr.getMap().getCharacterById(otherPlayer));
        } else if (mode == Action.DECLINE.getCode()) {
            MapleTrade.declineTrade(chr);
        } else if (mode == Action.VISIT.getCode()) {
            if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
            	if (!chr.getTrade().isFullTrade() && !chr.getTrade().getPartner().isFullTrade()) {
            		MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
            	} else {
            		c.announce(MaplePacketCreator.enableActions()); //Ill be nice and not dc u
            		return;
            	}
            } else {
                int oid = slea.readInt();
                MapleMapObject ob = chr.getMap().getMapObject(oid);
                if (ob instanceof MaplePlayerShop) {
                    MaplePlayerShop shop = (MaplePlayerShop) ob;
                    if (shop.isBanned(chr.getName())) {
                        chr.dropMessage(1, "You have been banned from this store.");
                        return;
                    }
                    if (shop.hasFreeSlot() && !shop.isVisitor(c.getPlayer())) {
                        shop.addVisitor(c.getPlayer());
                        chr.setPlayerShop(shop);
                        shop.sendShop(c);
                    }
                } else if (ob instanceof MapleMiniGame) {
                    MapleMiniGame game = (MapleMiniGame) ob;
                    if (game.hasFreeSlot() && !game.isVisitor(c.getPlayer())) {
                        game.addVisitor(c.getPlayer());
                        chr.setMiniGame(game);
                        switch (game.getGameType()) {
                            case "omok":
                                game.sendOmok(c, game.getPieceType());
                                break;
                            case "matchcard":
                                game.sendMatchCard(c, game.getPieceType());
                                break;
                        }
                    } else {
                        chr.getClient().announce(MaplePacketCreator.getMiniGameFull());
                    }
                } else if (ob instanceof HiredMerchant && chr.getHiredMerchant() == null) {
                    HiredMerchant merchant = (HiredMerchant) ob;
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors("");
                        c.announce(MaplePacketCreator.getHiredMerchant(chr, merchant, false));
                    } else if (!merchant.isOpen()) {
                        chr.dropMessage(1, "This shop is in maintenance, please come by later.");
                        return;
                    } else if (merchant.getFreeSlot() == -1) {
                        chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
                        return;
                    } else {
                        merchant.addVisitor(c.getPlayer());
                        c.announce(MaplePacketCreator.getHiredMerchant(c.getPlayer(), merchant, false));
                    }
                    chr.setHiredMerchant(merchant);
                }
            }
        } else if (mode == Action.CHAT.getCode()) { // chat lol
            HiredMerchant merchant = chr.getHiredMerchant();
            if (chr.getTrade() != null) {
                chr.getTrade().chat(slea.readMapleAsciiString());
            } else if (chr.getPlayerShop() != null) { //mini game
                MaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null) {
                    shop.chat(c, slea.readMapleAsciiString());
                }
            } else if (chr.getMiniGame() != null) {
                MapleMiniGame game = chr.getMiniGame();
                if (game != null) {
                    game.chat(c, slea.readMapleAsciiString());
                }
            } else if (merchant != null) {
                String message = chr.getName() + " : " + slea.readMapleAsciiString();
                byte slot = (byte) (merchant.getVisitorSlot(c.getPlayer()) + 1);
                merchant.getMessages().add(new Pair<>(message, slot));
                merchant.broadcastToVisitors(MaplePacketCreator.hiredMerchantChat(message, slot));
            }
        } else if (mode == Action.EXIT.getCode()) {
            if (chr.getTrade() != null) {
                MapleTrade.cancelTrade(c.getPlayer());
            } else {
                MaplePlayerShop shop = chr.getPlayerShop();
                MapleMiniGame game = chr.getMiniGame();
                HiredMerchant merchant = chr.getHiredMerchant();
                if (shop != null) {
                    if (shop.isOwner(c.getPlayer())) {
                        for (MaplePlayerShopItem mpsi : shop.getItems()) {
                            if (mpsi.getBundles() > 2) {
                                Item iItem = mpsi.getItem().copy();
                                iItem.setQuantity((short) (mpsi.getBundles() * iItem.getQuantity()));
                                MapleInventoryManipulator.addFromDrop(c, iItem, false);
                            } else if (mpsi.isExist()) {
                                MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), true);
                            }
                        }
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(c.getPlayer()));
                        shop.removeVisitors();
                    } else {
                        shop.removeVisitor(c.getPlayer());
                    }
                    chr.setPlayerShop(null);
                } else if (game != null) {
                    chr.setMiniGame(null);
                    if (game.isOwner(c.getPlayer())) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(c.getPlayer()));
                        game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose());
                    } else {
                        game.removeVisitor(c.getPlayer());
                    }
                } else if (merchant != null) {
                    merchant.removeVisitor(c.getPlayer());
                    chr.setHiredMerchant(null);
                }
            }
        } else if (mode == Action.OPEN.getCode()) {
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                slea.readByte();//01
                chr.getMap().broadcastMessage(MaplePacketCreator.addCharBox(c.getPlayer(), 4));
            } else if (merchant != null && merchant.isOwner(c.getPlayer())) {
                chr.setHasMerchant(true);
                merchant.setOpen(true);
                chr.getMap().addMapObject(merchant);
                chr.setHiredMerchant(null);
                chr.getMap().broadcastMessage(MaplePacketCreator.spawnHiredMerchant(merchant));
                slea.readByte();
            }
        } else if (mode == Action.READY.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            game.broadcast(MaplePacketCreator.getMiniGameReady(game));
        } else if (mode == Action.UN_READY.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            game.broadcast(MaplePacketCreator.getMiniGameUnReady(game));
        } else if (mode == Action.START.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getGameType().equals("omok")) {
                game.broadcast(MaplePacketCreator.getMiniGameStart(game, game.getLoser()));
                chr.getMap().broadcastMessage(MaplePacketCreator.addOmokBox(game.getOwner(), 2, 1));
            }
            if (game.getGameType().equals("matchcard")) {
                game.shuffleList();
                game.broadcast(MaplePacketCreator.getMatchCardStart(game, game.getLoser()));
                chr.getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(game.getOwner(), 2, 1));
            }
        } else if (mode == Action.GIVE_UP.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.getGameType().equals("omok")) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMiniGameOwnerForfeit(game));
                } else {
                    game.broadcast(MaplePacketCreator.getMiniGameVisitorForfeit(game));
                }
            }
            if (game.getGameType().equals("matchcard")) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMatchCardVisitorWin(game));
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardOwnerWin(game));
                }
            }
        } else if (mode == Action.REQUEST_TIE.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcastToVisitor(MaplePacketCreator.getMiniGameRequestTie(game));
            } else {
                game.getOwner().getClient().announce(MaplePacketCreator.getMiniGameRequestTie(game));
            }
        } else if (mode == Action.ANSWER_TIE.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            slea.readByte();
            if (game.getGameType().equals("omok")) {
                game.broadcast(MaplePacketCreator.getMiniGameTie(game));
            }
            if (game.getGameType().equals("matchcard")) {
                game.broadcast(MaplePacketCreator.getMatchCardTie(game));
            }
        } else if (mode == Action.SKIP.getCode()) {
            MapleMiniGame game = chr.getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcast(MaplePacketCreator.getMiniGameSkipOwner(game));
            } else {
                game.broadcast(MaplePacketCreator.getMiniGameSkipVisitor(game));
            }
        } else if (mode == Action.MOVE_OMOK.getCode()) {
            int x = slea.readInt(); // x point
            int y = slea.readInt(); // y point
            int type = slea.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)
            chr.getMiniGame().setPiece(x, y, type, c.getPlayer());
        } else if (mode == Action.SELECT_CARD.getCode()) {
            int turn = slea.readByte(); // 1st turn = 1; 2nd turn = 0
            int slot = slea.readByte(); // slot
            MapleMiniGame game = chr.getMiniGame();
            int firstslot = game.getFirstSlot();
            if (turn == 1) {
                game.setFirstSlot(slot);
                if (game.isOwner(c.getPlayer())) {
                    game.broadcastToVisitor(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                } else {
                    game.getOwner().getClient().announce(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                }
            } else if ((game.getCardId(firstslot + 1)) == (game.getCardId(slot + 1))) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 2));
                    game.setOwnerPoints();
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 3));
                    game.setVisitorPoints();
                }
            } else if (game.isOwner(c.getPlayer())) {
                game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 0));
            } else {
                game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 1));
            }
        } else if (mode == Action.SET_MESO.getCode()) {
            chr.getTrade().setMeso(slea.readInt());
        } else if (mode == Action.SET_ITEMS.getCode()) {
        	
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
            Item item = chr.getInventory(ivType).getItem(slea.readShort());
            short quantity = slea.readShort();
            byte targetSlot = slea.readByte();
            if (quantity < 1 || quantity > item.getQuantity()) {
                c.announce(MaplePacketCreator.enableActions());
                return;            	
            }
            if (chr.getTrade() != null) {
                if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.isRechargable(item.getItemId())) {
                    if (ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window
                        if (!((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA || (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES)) {
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    }
                    Item tradeItem = item.copy();
                    if (ItemConstants.isRechargable(item.getItemId())) {
                        tradeItem.setQuantity(item.getQuantity());
                        MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), item.getQuantity(), true);
                    } else {
                        tradeItem.setQuantity(quantity);
                        MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);
                    }
                    tradeItem.setPosition(targetSlot);
                    chr.getTrade().addItem(tradeItem);
                }
            }
        } else if (mode == Action.CONFIRM.getCode()) {
            MapleTrade.completeTrade(c.getPlayer());
        } else if (mode == Action.ADD_ITEM.getCode() || mode == Action.PUT_ITEM.getCode()) {
        	MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
            short slot = slea.readShort();
            short bundles = slea.readShort();
            if (chr.getInventory(type).getItem(slot) == null || chr.getItemQuantity(chr.getInventory(type).getItem(slot).getItemId(), false) < bundles || chr.getInventory(type).getItem(slot).getFlag() == ItemConstants.UNTRADEABLE) {
                return;
            }
            short perBundle = slea.readShort();
            int price = slea.readInt();
            if (perBundle <= 0 || perBundle * bundles > 2000 || bundles <= 0 || price <= 0 || price > Integer.MAX_VALUE) {
            	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with hired merchants.");
            	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " might of possibly packet edited Hired Merchants\nperBundle: " + perBundle + "\nperBundle * bundles (This multiplied cannot be greater than 2000): " + perBundle * bundles + "\nbundles: " + bundles + "\nprice: " + price);
                return;
            }
            Item ivItem = chr.getInventory(type).getItem(slot);
            Item sellItem = ivItem.copy();
            if (chr.getItemQuantity(ivItem.getItemId(), false) < perBundle * bundles) {
                return;
            }
            sellItem.setQuantity(perBundle);
            MaplePlayerShopItem item = new MaplePlayerShopItem(sellItem, bundles, price);
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                if (ivItem != null && ivItem.getQuantity() >= bundles * perBundle) {
                    shop.addItem(item);
                    c.announce(MaplePacketCreator.getPlayerShopItemUpdate(shop));
                }
            } else if (merchant != null && merchant.isOwner(c.getPlayer())) {
                merchant.addItem(item);
                c.announce(MaplePacketCreator.updateHiredMerchant(merchant, c.getPlayer()));
            }
            if (ItemConstants.isRechargable(ivItem.getItemId())) {
                MapleInventoryManipulator.removeFromSlot(c, type, slot, ivItem.getQuantity(), true);
            } else {
                MapleInventoryManipulator.removeFromSlot(c, type, slot, (short) (bundles * perBundle), true);
            }
        } else if (mode == Action.REMOVE_ITEM.getCode()) {
            MaplePlayerShop shop = chr.getPlayerShop();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                int slot = slea.readShort();
                if (slot >= shop.getItems().size() || slot < 0) {
                	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with a player shop.");
                	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to remove item at slot " + slot + "\r\n");
                	c.disconnect(true, false);
                	return;
                }
                MaplePlayerShopItem item = shop.getItems().get(slot);
                Item ivItem = item.getItem().copy();
                shop.removeItem(slot);
                ivItem.setQuantity(item.getBundles());
                MapleInventoryManipulator.addFromDrop(c, ivItem, false);
                c.announce(MaplePacketCreator.getPlayerShopItemUpdate(shop));
            }
        } else if (mode == Action.MERCHANT_MESO.getCode()) {//Hmmmm
            /*if (!chr.getHiredMerchant().isOwner(chr) || chr.getMerchantMeso() < 1) return;
            int possible = Integer.MAX_VALUE - chr.getMerchantMeso();
            if (possible > 0) {
                if (possible < chr.getMerchantMeso()) {
                    chr.gainMeso(possible, false);
                    chr.setMerchantMeso(chr.getMerchantMeso() - possible);
                } else {
                    chr.gainMeso(chr.getMerchantMeso(), false);
                    chr.setMerchantMeso(0);
                }
                c.announce(MaplePacketCreator.updateHiredMerchant(chr.getHiredMerchant(), chr));
            }*/
        } else if (mode == Action.MERCHANT_ORGANIZE.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (!merchant.isOwner(chr)) return;

            if (chr.getMerchantMeso() > 0) {
                int possible = Integer.MAX_VALUE - chr.getMerchantMeso();
                if (possible > 0) {
                    if (possible < chr.getMerchantMeso()) {
                        chr.gainMeso(possible, false);
                        chr.setMerchantMeso(chr.getMerchantMeso() - possible);
                    } else {
                        chr.gainMeso(chr.getMerchantMeso(), false);
                        chr.setMerchantMeso(0);
                    }
                }
            }
            for (int i = 0; i < merchant.getItems().size(); i++) {
                if (!merchant.getItems().get(i).isExist()) merchant.removeFromSlot(i);
            }
            if (merchant.getItems().isEmpty()) {
                c.announce(MaplePacketCreator.hiredMerchantOwnerLeave());
                c.announce(MaplePacketCreator.leaveHiredMerchant(0x00, 0x03));
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
                return;
            }
            c.announce(MaplePacketCreator.updateHiredMerchant(merchant, chr));

        } else if (mode == Action.BUY.getCode() || mode == Action.MERCHANT_BUY.getCode()) {
            int item = slea.readByte();
            short quantity = slea.readShort();
            if (quantity < 1) {
            	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with a hired merchant and or player shop.");
            	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to buy item " + item + " with quantity " + quantity + "\r\n");
            	c.disconnect(true, false);
            	return;
            }
            MaplePlayerShop shop = chr.getPlayerShop();
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.getOwner().equals(chr.getName())) {
                return;
            }
            if (shop != null && shop.isVisitor(c.getPlayer())) {
                shop.buy(c, item, quantity);
                shop.broadcast(MaplePacketCreator.getPlayerShopItemUpdate(shop));
            } else if (merchant != null) {
                merchant.buy(c, item, quantity);
                merchant.broadcastToVisitors(MaplePacketCreator.updateHiredMerchant(merchant, c.getPlayer()));
            }
        } else if (mode == Action.TAKE_ITEM_BACK.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                int slot = slea.readShort();
                MaplePlayerShopItem item = merchant.getItems().get(slot);
                if (!MapleInventory.checkSpot(chr, item.getItem())) {
                	c.announce(MaplePacketCreator.enableActions());
                	return;
                }
                if (item.getBundles() > 0) {
                    Item iitem = item.getItem();
                    iitem.setQuantity((short) (item.getItem().getQuantity() * item.getBundles()));                    
                    MapleInventoryManipulator.addFromDrop(c, iitem, true);
                }
                merchant.removeFromSlot(slot);
                c.announce(MaplePacketCreator.updateHiredMerchant(merchant, c.getPlayer()));
            }
        } else if (mode == Action.CLOSE_MERCHANT.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                c.announce(MaplePacketCreator.hiredMerchantOwnerLeave());
                c.announce(MaplePacketCreator.leaveHiredMerchant(0x00, 0x03));
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
            }
        } else if (mode == Action.MAINTENANCE_OFF.getCode()) {
            HiredMerchant merchant = chr.getHiredMerchant();
            if (merchant.getItems().isEmpty() && merchant.isOwner(c.getPlayer())) {
                merchant.closeShop(c, false);
                chr.setHasMerchant(false);
            }
            if (merchant != null && merchant.isOwner(c.getPlayer())) {
                merchant.getMessages().clear();
                merchant.setOpen(true);
            }
            chr.setHiredMerchant(null);
            c.announce(MaplePacketCreator.enableActions());
        } else if (mode == Action.BAN_PLAYER.getCode()) {
            if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(c.getPlayer())) {
                chr.getPlayerShop().banPlayer(slea.readMapleAsciiString());
            }
        }
    }
}
