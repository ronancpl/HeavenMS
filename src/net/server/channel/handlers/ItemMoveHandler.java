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
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class ItemMoveHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(4);
        if(c.getPlayer().getAutobanManager().getLastSpam(6) + 300 > currentServerTime()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        short src = slea.readShort();     //is there any reason to use byte instead of short in src and action?
        short action = slea.readShort();
        short quantity = slea.readShort();
        
        if (src < 0 && action > 0) {
            MapleInventoryManipulator.unequip(c, src, action);
        } else if (action < 0) {
            MapleInventoryManipulator.equip(c, src, action);
        } else if (action == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, action);
        }
        
        if (c.getPlayer().getMap().getHPDec() > 0) c.getPlayer().resetHpDecreaseTask();
        c.getPlayer().getAutobanManager().spam(6);
    }
}