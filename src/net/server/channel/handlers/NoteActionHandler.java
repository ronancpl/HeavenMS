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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import java.sql.Connection;

public final class NoteActionHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        if (action == 0 && c.getPlayer().getCashShop().getAvailableNotes() > 0) {
            String charname = slea.readMapleAsciiString();
            String message = slea.readMapleAsciiString();
            try {
                if (c.getPlayer().getCashShop().isOpened())
                    c.announce(MaplePacketCreator.showCashInventory(c));
                
                    c.getPlayer().sendNote(charname, message, (byte) 1);
                    c.getPlayer().getCashShop().decreaseNotes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (action == 1) {
            int num = slea.readByte();
            slea.readByte();
            slea.readByte();
            int fame = 0;
            for (int i = 0; i < num; i++) {
                int id = slea.readInt();
                slea.readByte(); //Fame, but we read it from the database :)
                PreparedStatement ps;
                try {
                    Connection con = DatabaseConnection.getConnection();
                    ps = con.prepareStatement("SELECT `fame` FROM notes WHERE id=? AND deleted=0");
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next())
                            fame += rs.getInt("fame");
                    rs.close();

                    ps = con.prepareStatement("UPDATE notes SET `deleted` = 1 WHERE id = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (fame > 0) {
                c.getPlayer().gainFame(fame);
            }
        }
    }
}
