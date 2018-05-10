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
package server.quest;

/**
 *
 * @author Matze
 */
public enum MapleQuestRequirementType {
    UNDEFINED(-1), JOB(0), ITEM(1), QUEST(2), MIN_LEVEL(3), MAX_LEVEL(4), END_DATE(5), MOB(6), NPC(7), FIELD_ENTER(8), INTERVAL(9), SCRIPT(10), PET(11), MIN_PET_TAMENESS(12), MONSTER_BOOK(13), NORMAL_AUTO_START(14), INFO_NUMBER(15), INFO_EX(16), COMPLETED_QUEST(17), START(18), END(19), DAY_BY_DAY(20), MESO(21), BUFF(22), EXCEPT_BUFF(23);
    final byte type;

    private MapleQuestRequirementType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public static MapleQuestRequirementType getByWZName(String name) {
        if (name.equals("job")) {
            return JOB;
        } else if (name.equals("quest")) {
            return QUEST;
        } else if (name.equals("item")) {
            return ITEM;
        } else if (name.equals("lvmin")) {
            return MIN_LEVEL;
        } else if (name.equals("lvmax")) {
            return MAX_LEVEL;
        } else if (name.equals("end")) {
            return END_DATE;
        } else if (name.equals("mob")) {
            return MOB;
        } else if (name.equals("npc")) {
            return NPC;
        } else if (name.equals("fieldEnter")) {
            return FIELD_ENTER;
        } else if (name.equals("interval")) {
            return INTERVAL;
        } else if (name.equals("startscript")) {
            return SCRIPT;
        } else if (name.equals("endscript")) {
            return SCRIPT;
        } else if (name.equals("pet")) {
            return PET;
        } else if (name.equals("pettamenessmin")) {
            return MIN_PET_TAMENESS;
        } else if (name.equals("mbmin")) {
            return MONSTER_BOOK;
        } else if (name.equals("normalAutoStart")) {
            return NORMAL_AUTO_START;
        } else if (name.equals("infoNumber")) {
            return INFO_NUMBER;
        } else if (name.equals("infoex")) {
            return INFO_EX;
        } else if (name.equals("questComplete")) {
            return COMPLETED_QUEST;
	} else if(name.equals("start")) {
            return START;
	/*} else if(name.equals("end")) {   already coded
            return END;*/
	} else if(name.equals("daybyday")) {
            return DAY_BY_DAY;
        } else if (name.equals("money")) {
            return MESO;
        } else if (name.equals("buff")) {
            return BUFF;
        } else if (name.equals("exceptbuff")) {
            return EXCEPT_BUFF;
        } else {
            return UNDEFINED;
        }
    }
}
