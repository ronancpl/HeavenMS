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
package scripting.portal;

import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import scripting.AbstractPlayerInteraction;
import server.MaplePortal;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c);
        this.portal = portal;
    }

    public MaplePortal getPortal() {
        return portal;
    }

    public boolean hasLevel30Character() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT `level` FROM `characters` WHERE accountid = ?");
            ps.setInt(1, getPlayer().getAccountID());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("level") >= 30) {
                    ps.close();
                    rs.close();
                    return true;
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        return getPlayer().getLevel() >= 30;
    }
    
    public boolean forceStartQuest(int id) {
            return forceStartQuest(id, 9010000);
    }

    public boolean forceStartQuest(int id, int npc) {
            return MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
    }
    
    public boolean forceCompleteQuest(int id) {
            return forceCompleteQuest(id, 9010000);
    }

    public boolean forceCompleteQuest(int id, int npc) {
            return MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
    }
    
    public void blockPortal() {
        c.getPlayer().blockPortal(getPortal().getScriptName());
    }

    public void unblockPortal() {
        c.getPlayer().unblockPortal(getPortal().getScriptName());
    }

    public void playPortalSound() {
        c.announce(MaplePacketCreator.playPortalSound());
    }
}