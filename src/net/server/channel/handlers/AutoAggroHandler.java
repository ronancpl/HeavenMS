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
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;

public final class AutoAggroHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().isHidden()) return; // Don't auto aggro GM's in hide...
        
        MapleMap map = c.getPlayer().getMap();
        int oid = slea.readInt();
        
        MapleMonster monster = map.getMonsterByOid(oid);
        if (monster != null) {
            MapleCharacter currentController = monster.getController();
            monster.lockMonster();
            try {
                if (currentController != null) {
                    if (!monster.isControllerHasAggro()) {
                        if (map.getCharacterById(currentController.getId()) == null) {
                            monster.switchController(c.getPlayer(), true);
                        } else {
                            monster.switchController(currentController, true);
                        }
                    } else if (map.getCharacterById(currentController.getId()) == null) {
                        monster.switchController(c.getPlayer(), true);
                    }
                } else {
                    monster.switchController(c.getPlayer(), true);
                }
            } finally {
                monster.unlockMonster();
            }
        }
    }
}
