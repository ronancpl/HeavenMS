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

import config.YamlConfig;
import net.MaplePacketHandler;
import net.server.Server;
import tools.BCrypt;
import tools.DatabaseConnection;
import tools.HexTool;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.server.coordinator.session.MapleSessionCoordinator;
import org.apache.mina.core.session.IoSession;

public final class LoginPasswordHandler implements MaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    private static String hashpwSHA512(String pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digester = MessageDigest.getInstance("SHA-512");
        digester.update(pwd.getBytes("UTF-8"), 0, pwd.length());
        return HexTool.toString(digester.digest()).replace(" ", "").toLowerCase();
    }

    private static String getRemoteIp(IoSession session) {
        return MapleSessionCoordinator.getSessionRemoteAddress(session);
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String remoteHost = getRemoteIp(c.getSession());
        if (!remoteHost.contentEquals("null")) {
            if (YamlConfig.config.server.USE_IP_VALIDATION) {    // thanks Alex-0000 (CanIGetaPR) for suggesting IP validation as a server flag
                if (remoteHost.startsWith("127.")) {
                    if (!YamlConfig.config.server.LOCALSERVER) { // thanks Mills for noting HOST can also have a field named "localhost"
                        c.announce(MaplePacketCreator.getLoginFailed(13));  // cannot login as localhost if it's not a local server
                        return;
                    }
                } else {
                    if (YamlConfig.config.server.LOCALSERVER) {
                        c.announce(MaplePacketCreator.getLoginFailed(13));  // cannot login as non-localhost if it's a local server
                        return;
                    }
                }
            }
        } else {
            c.announce(MaplePacketCreator.getLoginFailed(14));          // thanks Alchemist for noting remoteHost could be null
            return;
        }
        
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        c.setAccountName(login);
        
        slea.skip(6);   // localhost masked the initial part with zeroes...
        byte[] hwidNibbles = slea.read(4);
        String nibbleHwid = HexTool.toCompressedString(hwidNibbles);
        int loginok = c.login(login, pwd, nibbleHwid);
        
        Connection con = null;
        PreparedStatement ps = null;

        if (YamlConfig.config.server.AUTOMATIC_REGISTER && loginok == 5) {
            try {
                con = DatabaseConnection.getConnection();
                ps = con.prepareStatement("INSERT INTO accounts (name, password, birthday, tempban) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS); //Jayd: Added birthday, tempban
                ps.setString(1, login);
                ps.setString(2, YamlConfig.config.server.BCRYPT_MIGRATION ? BCrypt.hashpw(pwd, BCrypt.gensalt(12)) : hashpwSHA512(pwd));
                ps.setString(3, "2018-06-20"); //Jayd's idea: was added to solve the MySQL 5.7 strict checking (birthday)
                ps.setString(4, "2018-06-20"); //Jayd's idea: was added to solve the MySQL 5.7 strict checking (tempban)
                ps.executeUpdate();
                
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                c.setAccID(rs.getInt(1));
                rs.close();
            } catch (SQLException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
                c.setAccID(-1);
                e.printStackTrace();
            } finally {
                disposeSql(con, ps);
                loginok = c.login(login, pwd, nibbleHwid);
            }
        }

        if (YamlConfig.config.server.BCRYPT_MIGRATION && (loginok <= -10)) { // -10 means migration to bcrypt, -23 means TOS wasn't accepted
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
        Calendar tempban = c.getTempBanCalendarFromDB();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
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
            c.checkChar(c.getAccID());
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
