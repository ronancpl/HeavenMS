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
package server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import tools.LogHelper;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.GameConstants;
import constants.ServerConstants;
import tools.Pair;

/**
 *
 * @author Matze
 * @author Ronan (concurrency safety & check available slots)
 */
public class MapleTrade {
    private MapleTrade partner = null;
    private List<Item> items = new ArrayList<>();
    private List<Item> exchangeItems;
    private int meso = 0;
    private int exchangeMeso;
    private AtomicBoolean locked = new AtomicBoolean(false);
    private MapleCharacter chr;
    private byte number;
    private boolean fullTrade = false;
    
    public MapleTrade(byte number, MapleCharacter c) {
        chr = c;
        this.number = number;
    }

    private static int getFee(long meso) {
        long fee = 0;
        if (meso >= 100000000) {
            fee = (meso * 6) / 100;
        } else if (meso >= 25000000) {
            fee = (meso * 5) / 100;
        } else if (meso >= 10000000) {
            fee = (meso * 4) / 100;
        } else if (meso >= 5000000) {
            fee = (meso * 3) / 100;
        } else if (meso >= 1000000) {
            fee = (meso * 18) / 1000;
        } else if (meso >= 100000) {
            fee = (meso * 8) / 1000;
        }
        return (int) fee;
    }

    private void lockTrade() {
        locked.set(true);
        partner.getChr().getClient().announce(MaplePacketCreator.getTradeConfirmation());
    }

    private void complete1() {
        exchangeItems = partner.getItems();
        exchangeMeso = partner.getMeso();
    }

