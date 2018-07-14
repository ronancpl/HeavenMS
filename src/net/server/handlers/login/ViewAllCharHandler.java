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
import java.util.ArrayList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.Pair;

public final class ViewAllCharHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            List<World> wlist = Server.getInstance().getWorlds();
            List<Pair<Integer, List<MapleCharacter>>> worldChars = new ArrayList<>(wlist.size() + 1);
            
            int chrTotal = 0;
            int accountId = c.getAccID();
            List<MapleCharacter> lastwchars = null;
            for(World w : wlist) {
                List<MapleCharacter> wchars = w.getAccountCharactersView(accountId);
                
                if(!wchars.isEmpty()) {
                    lastwchars = wchars;
                    
                    worldChars.add(new Pair<>(w.getId(), wchars));
                    chrTotal += wchars.size();
                }
            }
            
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
