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

import config.YamlConfig;
import tools.LogHelper;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.game.GameConstants;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
import net.server.coordinator.world.MapleInviteCoordinator.MapleInviteResult;
import tools.Pair;

/**
 *
 * @author Matze
 * @author Ronan - concurrency safety + check available slots + trade results
 */
public class MapleTrade {
    
    public enum TradeResult {
        NO_RESPONSE(1),
        PARTNER_CANCEL(2),
        SUCCESSFUL(7),
        UNSUCCESSFUL(8),
        UNSUCCESSFUL_UNIQUE_ITEM_LIMIT(9),
        UNSUCCESSFUL_ANOTHER_MAP(12),
        UNSUCCESSFUL_DAMAGED_FILES(13);
        
        private final int res;

        private TradeResult(int res) {
            this.res = res;
        }
        
        private byte getValue() {
            return (byte) res;
        }
    }
    
    private MapleTrade partner = null;
    private List<Item> items = new ArrayList<>();
    private List<Item> exchangeItems;
    private int meso = 0;
    private int exchangeMeso;
    private AtomicBoolean locked = new AtomicBoolean(false);
    private MapleCharacter chr;
    private byte number;
    private boolean fullTrade = false;
    
    public MapleTrade(byte number, MapleCharacter chr) {
        this.chr = chr;
        this.number = number;
    }

    public static int getFee(long meso) {
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

    private void fetchExchangedItems() {
        exchangeItems = partner.getItems();
        exchangeMeso = partner.getMeso();
    }

    private void completeTrade() {
        byte result;
        boolean show = YamlConfig.config.server.USE_DEBUG;
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
            
            result = TradeResult.NO_RESPONSE.getValue();
        } else {
            result = TradeResult.SUCCESSFUL.getValue();
        }
        
        exchangeMeso = 0;
        if (exchangeItems != null) {
            exchangeItems.clear();
        }
        
        chr.getClient().announce(MaplePacketCreator.getTradeResult(number, result));
    }

    private void cancel(byte result) {
        boolean show = YamlConfig.config.server.USE_DEBUG;
        
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
        
        chr.getClient().announce(MaplePacketCreator.getTradeResult(number, result));
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
            System.out.println("[Hack] " + chr.getName() + " Trying to trade < 0 mesos");
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

    public boolean addItem(Item item) {
        synchronized (items) {
            if (items.size() > 9) {
                return false;
            }
            for (Item it : items) {
                if (it.getPosition() == item.getPosition()) {
                    return false;
                }
            }
            
            items.add(item);
        }
        
        return true;
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
    
    private boolean fitsMeso() {
        return chr.canHoldMeso(exchangeMeso - getFee(exchangeMeso));
    }
    
    private boolean fitsInInventory() {
        List<Pair<Item, MapleInventoryType>> tradeItems = new LinkedList<>();
        for (Item item : exchangeItems) {
            tradeItems.add(new Pair<>(item, item.getInventoryType()));
        }
        
        return MapleInventory.checkSpotsAndOwnership(chr, tradeItems);
    }
    
    private boolean fitsUniquesInInventory() {
        List<Integer> exchangeItemids = new LinkedList<>();
        for (Item item : exchangeItems) {
            exchangeItemids.add(item.getItemId());
        }
        
        return chr.canHoldUniques(exchangeItemids);
    }
    
    private synchronized boolean checkTradeCompleteHandshake(boolean updateSelf) {
        MapleTrade self, other;
                
        if (updateSelf) {
            self = this;
            other = this.getPartner();
        } else {
            self = this.getPartner();
            other = this;
        }
        
        if (self.isLocked()) {
            return false;
        }
        
        self.lockTrade();
        return other.isLocked();
    }
    
    private boolean checkCompleteHandshake() {  // handshake checkout thanks to Ronan
        if (this.getChr().getId() < this.getPartner().getChr().getId()) {
            return this.checkTradeCompleteHandshake(true);
        } else {
            return this.getPartner().checkTradeCompleteHandshake(false);
        }
    }
    
    public static void completeTrade(MapleCharacter chr) {
        MapleTrade local = chr.getTrade();
        MapleTrade partner = local.getPartner();
        if (local.checkCompleteHandshake()) {
            local.fetchExchangedItems();
            partner.fetchExchangedItems();
            
            if (!local.fitsMeso()) {
                cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL);
                chr.message("There is not enough meso inventory space to complete the trade.");
                partner.getChr().message("Partner does not have enough meso inventory space to complete the trade.");
                return;
            } else if (!partner.fitsMeso()) {
                cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL);
                chr.message("Partner does not have enough meso inventory space to complete the trade.");
                partner.getChr().message("There is not enough meso inventory space to complete the trade.");
                return;
            }
            
            if (!local.fitsInInventory()) {
                if (local.fitsUniquesInInventory()) {
                    cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL);
                    chr.message("There is not enough inventory space to complete the trade.");
                    partner.getChr().message("Partner does not have enough inventory space to complete the trade.");
                } else {
                    cancelTrade(local.getChr(), TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT);
                    partner.getChr().message("Partner cannot hold more than one one-of-a-kind item at a time.");
                }
                return;
            } else if (!partner.fitsInInventory()) {
                if (partner.fitsUniquesInInventory()) {
                    cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL);
                    chr.message("Partner does not have enough inventory space to complete the trade.");
                    partner.getChr().message("There is not enough inventory space to complete the trade.");
                } else {
                    cancelTrade(partner.getChr(), TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT);
                    chr.message("Partner cannot hold more than one one-of-a-kind item at a time.");
                }
                return;
            }
            
