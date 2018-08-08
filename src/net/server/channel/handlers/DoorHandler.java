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
import net.AbstractMaplePacketHandler;
import server.maps.MapleDoorObject;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class DoorHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int ownerid = slea.readInt();
        slea.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town
        
        MapleCharacter chr = c.getPlayer();
        if (chr.isChangingMaps() || chr.isBanned()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        for (MapleMapObject obj : chr.getMap().getMapObjects()) {
            if (obj instanceof MapleDoorObject) {
                MapleDoorObject door = (MapleDoorObject) obj;
                if (door.getOwnerId() == ownerid) {
                    door.warp(chr);
                    return;
                }
            }
        }
        
        c.announce(MaplePacketCreator.blockedMessage(6));
        c.announce(MaplePacketCreator.enableActions());
    }
}
