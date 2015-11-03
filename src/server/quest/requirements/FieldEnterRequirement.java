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
package server.quest.requirements;

import client.MapleCharacter;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;

/**
 *
 * @author Tyler (Twdtwd)
 */
public class FieldEnterRequirement extends MapleQuestRequirement {
	private int mapId = -1;
	
	
	public FieldEnterRequirement(MapleQuest quest, MapleData data) {
		super(MapleQuestRequirementType.FIELD_ENTER);
		processData(data);
	}
	
	@Override
	public void processData(MapleData data) {
		MapleData zeroField = data.getChildByPath("0");
		if (zeroField != null) {
			 mapId = MapleDataTool.getInt(zeroField);
		}
	}
	
	
	@Override
	public boolean check(MapleCharacter chr, Integer npcid) {
		return mapId == chr.getMapId();
	}
}
