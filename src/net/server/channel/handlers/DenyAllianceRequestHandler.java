/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
import net.AbstractMaplePacketHandler;
import net.server.guild.MapleAlliance;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Ronan
 */
public final class DenyAllianceRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        String inviterName = slea.readMapleAsciiString();
        String guildName = slea.readMapleAsciiString();
        
        MapleCharacter chr = c.getWorldServer().getPlayerStorage().getCharacterByName(inviterName);
        if (chr != null) {
            MapleAlliance alliance = chr.getAlliance();
            if (alliance != null) {
                MapleAlliance.answerInvitation(c.getPlayer().getId(), guildName, alliance.getId(), false);
            }
        }
    }
}