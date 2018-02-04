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
import java.awt.Point;
import java.util.List;
import server.maps.MapleDragon;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;


public class MoveDragonHandler extends AbstractMovementPacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter chr = c.getPlayer();
        final Point startPos = new Point(slea.readShort(), slea.readShort());
        List<LifeMovementFragment> res = parseMovement(slea);
        final MapleDragon dragon = chr.getDragon();
        if (dragon != null && res != null && res.size() > 0) {
            updatePosition(res, dragon, 0);
            if (chr.isHidden()) {
                chr.getMap().broadcastGMMessage(chr, MaplePacketCreator.moveDragon(dragon, startPos, res));
            } else {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.moveDragon(dragon, startPos, res), dragon.getPosition());
            }
        }
    }
}