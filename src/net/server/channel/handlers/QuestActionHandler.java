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
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import scripting.quest.QuestScriptManager;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class QuestActionHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte action = slea.readByte();
        short questid = slea.readShort();
        MapleCharacter player = c.getPlayer();
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (action == 1) { //Start Quest
            int npc = slea.readInt();
            if (slea.available() >= 4) {
                slea.readInt();
            }
            quest.start(player, npc);
        } else if (action == 2) { // Complete Quest
            int npc = slea.readInt();
            slea.readInt();
            if (slea.available() >= 2) {
                int selection = slea.readShort();
                quest.complete(player, npc, selection);
            } else {
                quest.complete(player, npc);
            }
        } else if (action == 3) {// forfeit quest
            quest.forfeit(player);
        } else if (action == 4) { // scripted start quest
            int npc = slea.readInt();
            slea.readInt();
			if(quest.canStart(player, npc)) {
				QuestScriptManager.getInstance().start(c, questid, npc);
			}
        } else if (action == 5) { // scripted end quests
            //System.out.println(slea.toString());
            int npc = slea.readInt();
            slea.readInt();
			if(quest.canComplete(player, npc)) {
				QuestScriptManager.getInstance().end(c, questid, npc);
			}
        }
    }
}
