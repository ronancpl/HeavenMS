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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ViewCharHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            short charsNum;
            List<Integer> worlds;
            List<MapleCharacter> chars;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT world, id FROM characters WHERE accountid = ?")) {
                ps.setInt(1, c.getAccID());
                charsNum = 0;
                worlds = new ArrayList<>();
                chars = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int cworld = rs.getByte("world");
                        boolean inside = false;
                        for (int w : worlds) {
                            if (w == cworld) {
                                inside = true;
                            }
                        }
                        if (!inside) {
                            worlds.add(cworld);
                        }
                        MapleCharacter chr = MapleCharacter.loadCharFromDB(rs.getInt("id"), c, false);
                        chars.add(chr);
                        charsNum++;
                    }
                }
            }
            int unk = charsNum + 3 - charsNum % 3;
            c.announce(MaplePacketCreator.showAllCharacter(charsNum, unk));
            for (Iterator<Integer> it = worlds.iterator(); it.hasNext();) {
                int w = it.next();
                List<MapleCharacter> chrsinworld = new ArrayList<>();
                for (MapleCharacter chr : chars) {
                    if (chr.getWorld() == w) {
                        chrsinworld.add(chr);
                    }
                }
                c.announce(MaplePacketCreator.showAllCharacterInfo(w, chrsinworld));
            }
        } catch (Exception e) {
        }
    }
}
