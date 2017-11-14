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
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MakerItemFactory;
import server.MakerItemFactory.MakerItemCreateEntry;
import tools.Pair;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella, Ronan
 */
public final class MakerSkillHandler extends AbstractMaplePacketHandler {
    private MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int toCreate = slea.readInt();
        
        MakerItemCreateEntry recipe = MakerItemFactory.getItemCreateEntry(toCreate);
        short createStatus = getCreateStatus(c, recipe);
        
        switch(createStatus) {
            case 4: // no req skill level
                c.announce(MaplePacketCreator.serverNotice(1, "You don't have enough Maker level to complete this operation."));
                break;
                
            case 3: // no meso
                c.announce(MaplePacketCreator.serverNotice(1, "You don't have enough mesos to complete this operation."));
                break;
                    
            case 2: // no req level
                c.announce(MaplePacketCreator.serverNotice(1, "You don't have enough level to complete this operation."));
                break;
                
            case 1: // no items
                c.announce(MaplePacketCreator.serverNotice(1, "You don't have all required items in your inventory to make " + recipe.getRewardAmount() + " " + ii.getName(toCreate) + "."));
                break;
                
            default:
                if (!c.getPlayer().getInventory(ii.getInventoryType(toCreate)).isFull()) {
                    for (Pair<Integer, Integer> p : recipe.getReqItems()) {
                        int toRemove = p.getLeft();
                        MapleInventoryManipulator.removeById(c, ii.getInventoryType(toRemove), toRemove, p.getRight(), false, false);
                    }
                    MapleInventoryManipulator.addById(c, toCreate, (short) recipe.getRewardAmount());

                    c.announce(MaplePacketCreator.serverNotice(1, "You have created " + recipe.getRewardAmount() + " " + ii.getName(toCreate) + "."));
                    c.announce(MaplePacketCreator.showMakerEffect());
                } else {
                    c.announce(MaplePacketCreator.serverNotice(1, "Your inventory is full."));
                }
        }
    }

    private short getCreateStatus(MapleClient c, MakerItemCreateEntry recipe) {
        if(!hasItems(c, recipe)) {
            return 1;
        }
        
        if(c.getPlayer().getMeso() < recipe.getCost()) {
            return 2;
        }
        
        if(c.getPlayer().getLevel() < recipe.getReqLevel()) {
            return 3;
        }
        
        if(c.getPlayer().getSkillLevel((c.getPlayer().getJob().getId() / 1000) * 10000000 + 1007) < recipe.getReqSkillLevel()) {
            return 4;
        }
        
        return 0;
    }

    private boolean hasItems(MapleClient c, MakerItemCreateEntry recipe) {
        for (Pair<Integer, Integer> p : recipe.getReqItems()) {
            int itemId = p.getLeft();
            if (c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < p.getRight()) {
                return false;
            }
        }
        return true;
    }
}
