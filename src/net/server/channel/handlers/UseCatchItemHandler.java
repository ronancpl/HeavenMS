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
import client.inventory.MapleInventoryType;
import client.autoban.AutobanManager;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public final class UseCatchItemHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();
        slea.readInt();
        abm.setTimestamp(5, Server.getInstance().getCurrentTimestamp(), 4);
        slea.readShort();
        int itemId = slea.readInt();
        int monsterid = slea.readInt();

        int itemGanho = MapleItemInformationProvider.getInstance().getCreatItem(itemId);
        int mobItem = MapleItemInformationProvider.getInstance().getMobItem(itemId);
        int timeCatch = MapleItemInformationProvider.getInstance().getUseDelay(itemId);
        int mobHp = MapleItemInformationProvider.getInstance().getMobHP(itemId);

        MapleMonster mob = chr.getMap().getMonsterByOid(monsterid);
        if (chr.getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId) <= 0) {
            return;
        }
        if (mob == null) {
            return;
        }

        if (itemGanho != 0 && mobItem == mob.getId()) {
            if (timeCatch != 0 && (abm.getLastSpam(10) + timeCatch) < currentServerTime()) {
                if (mobHp != 0 && mob.getHp() < ((mob.getMaxHp() / 100) * mobHp)) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.catchMonster(monsterid, itemId, (byte) 1));
                    mob.getMap().killMonster(mob, null, false);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                    MapleInventoryManipulator.addById(c, itemGanho, (short) 1, "", -1);
                    if (itemGanho == 4031868) {
                        chr.updateAriantScore();
                    }
                } else if (mobHp != 0 && mob.getId() != 9500336) {
                    abm.spam(10);
                    c.announce(MaplePacketCreator.catchMessage(0));
                } else if (mob.getId() == 9500336) {
                    chr.message("You cannot use the Fishing Net yet.");
                }
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }
}
