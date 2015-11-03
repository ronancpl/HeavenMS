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
package server.gachapon;

import tools.Randomizer;

/**
 *
 * @author SharpAceX(Alan)
 */
public class MapleGachapon {

	private static final MapleGachapon instance = new MapleGachapon();
	
	public enum Gachapon {

		GLOBAL(-1, -1, -1, -1, new Global()),
		HENESYS(9100100, 90, 8, 2, new Henesys()),
		ELLINIA(9100101, 90, 8, 2, new Ellinia()),
		PERION(9100102, 90, 8, 2, new Perion()),
		KERNING_CITY(9100103, 90, 8, 2, new KerningCity()),
		SLEEPYWOOD(9100104, 90, 8, 2, new Sleepywood()),
		MUSHROOM_SHRINE(9100105, 90, 8, 2, new MushroomShrine()),
		SHOWA_SPA_MALE(9100106, 90, 8, 2, new ShowaSpaMale()),
		SHOWA_SPA_FEMALE(9100107, 90, 8, 2, new ShowaSpaFemale()),
		NEW_LEAF_CITY(9100109, 90, 8, 2, new NewLeafCity()),
		NAUTILUS_HARBOR(9100117, 90, 8, 2, new NautilusHarbor());

		private GachaponItems gachapon;
		private int npcId;
		private int common;
		private int uncommon;
		private int rare;

		private Gachapon(int npcid, int c, int u, int r, GachaponItems g) {
			npcId = npcid;
			gachapon = g;
			common = c;
			uncommon = u;
			rare = r;
		}

		private int getTier() {
			int chance = Randomizer.nextInt(common + uncommon + rare) + 1;
			if (chance > common + uncommon) {
				return 2; //Rare
			} else if (chance > common) {
				return 1; //Uncommon
			}
			return 0; //Common
		}
		
		public int [] getItems(int tier){
			return gachapon.getItems(tier);
		}
		
		public int getItem(int tier) {
			int[] gacha = getItems(tier);
			int[] global = GLOBAL.getItems(tier);
			int chance = Randomizer.nextInt(gacha.length + global.length);
			return chance < gacha.length ? gacha[chance] : global[chance - gacha.length];
		}

		public static Gachapon getByNpcId(int npcId) {
			for (Gachapon gacha : Gachapon.values()) {
				if (npcId == gacha.npcId) {
					return gacha;
				}
			}
			return null;
		}
	}

	public static MapleGachapon getInstance() {
		return instance;
	}
	
	public MapleGachaponItem process(int npcId) {
		Gachapon gacha = Gachapon.getByNpcId(npcId);
		int tier = gacha.getTier();
		int item = gacha.getItem(tier);
		return new MapleGachaponItem(tier, item);
	}
	
	public class MapleGachaponItem {
		private int id;
		private int tier;
		
		public MapleGachaponItem(int t, int i) {
			id = i;
			tier = t;
		}
		
		public int getTier() {
			return tier;
		}
		
		public int getId() {
			return id;
		}
	}
}
