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

import constants.ServerConstants;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.MapleInviteCoordinator;
import net.server.coordinator.MapleInviteCoordinator.InviteType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 * @author Ubaware
 */
public final class FamilyAddHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(!ServerConstants.USE_FAMILY_SYSTEM) {
            return;
        }
        String toAdd = slea.readMapleAsciiString();
        MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
        if(addChr == null) {
            c.announce(MaplePacketCreator.sendFamilyMessage(65, 0));
        } else if(addChr.getMap() != c.getPlayer().getMap() || (addChr.isHidden()) && c.getPlayer().gmLevel() < addChr.gmLevel()) {
            c.announce(MaplePacketCreator.sendFamilyMessage(69, 0));
        } else if(addChr.getLevel() <= 10) {
            c.announce(MaplePacketCreator.sendFamilyMessage(77, 0));
        } else if(Math.abs(addChr.getLevel() - c.getPlayer().getLevel()) > 20) {
            c.announce(MaplePacketCreator.sendFamilyMessage(72, 0));
        } else if(addChr.getFamily() != null && addChr.getFamily() == c.getPlayer().getFamily()) { //same family
            c.announce(MaplePacketCreator.enableActions());
        } else if(addChr.getFamily() != null && addChr.getFamily().getTotalMembers() > 1) {
            c.announce(MaplePacketCreator.sendFamilyMessage(70, 0));
        } else if(MapleInviteCoordinator.hasInvite(InviteType.FAMILY, addChr.getId())) {
            c.announce(MaplePacketCreator.sendFamilyMessage(73, 0));
        } else {
            MapleInviteCoordinator.createInvite(InviteType.FAMILY, c.getPlayer(), addChr, addChr.getId());
            addChr.getClient().announce(MaplePacketCreator.sendFamilyInvite(c.getPlayer().getId(), toAdd));
            c.getPlayer().dropMessage("The invite has been sent.");
            c.announce(MaplePacketCreator.enableActions());
        }
    }
}
