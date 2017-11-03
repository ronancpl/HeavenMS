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
package server.quest.requirements;

import java.util.HashMap;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import client.MapleCharacter;
import client.MapleQuestStatus;

/**
 *
 * @author Tyler (Twdtwd)
 */
public class QuestRequirement extends MapleQuestRequirement {
	Map<Integer, Integer> quests = new HashMap<>();
	
	public QuestRequirement(MapleQuest quest, MapleData data) {
		super(MapleQuestRequirementType.QUEST);
		processData(data);
	}
	
	/**
	 * 
	 * @param data 
	 */
	@Override
	public void processData(MapleData data) {
		for (MapleData questEntry : data.getChildren()) {
			int questID = MapleDataTool.getInt(questEntry.getChildByPath("id"));
			int stateReq = MapleDataTool.getInt(questEntry.getChildByPath("state"));
			quests.put(questID, stateReq);
		}
	}
	
	
	@Override
	public boolean check(MapleCharacter chr, Integer npcid) {
		for(Integer questID : quests.keySet()) {
			int stateReq = quests.get(questID);
			MapleQuestStatus q = chr.getQuest(MapleQuest.getInstance(questID));
			
			if(q == null && MapleQuestStatus.Status.getById(stateReq).equals(MapleQuestStatus.Status.NOT_STARTED))
				continue;
			
			if(q == null || !q.getStatus().equals(MapleQuestStatus.Status.getById(stateReq))) {
				return false;
			}
			
		}
		return true;
	}
}
