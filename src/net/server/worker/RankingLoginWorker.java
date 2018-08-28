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
package net.server.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import client.MapleJob;
import tools.DatabaseConnection;
import constants.ServerConstants;
import net.server.Server;

/**
 * @author Matze
 * @author Quit
 * @author Ronan
 */
public class RankingLoginWorker implements Runnable {
    private Connection con;
    private long lastUpdate = System.currentTimeMillis();
    
    private void resetMoveRank(boolean job) throws SQLException {
        String query = "UPDATE characters SET " + (job == true ? "jobRankMove = 0" : "rankMove = 0");
        PreparedStatement reset = con.prepareStatement(query);
        reset.executeUpdate();
    }

    private void updateRanking(int job, int world) throws SQLException {
        String sqlCharSelect = "SELECT c.id, " + (job != -1 ? "c.jobRank, c.jobRankMove" : "c.rank, c.rankMove") + ", a.lastlogin AS lastlogin, a.loggedin FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id WHERE c.gm < 2 AND c.world = ? ";
        if (job != -1) {
            sqlCharSelect += "AND c.job DIV 100 = ? ";
        }
        sqlCharSelect += "ORDER BY c.level DESC , c.exp DESC , c.fame DESC , c.meso DESC";
        
        PreparedStatement charSelect = con.prepareStatement(sqlCharSelect);
        charSelect.setInt(1, world);
        if (job != -1) {
            charSelect.setInt(2, job);
        }
        ResultSet rs = charSelect.executeQuery();
        PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (job != -1 ? "jobRank = ?, jobRankMove = ? " : "rank = ?, rankMove = ? ") + "WHERE id = ?");
        int rank = 0;
        
        while (rs.next()) {
            int rankMove = 0;
            rank++;
            if (rs.getLong("lastlogin") < lastUpdate || rs.getInt("loggedin") > 0) {
                rankMove = rs.getInt((job != -1 ? "jobRankMove" : "rankMove"));
            }
            rankMove += rs.getInt((job != -1 ? "jobRank" : "rank")) - rank;
            ps.setInt(1, rank);
            ps.setInt(2, rankMove);
            ps.setInt(3, rs.getInt("id"));
            ps.executeUpdate();
        }
        
        rs.close();
        charSelect.close();
        ps.close();
    }
    
    @Override
    public void run() {
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);
            
            if(ServerConstants.USE_REFRESH_RANK_MOVE == true) {
                resetMoveRank(true);
                resetMoveRank(false);
            }
            
            for(int j = 0; j < Server.getInstance().getWorldsSize(); j++) {
                updateRanking(-1, j);    //overall ranking
                for (int i = 0; i <= MapleJob.getMax(); i++) {
                    updateRanking(i, j);
                }
                con.commit();
            }
            
            con.setAutoCommit(true);
            lastUpdate = System.currentTimeMillis();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            
            try {
                con.rollback();
                con.setAutoCommit(true);
                if(!con.isClosed()) con.close();
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
