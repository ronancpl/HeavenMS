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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.sql.Connection;

import client.MapleCharacter;
import client.MapleClient;
import config.YamlConfig;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Ronan
 * @author Ubaware
 */
public final class TransferNameHandler extends AbstractMaplePacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); //cid
        int birthday = slea.readInt();
        if (!CashOperationHandler.checkBirthday(c, birthday)) {
            c.announce(MaplePacketCreator.showCashShopMessage((byte) 0xC4));
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        if(!YamlConfig.config.server.ALLOW_CASHSHOP_NAME_CHANGE) {
            c.announce(MaplePacketCreator.sendNameTransferRules(4));
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if(chr.getLevel() < 10) {
            c.announce(MaplePacketCreator.sendNameTransferRules(4));
            return;
        } else if(c.getTempBanCalendar() != null && c.getTempBanCalendar().getTimeInMillis() + (30*24*60*60*1000) < Calendar.getInstance().getTimeInMillis()) {
            c.announce(MaplePacketCreator.sendNameTransferRules(2));
            return;
        }
        //sql queries
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT completionTime FROM namechanges WHERE characterid=?")) { //double check, just in case
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Timestamp completedTimestamp = rs.getTimestamp("completionTime");
                if(completedTimestamp == null) { //has pending name request
                    c.announce(MaplePacketCreator.sendNameTransferRules(1));
                    return;
                } else if(completedTimestamp.getTime() + YamlConfig.config.server.NAME_CHANGE_COOLDOWN > System.currentTimeMillis()) {
                    c.announce(MaplePacketCreator.sendNameTransferRules(3));
                    return;
                };
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return;
        }
        c.announce(MaplePacketCreator.sendNameTransferRules(0));
    }
}