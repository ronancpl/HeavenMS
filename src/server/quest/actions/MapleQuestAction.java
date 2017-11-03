/*
	This file is part of the MapleSolaxia Maple Story Server

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
package server.quest.actions;

import client.MapleCharacter;
import client.MapleQuestStatus;
import provider.MapleData;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Tyler (Twdtwd)
 */
public abstract class MapleQuestAction {
	private final MapleQuestActionType type;
	protected int questID;
	
	public MapleQuestAction(MapleQuestActionType action, MapleQuest quest) {
		this.type = action;
		this.questID = quest.getId();
	}
	
	public abstract void run(MapleCharacter chr, Integer extSelection);
	public abstract void processData(MapleData data);
	
	
	public boolean check(MapleCharacter chr, Integer extSelection) {
		MapleQuestStatus status = chr.getQuest(MapleQuest.getInstance(questID));
		return !(status.getStatus() == MapleQuestStatus.Status.NOT_STARTED && status.getForfeited() > 0);
	}
	
	public MapleQuestActionType getType() {
                return type;
        }
        
        public static List<Integer> getJobBy5ByteEncoding(int encoded) {
                List<Integer> ret = new ArrayList<Integer>();
                if ((encoded & 0x1) != 0) {
                    ret.add(0);
                }
                if ((encoded & 0x2) != 0) {
                    ret.add(100);
                }
                if ((encoded & 0x4) != 0) {
                    ret.add(200);
                }
                if ((encoded & 0x8) != 0) {
                    ret.add(300);
                }
                if ((encoded & 0x10) != 0) {
                    ret.add(400);
                }
                if ((encoded & 0x20) != 0) {
                    ret.add(500);
                }
                if ((encoded & 0x400) != 0) {
                    ret.add(1000);
                }
                if ((encoded & 0x800) != 0) {
                    ret.add(1100);
                }
                if ((encoded & 0x1000) != 0) {
                    ret.add(1200);
                }
                if ((encoded & 0x2000) != 0) {
                    ret.add(1300);
                }
                if ((encoded & 0x4000) != 0) {
                    ret.add(1400);
                }
                if ((encoded & 0x8000) != 0) {
                    ret.add(1500);
                }
                if ((encoded & 0x20000) != 0) {
                    ret.add(2001); //im not sure of this one
                    ret.add(2200);
                }
                if ((encoded & 0x100000) != 0) {
                    ret.add(2000);
                    ret.add(2001); //?
                }
                if ((encoded & 0x200000) != 0) {
                    ret.add(2100);
                }
                if ((encoded & 0x400000) != 0) {
                    ret.add(2001); //?
                    ret.add(2200);
                }

                if ((encoded & 0x40000000) != 0) { //i haven't seen any higher than this o.o
                    ret.add(3000);
                    ret.add(3200);
                    ret.add(3300);
                    ret.add(3500);
                }
                return ret;
        }

        public static List<Integer> getJobBySimpleEncoding(int encoded) {
                List<Integer> ret = new ArrayList<Integer>();
                if ((encoded & 0x1) != 0) {
                    ret.add(200);
                }
                if ((encoded & 0x2) != 0) {
                    ret.add(300);
                }
                if ((encoded & 0x4) != 0) {
                    ret.add(400);
                }
                if ((encoded & 0x8) != 0) {
                    ret.add(500);
                }
                return ret;
        }
}
