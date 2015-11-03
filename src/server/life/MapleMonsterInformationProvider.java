/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.life;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.DatabaseConnection;
import tools.Pair;

public class MapleMonsterInformationProvider {
	// Author : LightPepsi

	private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
	private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<>();
	private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();

	protected MapleMonsterInformationProvider() {
		retrieveGlobal();
	}

	public static MapleMonsterInformationProvider getInstance() {
		return instance;
	}

	public final List<MonsterGlobalDropEntry> getGlobalDrop() {
		return globaldrops;
	}

	private void retrieveGlobal() {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			final Connection con = DatabaseConnection.getConnection();
			ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
			rs = ps.executeQuery();

			while (rs.next()) {
				globaldrops.add(
						new MonsterGlobalDropEntry(
								rs.getInt("itemid"),
								rs.getInt("chance"),
								rs.getInt("continent"),
								rs.getByte("dropType"),
								rs.getInt("minimum_quantity"),
								rs.getInt("maximum_quantity"),
								rs.getShort("questid")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			System.err.println("Error retrieving drop" + e);
		} finally {
			try {
				if (ps != null) { 
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignore) {
			}
		}
	}

	public final List<MonsterDropEntry> retrieveDrop(final int monsterId) {
		if (drops.containsKey(monsterId)) {
			return drops.get(monsterId);
		}
		final List<MonsterDropEntry> ret = new LinkedList<>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
			ps.setInt(1, monsterId);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(
						new MonsterDropEntry(
								rs.getInt("itemid"),
								rs.getInt("chance"),
								rs.getInt("minimum_quantity"),
								rs.getInt("maximum_quantity"),
								rs.getShort("questid")));
			}
		} catch (SQLException e) {
			return ret;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignore) {
				return ret;
			}
		}
		drops.put(monsterId, ret);
		return ret;
	}

	public static ArrayList<Pair<Integer, String>> getMobsIDsFromName(String search)
	{
		MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
		ArrayList<Pair<Integer, String>> retMobs = new ArrayList<Pair<Integer, String>>();
		MapleData data = dataProvider.getData("Mob.img");
		List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
		for (MapleData mobIdData : data.getChildren()) {
			int mobIdFromData = Integer.parseInt(mobIdData.getName());
			String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
			mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
		}
		for (Pair<Integer, String> mobPair : mobPairList) {
			if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
				retMobs.add(mobPair);
			}
		}
		return retMobs;
	}

	public static String getMobNameFromID(int id)
	{
		try
		{
			return MapleLifeFactory.getMonster(id).getName();
		} catch (Exception e)
		{
			return null; //nonexistant mob
		}
	}

	public final void clearDrops() {
		drops.clear();
		globaldrops.clear();
		retrieveGlobal();
	}
}