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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import client.MapleFamily;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String pic = slea.readMapleAsciiString();
        int cid = slea.readInt();
        if (c.checkPic(pic)) {
        	//check for family, guild leader, pending marriage, world transfer
        	try (Connection con = DatabaseConnection.getConnection();
        			PreparedStatement ps = con.prepareStatement("SELECT `world`, `guildid`, `guildrank`, `familyId` FROM characters WHERE id = ?");
        			PreparedStatement ps2 = con.prepareStatement("SELECT COUNT(*) as rowcount FROM worldtransfers WHERE `characterid` = ? AND completionTime IS NULL")) {
        		ps.setInt(1, cid);
        		ResultSet rs = ps.executeQuery();
        		if(!rs.next()) throw new SQLException("Character record does not exist.");
        		int world = rs.getInt("world");
        		int guildId = rs.getInt("guildid");
        		int guildRank = rs.getInt("guildrank");
        		int familyId = rs.getInt("familyId");
        		if(guildId != 0 && guildRank <= 1) {
        			c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x16));
        			return;
        		} else if(familyId != -1) {
        			MapleFamily family = Server.getInstance().getWorld(world).getFamily(familyId);
        			if(family != null && family.getTotalMembers() > 1) {
            			c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x1D));
            			return;
        			}
        		}
        		rs.close();
        		ps2.setInt(1, cid);
        		rs = ps2.executeQuery();
        		rs.next();
        		if(rs.getInt("rowcount") > 0) {
        			c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x1A));
        			return;
        		}
        	} catch(SQLException e) {
        		e.printStackTrace();
        		c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x09));
        		return;
        	}
            if(c.deleteCharacter(cid, c.getAccID())) {
                FilePrinter.print(FilePrinter.DELETED_CHAR + c.getAccountName() + ".txt", c.getAccountName() + " deleted CID: " + cid);
                c.announce(MaplePacketCreator.deleteCharResponse(cid, 0));
            } else {
                c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x09));
            }
        } else {
            c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x14));
        }
    }
}
