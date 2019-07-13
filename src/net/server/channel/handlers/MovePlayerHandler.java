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
import java.util.List;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MovePlayerHandler extends AbstractMovementPacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	long startTime = System.nanoTime();
        slea.skip(9);
        long movementDataStart = slea.getPosition();
        updatePosition(slea, c.getPlayer(), 0);
        long movementDataLength = slea.getPosition() - movementDataStart; //how many bytes were read by updatePosition
        System.out.println("Movement data length : " + movementDataLength);
        System.out.println(c.getPlayer().getName() + " : Movement packet handler parse took " + ((System.nanoTime() - startTime) / 1000000f) + "ms.");
        if (movementDataLength > 0) {
        	slea.seek(movementDataStart);
            System.out.println(c.getPlayer().getName() + " : Movement packet handler updatePos took " + ((System.nanoTime() - startTime) / 1000000f) + "ms.");
            c.getPlayer().getMap().movePlayer(c.getPlayer(), c.getPlayer().getPosition());
            System.out.println(c.getPlayer().getName() + " : Movement packet handler movePlayer took " + ((System.nanoTime() - startTime) / 1000000f) + "ms.");
            if (c.getPlayer().isHidden()) {
                c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), slea, movementDataLength), false);
            } else {
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), slea, movementDataLength), false);
            }
        }
        System.out.println(c.getPlayer().getName() + " : Movement packet handler total took " + ((System.nanoTime() - startTime) / 1000000f) + "ms.");
    }
}
