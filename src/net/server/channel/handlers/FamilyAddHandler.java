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

import config.YamlConfig;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
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
        if(!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        String toAdd = slea.readMapleAsciiString();
        MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
        MapleCharacter chr = c.getPlayer();
        if(addChr == null) {
            c.announce(MaplePacketCreator.sendFamilyMessage(65, 0));
        } else if(addChr == chr) { //only possible through packet editing/client editing i think?
            c.announce(MaplePacketCreator.enableActions());
        } else if(addChr.getMap() != chr.getMap() || (addChr.isHidden()) && chr.gmLevel() < addChr.gmLevel()) {
            c.announce(MaplePacketCreator.sendFamilyMessage(69, 0));
        } else if(addChr.getLevel() <= 10) {
            c.announce(MaplePacketCreator.sendFamilyMessage(77, 0));
        } else if(Math.abs(addChr.getLevel() - chr.getLevel()) > 20) {
            c.announce(MaplePacketCreator.sendFamilyMessage(72, 0));
        } else if(addChr.getFamily() != null && addChr.getFamily() == chr.getFamily()) { //same family
            c.announce(MaplePacketCreator.enableActions());
        } else if(MapleInviteCoordinator.hasInvite(InviteType.FAMILY, addChr.getId())) {
            c.announce(MaplePacketCreator.sendFamilyMessage(73, 0));
        } else if(chr.getFamily() != null && addChr.getFamily() != null && addChr.getFamily().getTotalGenerations() + chr.getFamily().getTotalGenerations() > YamlConfig.config.server.FAMILY_MAX_GENERATIONS) {
            c.announce(MaplePacketCreator.sendFamilyMessage(76, 0));
        } else {
            MapleInviteCoordinator.createInvite(InviteType.FAMILY, chr, addChr, addChr.getId());
            addChr.getClient().announce(MaplePacketCreator.sendFamilyInvite(chr.getId(), chr.getName()));
            chr.dropMessage("The invite has been sent.");
            c.announce(MaplePacketCreator.enableActions());
        }
    }
}