            if (local.getChr().getLevel() < 15) {
                if (local.getChr().getMesosTraded() + local.exchangeMeso > 1000000) {
                    cancelTrade(local.getChr(), TradeResult.NO_RESPONSE);
                    local.getChr().getClient().announce(MaplePacketCreator.serverNotice(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    local.getChr().addMesosTraded(local.exchangeMeso);
                }
            } else if (partner.getChr().getLevel() < 15) {
                if (partner.getChr().getMesosTraded() + partner.exchangeMeso > 1000000) {
                    cancelTrade(partner.getChr(), TradeResult.NO_RESPONSE);
                    partner.getChr().getClient().announce(MaplePacketCreator.serverNotice(1, "Characters under level 15 may not trade more than 1 million mesos per day."));
                    return;
                } else {
                    partner.getChr().addMesosTraded(partner.exchangeMeso);
                }
            }
            
            LogHelper.logTrade(local, partner);
            local.completeTrade();
            partner.completeTrade();
            
            partner.getChr().setTrade(null);
            chr.setTrade(null);
        }
    }
    
    private static void cancelTradeInternal(MapleCharacter chr, byte selfResult, byte partnerResult) {
        MapleTrade trade = chr.getTrade();
        if(trade == null) return;
        
        trade.cancel(selfResult);
        if (trade.getPartner() != null) {
            trade.getPartner().cancel(partnerResult);
            trade.getPartner().getChr().setTrade(null);
            
            MapleInviteCoordinator.answerInvite(InviteType.TRADE, trade.getChr().getId(), trade.getPartner().getChr().getId(), false);
            MapleInviteCoordinator.answerInvite(InviteType.TRADE, trade.getPartner().getChr().getId(), trade.getChr().getId(), false);
        }
        chr.setTrade(null);
    }
    
    private static byte[] tradeResultsPair(byte result) {
        byte selfResult, partnerResult;
        
        if (result == TradeResult.PARTNER_CANCEL.getValue()) {
            partnerResult = result;
            selfResult = TradeResult.NO_RESPONSE.getValue();
        } else if (result == TradeResult.UNSUCCESSFUL_UNIQUE_ITEM_LIMIT.getValue()) {
            partnerResult = TradeResult.UNSUCCESSFUL.getValue();
            selfResult = result;
        } else {
            partnerResult = result;
            selfResult = result;
        }
        
        return new byte[]{selfResult, partnerResult};
    }
    
    private synchronized void tradeCancelHandshake(boolean updateSelf, byte result) {
        byte selfResult, partnerResult;
        MapleTrade self;
        
        byte[] pairedResult = tradeResultsPair(result);
        selfResult = pairedResult[0];
        partnerResult = pairedResult[1];
        
        if (updateSelf) {
            self = this;
        } else {
            self = this.getPartner();
        }
        
        cancelTradeInternal(self.getChr(), selfResult, partnerResult);
    }
    
