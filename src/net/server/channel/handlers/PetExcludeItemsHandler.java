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
import client.MapleCharacter;
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
//import tools.MaplePacketCreator;

/**
 * @author BubblesDev
 * @author Ronan
 */
public final class PetExcludeItemsHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int petId = slea.readInt();
        slea.skip(4);
        
        MapleCharacter chr = c.getPlayer();
        byte petIndex = (byte)chr.getPetIndex(petId);
        if (petIndex < 0) return;
        
        final MaplePet pet = chr.getPet(petIndex);
        if (pet == null) {
            return;
        }
        
        chr.resetExcluded(petId);
        byte amount = slea.readByte();
        for (int i = 0; i < amount; i++) {
            chr.addExcluded(petId, slea.readInt());
        }
        chr.commitExcludedItems();
    }
}
