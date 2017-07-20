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
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import server.maps.MapleMapObject;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.ServerConstants;

/**
 * @author TheRamon
 * @author Ronan
 */
public final class PetLootHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if(System.currentTimeMillis() - chr.getPetLootCd() < ServerConstants.PET_LOOT_UPON_ATTACK) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        int petIndex = chr.getPetIndex(slea.readInt());
        MaplePet pet = chr.getPet(petIndex);
        if (pet == null || !pet.isSummoned()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        
        slea.skip(13);
        int oid = slea.readInt();
        MapleMapObject ob = chr.getMap().getMapObject(oid);
        chr.pickupItem(ob, petIndex);
    }
}
