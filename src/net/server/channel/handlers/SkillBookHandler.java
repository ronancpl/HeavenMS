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
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.util.Map;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SkillBookHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        slea.readInt();
        short slot = (short) slea.readShort();
        int itemId = slea.readInt();
        
        boolean canuse;
        boolean success = false;
        int skill = 0;
        int maxlevel = 0;
        
        MapleCharacter player = c.getPlayer();
        if (c.tryacquireClient()) {
            try {
                MapleInventory inv = c.getPlayer().getInventory(MapleInventoryType.USE);
                Item toUse = inv.getItem(slot);
                if (toUse == null || toUse.getItemId() != itemId) {
                    return;
                }
                Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getSkillStats(toUse.getItemId(), c.getPlayer().getJob().getId());
                if (skilldata == null) {
                    return;
                }
                Skill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
                if (skilldata.get("skillid") == 0) {
                    canuse = false;
                } else if ((player.getSkillLevel(skill2) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) && player.getMasterLevel(skill2) < skilldata.get("masterLevel")) {
                    inv.lockInventory();
                    try {
                        Item used = inv.getItem(slot);
                        if (used != toUse || toUse.getQuantity() < 1) {    // thanks ClouD for noticing skillbooks not being usable when stacked
                            return;
                        }

                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                    } finally {
                        inv.unlockInventory();
                    }

                    canuse = true;
                    if (MapleItemInformationProvider.rollSuccessChance(skilldata.get("success"))) {
                        success = true;
                        player.changeSkillLevel(skill2, player.getSkillLevel(skill2), Math.max(skilldata.get("masterLevel"), player.getMasterLevel(skill2)), -1);
                    } else {
                        success = false;
                        //player.dropMessage("The skill book lights up, but the skill winds up as if nothing happened.");
                    }
                } else {
                    canuse = false;
                }
            } finally {
                c.releaseClient();
            }
            
            // thanks Vcoc for noting skill book result not showing for all in area
            player.getMap().broadcastMessage(MaplePacketCreator.skillBookResult(player, skill, maxlevel, canuse, success));
        }
    }
}
