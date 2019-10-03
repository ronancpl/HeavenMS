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

import java.awt.Point;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.MapleDragon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.exceptions.EmptyMovementException;


public class MoveDragonHandler extends AbstractMovementPacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter chr = c.getPlayer();
        final Point startPos = new Point(slea.readShort(), slea.readShort());
        final MapleDragon dragon = chr.getDragon();
        if (dragon != null) {
            try {
                long movementDataStart = slea.getPosition();
                updatePosition(slea, dragon, 0);
                long movementDataLength = slea.getPosition() - movementDataStart; //how many bytes were read by updatePosition
                slea.seek(movementDataStart);
                
                if (chr.isHidden()) {
                    chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.moveDragon(dragon, startPos, slea, movementDataLength));
                } else {
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.moveDragon(dragon, startPos, slea, movementDataLength), dragon.getPosition());
                }
            } catch (EmptyMovementException e) {}
        }
    }
}