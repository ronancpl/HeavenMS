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
package net.server.handlers.login;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.Pair;

public final class ViewAllCharHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            if(!c.canRequestCharlist()) {   // client breaks if the charlist request pops too soon
                c.announce(MaplePacketCreator.showAllCharacter(0, 0));
                return;
            }
            
            int accountId = c.getAccID();
            Pair<Pair<Integer, List<MapleCharacter>>, List<Pair<Integer, List<MapleCharacter>>>> loginBlob = Server.getInstance().loadAccountCharlist(accountId, c.getVisibleWorlds());
            
            List<Pair<Integer, List<MapleCharacter>>> worldChars = loginBlob.getRight();
            int chrTotal = loginBlob.getLeft().getLeft();
            List<MapleCharacter> lastwchars = loginBlob.getLeft().getRight();
            
            if (chrTotal > 9) {
                int padRight = chrTotal % 3;
                if (padRight > 0 && lastwchars != null) {
                    MapleCharacter chr = lastwchars.get(lastwchars.size() - 1);
                    
                    for(int i = padRight; i < 3; i++) { // filling the remaining slots with the last character loaded
                        chrTotal++;
                        lastwchars.add(chr);
                    }
                }
            }
            
            int charsSize = chrTotal;
            int unk = charsSize + (3 - charsSize % 3); //rowSize?
            c.announce(MaplePacketCreator.showAllCharacter(charsSize, unk));
            
            for (Pair<Integer, List<MapleCharacter>> wchars : worldChars) {
                c.announce(MaplePacketCreator.showAllCharacterInfo(wchars.getLeft(), wchars.getRight(), ServerConstants.ENABLE_PIC));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