    private void complete2() {
        boolean show = ServerConstants.USE_DEBUG;
        
        items.clear();
        meso = 0;
        for (Item item : exchangeItems) {
            MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
            MapleInventoryManipulator.addFromDrop(chr.getClient(), item, show);
        }
        
        if (exchangeMeso > 0) {
            int fee = getFee(exchangeMeso);
            
            chr.gainMeso(exchangeMeso - fee, show, true, show);
            if(fee > 0) {
                chr.dropMessage(1, "Transaction completed. You received " + GameConstants.numberWithCommas(exchangeMeso - fee) + " mesos due to trade fees.");
            } else {
                chr.dropMessage(1, "Transaction completed. You received " + GameConstants.numberWithCommas(exchangeMeso) + " mesos.");
            }
        } else {
            chr.dropMessage(1, "Transaction completed.");
        }
        
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }
        chr.getClient().announce(MaplePacketCreator.getTradeCompletion(number));
    }

    private void cancel() {
        boolean show = ServerConstants.USE_DEBUG;
        
        for (Item item : items) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), item, show);
        }
        if (meso > 0) {
            chr.gainMeso(meso, show, true, show);
        }
        meso = 0;
        if (items != null) {
            items.clear();
        }
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }
        chr.getClient().announce(MaplePacketCreator.getTradeCancel(number));
    }

    private boolean isLocked() {
        return locked.get();
    }

    private int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (locked.get()) {
            throw new RuntimeException("Trade is locked.");
        }
        if (meso < 0) {
            System.out.println("[h4x] " + chr.getName() + " Trying to trade < 0 mesos");
            return;
        }
        if (chr.getMeso() >= meso) {
            chr.gainMeso(-meso, false, true, false);
            this.meso += meso;
            chr.getClient().announce(MaplePacketCreator.getTradeMesoSet((byte) 0, this.meso));
            if (partner != null) {
                partner.getChr().getClient().announce(MaplePacketCreator.getTradeMesoSet((byte) 1, this.meso));
            }
        } else {
        }
    }

    public void addItem(Item item) {
        items.add(item);
        chr.getClient().announce(MaplePacketCreator.getTradeItemAdd((byte) 0, item));
        if (partner != null) {
            partner.getChr().getClient().announce(MaplePacketCreator.getTradeItemAdd((byte) 1, item));
        }
    }

    public void chat(String message) {
        chr.getClient().announce(MaplePacketCreator.getTradeChat(chr, message, true));
        if (partner != null) {
            partner.getChr().getClient().announce(MaplePacketCreator.getTradeChat(chr, message, false));
        }
    }

    public MapleTrade getPartner() {
        return partner;
    }

    public void setPartner(MapleTrade partner) {
        if (locked.get()) {
            return;
        }
        this.partner = partner;
    }

    public MapleCharacter getChr() {
        return chr;
    }

    public List<Item> getItems() {
        return new LinkedList<>(items);
    }

    public int getExchangeMesos(){
    	return exchangeMeso;
    }
    
    private boolean fitsInInventory() {
        List<Pair<Item, MapleInventoryType>> tradeItems = new LinkedList<>();
        for (Item item : exchangeItems) {
            tradeItems.add(new Pair<>(item, item.getInventoryType()));
        }
        
        return MapleInventory.checkSpotsAndOwnership(chr, tradeItems);
    }

    public static void completeTrade(MapleCharacter c) {
        c.getTrade().lockTrade();
        MapleTrade local = c.getTrade();
        MapleTrade partner = local.getPartner();
        if (partner.isLocked()) {
            local.complete1();
            partner.complete1();
            if (!local.fitsInInventory()) {
                cancelTrade(c);
                c.message("There is not enough inventory space to complete the trade.");
                partner.getChr().message("Partner does not have enough inventory space to complete the trade.");
                return;
            }
            else if (!partner.fitsInInventory()) {
                cancelTrade(c);
                c.message("Partner does not have enough inventory space to complete the trade.");
                partner.getChr().message("There is not enough inventory space to complete the trade.");
                return;
            }
            if (local.getChr().getLevel() < 15) {
                if (local.getChr().getMesosTraded() + local.exchangeMeso > 1000000) {
                    cancelTrade(c);
                    local.getChr().getClient().announce(MaplePacketCreator.serverNotice(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    local.getChr().addMesosTraded(local.exchangeMeso);
                }
            } else if (partner.getChr().getLevel() < 15) {
                if (partner.getChr().getMesosTraded() + partner.exchangeMeso > 1000000) {
                    cancelTrade(c);
                    partner.getChr().getClient().announce(MaplePacketCreator.serverNotice(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    partner.getChr().addMesosTraded(partner.exchangeMeso);
                }
            }
            LogHelper.logTrade(local, partner);
            local.complete2();
            partner.complete2();
            partner.getChr().setTrade(null);
            c.setTrade(null);
        }
    }

    public static void cancelTrade(MapleCharacter c) {
        MapleTrade trade = c.getTrade();
        if(trade == null) return;
        
        trade.cancel();
        if (trade.getPartner() != null) {
            trade.getPartner().cancel();
            trade.getPartner().getChr().setTrade(null);
        }
        c.setTrade(null);
    }

    public static void startTrade(MapleCharacter c) {
        if (c.getTrade() == null) {
            c.setTrade(new MapleTrade((byte) 0, c));
            c.getClient().announce(MaplePacketCreator.getTradeStart(c.getClient(), c.getTrade(), (byte) 0));
        } else {
            c.message("You are already in a trade.");
        }
    }

    public static void inviteTrade(MapleCharacter c1, MapleCharacter c2) {
        if (c2.getTrade() == null) {
            c2.setTrade(new MapleTrade((byte) 1, c2));
            c2.getTrade().setPartner(c1.getTrade());
            c1.getTrade().setPartner(c2.getTrade());
            c2.getClient().announce(MaplePacketCreator.getTradeInvite(c1));
        } else {
            c1.message("The other player is already trading with someone else.");
            cancelTrade(c1);
        }
    }

    public static void visitTrade(MapleCharacter c1, MapleCharacter c2) {
        if (c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
            c2.getClient().announce(MaplePacketCreator.getTradePartnerAdd(c1));
            c1.getClient().announce(MaplePacketCreator.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 1));
            c1.getTrade().setFullTrade(true);
            c2.getTrade().setFullTrade(true);
        } else {
            c1.message("The other player has already closed the trade.");
        }
    }

    public static void declineTrade(MapleCharacter c) {
        MapleTrade trade = c.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                MapleCharacter other = trade.getPartner().getChr();
                other.getTrade().cancel();
                other.setTrade(null);
                other.message(c.getName() + " has declined your trade request.");
            }
            trade.cancel();
            c.setTrade(null);
        }
    }

	public boolean isFullTrade() {
		return fullTrade;
	}

	public void setFullTrade(boolean fullTrade) {
		this.fullTrade = fullTrade;
	}
}