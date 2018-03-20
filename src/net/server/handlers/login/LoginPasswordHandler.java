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
import java.sql.SQLException;
import java.util.Calendar;

import constants.ServerConstants;
import net.MaplePacketHandler;
import net.server.Server;
import tools.BCrypt;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

public final class LoginPasswordHandler implements MaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }


    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        c.setAccountName(login);

        int loginok = c.login(login, pwd);

        Connection con = null;
        PreparedStatement ps = null;

        if (ServerConstants.AUTOMATIC_REGISTER && loginok == 5) {
            try {
                con = DatabaseConnection.getConnection();
                ps = con.prepareStatement("INSERT INTO accounts (name, password) VALUES (?, ?);");
                ps.setString(1, login);
                ps.setString(2, BCrypt.hashpw(pwd, BCrypt.gensalt(12)));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                disposeSql(con, ps);
                loginok = c.login(login, pwd);
            }
        }

        if (ServerConstants.BCRYPT_MIGRATION && (loginok <= -10)) { // -10 means migration to bcrypt, -23 means TOS wasn't accepted
            try {
                con = DatabaseConnection.getConnection();
                ps = con.prepareStatement("UPDATE accounts SET password = ? WHERE name = ?;");
                ps.setString(1, BCrypt.hashpw(pwd, BCrypt.gensalt(12)));
                ps.setString(2, login);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                disposeSql(con, ps);
                loginok = (loginok == -10) ? 0 : 23;
            }
        }

        if (c.hasBannedIP() || c.hasBannedMac()) {
            c.announce(MaplePacketCreator.getLoginFailed(3));
            return;
        }
        Calendar tempban = c.getTempBanCalendar();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > System.currentTimeMillis()) {
                c.announce(MaplePacketCreator.getTempBan(tempban.getTimeInMillis(), c.getGReason()));
                return;
            }
        }
        if (loginok == 3) {
            c.announce(MaplePacketCreator.getPermBan(c.getGReason()));//crashes but idc :D
            return;
        } else if (loginok != 0) {
            c.announce(MaplePacketCreator.getLoginFailed(loginok));
            return;
        }
        if (c.finishLogin() == 0) {
            login(c);
        } else {
            c.announce(MaplePacketCreator.getLoginFailed(7));
        }
    }

    private static void login(MapleClient c){
        c.announce(MaplePacketCreator.getAuthSuccess(c));//why the fk did I do c.getAccountName()?

        Server.getInstance().registerLoginState(c);
    }

    private static void disposeSql(Connection con, PreparedStatement ps) {
        try {
            if (con != null) {
                con.close();
            }

            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
