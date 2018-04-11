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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import tools.DatabaseConnection;

/**
 *
 * @author Jay Estrella :3 (Mr.Trash)
 */
public class MapleFamily {
	private static int id;
	private static Map<Integer, MapleFamilyEntry> members = new HashMap<Integer, MapleFamilyEntry>();

	public MapleFamily(int cid) {
		try {
                        Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT familyid FROM family_character WHERE cid = ?");
			ps.setInt(1, cid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				id = rs.getInt("familyid");
			}
			ps.close();
			rs.close();
                        con.close();
			getMapleFamily();
		} catch (SQLException ex) {
                    ex.printStackTrace();
		}
	}

	private static void getMapleFamily() {
		try {
                        Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM family_character WHERE familyid = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MapleFamilyEntry ret = new MapleFamilyEntry();
				ret.setFamilyId(id);
				ret.setRank(rs.getInt("rank"));
				ret.setReputation(rs.getInt("reputation"));
				ret.setTotalJuniors(rs.getInt("totaljuniors"));
				ret.setFamilyName(rs.getString("name"));
				ret.setJuniors(rs.getInt("juniorsadded"));
				ret.setTodaysRep(rs.getInt("todaysrep"));
				int cid = rs.getInt("cid");
				ret.setChrId(cid);
				members.put(cid, ret);
			}
			rs.close();
			ps.close();
                        con.close();
		} catch (SQLException sqle) {
                    sqle.printStackTrace();
		}
	}

	public MapleFamilyEntry getMember(int cid) {
		if (members.containsKey(cid)){
			return members.get(cid);
		}
		return null;
	}

	public Map<Integer, MapleFamilyEntry> getMembers() {
		return members;
	}
        
        public void broadcast(byte[] packet) {
                // family currently not developed
        }
}