    private void cancelHandshake(byte result) {  // handshake checkout thanks to Ronan
        MapleTrade partner = this.getPartner();
        if (partner == null || this.getChr().getId() < partner.getChr().getId()) {
            this.tradeCancelHandshake(true, result);
        } else {
            partner.tradeCancelHandshake(false, result);
        }
    }

    public static void cancelTrade(MapleCharacter chr, TradeResult result) {
        MapleTrade trade = chr.getTrade();
        if(trade == null) return;
        
        trade.cancelHandshake(result.getValue());
    }
    
    public static void startTrade(MapleCharacter chr) {
        if (chr.getTrade() == null) {
            chr.setTrade(new MapleTrade((byte) 0, chr));
        }
    }
    
    private static boolean hasTradeInviteBack(MapleCharacter c1, MapleCharacter c2) {
        MapleTrade other = c2.getTrade();
        if (other != null) {
            MapleTrade otherPartner = other.getPartner();
            if (otherPartner != null) {
                if (otherPartner.getChr().getId() == c1.getId()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static void inviteTrade(MapleCharacter c1, MapleCharacter c2) {
        if (MapleInviteCoordinator.hasInvite(InviteType.TRADE, c1.getId())) {
            if (hasTradeInviteBack(c1, c2)) {
                c1.message("You are already managing this player's trade invitation.");
            } else {
                c1.message("You are already managing someone's trade invitation.");
            }
            
            return;
        } else if (c1.getTrade().isFullTrade()) {
            c1.message("You are already in a trade.");
            return;
        }
        
        if (MapleInviteCoordinator.createInvite(InviteType.TRADE, c1, c1.getId(), c2.getId())) {
            if (c2.getTrade() == null) {
                c2.setTrade(new MapleTrade((byte) 1, c2));
                c2.getTrade().setPartner(c1.getTrade());
                c1.getTrade().setPartner(c2.getTrade());
                
                c1.getClient().announce(MaplePacketCreator.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 0));
                c2.getClient().announce(MaplePacketCreator.tradeInvite(c1));
            } else {
                c1.message("The other player is already trading with someone else.");
                cancelTrade(c1, TradeResult.NO_RESPONSE);
                MapleInviteCoordinator.answerInvite(InviteType.TRADE, c2.getId(), c1.getId(), false);
            }
        } else {
            c1.message("The other player is already managing someone else's trade invitation.");
            cancelTrade(c1, TradeResult.NO_RESPONSE);
        }
    }

    public static void visitTrade(MapleCharacter c1, MapleCharacter c2) {
        MapleInviteResult inviteRes = MapleInviteCoordinator.answerInvite(InviteType.TRADE, c1.getId(), c2.getId(), true);
        
        InviteResult res = inviteRes.result;
        if (res == InviteResult.ACCEPTED) {
            if (c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
                c2.getClient().announce(MaplePacketCreator.getTradePartnerAdd(c1));
                c1.getClient().announce(MaplePacketCreator.getTradeStart(c1.getClient(), c1.getTrade(), (byte) 1));
                c1.getTrade().setFullTrade(true);
                c2.getTrade().setFullTrade(true);
            } else {
                c1.message("The other player has already closed the trade.");
            }
        } else {
            c1.message("This trade invitation already rescinded.");
            cancelTrade(c1, TradeResult.NO_RESPONSE);
        }
    }

    public static void declineTrade(MapleCharacter chr) {
        MapleTrade trade = chr.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                MapleCharacter other = trade.getPartner().getChr();
                if (MapleInviteCoordinator.answerInvite(InviteType.TRADE, chr.getId(), other.getId(), false).result == InviteResult.DENIED) {
                    other.message(chr.getName() + " has declined your trade request.");
                }
                
                other.getTrade().cancel(TradeResult.PARTNER_CANCEL.getValue());
                other.setTrade(null);
                
            }
            trade.cancel(TradeResult.NO_RESPONSE.getValue());
            chr.setTrade(null);
        }
    }

	public boolean isFullTrade() {
		return fullTrade;
	}

	public void setFullTrade(boolean fullTrade) {
		this.fullTrade = fullTrade;
	}
}