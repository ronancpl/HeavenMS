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

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.RewardItem;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Jay Estrella/ Modified by kevintjuh93
 */
public final class ItemRewardHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt(); // will load from xml I don't care.
        if (c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot).getItemId() != itemId || c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) < 1) return;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Pair<Integer, List<RewardItem>> rewards = ii.getItemReward(itemId);
        for (RewardItem reward : rewards.getRight()) {
            if (!MapleInventoryManipulator.checkSpace(c, reward.itemid, reward.quantity, "")) {
                c.announce(MaplePacketCreator.getShowInventoryFull());
                break;
            }
            if (Randomizer.nextInt(rewards.getLeft()) < reward.prob) {//Is it even possible to get an item with prob 1?
            	if (ItemConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                    final Item item = ii.getEquipById(reward.itemid);
                    if (reward.period != -1) {
                    	item.setExpiration(currentServerTime() + (reward.period * 60 * 60 * 10));
                    }
                    MapleInventoryManipulator.addFromDrop(c, item, false);
                } else {
                    MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, "", -1);
                }
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, false, false);
                if (reward.worldmsg != null) {
                    String msg = reward.worldmsg;
                    msg.replaceAll("/name", c.getPlayer().getName());
                    msg.replaceAll("/item", ii.getName(reward.itemid));
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(6, msg));
                }
                break;
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }
}
