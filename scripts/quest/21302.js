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
importPackage(Packages.client);
importPackage(Packages.constants);

var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode != 1){
        if (mode == 0 && type == 1)
            qm.sendNext("Hey! At least say you tried!");
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendNext("Wait.. Isn't that.. Did you remember how to make Red Jade?\r\nWow... you may be stupid and prone to amnesia, but this is why I can't abandon you. Now give me the jade!"); //Giant Polearm
    } else if (status == 1) {
        qm.sendNextPrev("Okay, now that I have the Red Jade back on, let me work on reawakening more of your abilities. I mean, your level's gone much higher since the last time we met, so I am sure I can work my magic a bit more this time!");
    } else if (status == 2) {
        if(!qm.isQuestCompleted(21302)) {
            if(!qm.canHold(1142131)) {
                cm.sendOk("Wow, your #bequip#k inventory is full. I need you to make at least 1 empty slot to complete this quest.");
                qm.dispose();
                return;
            }
            
            if(qm.haveItem(4032312, 1)) {
                qm.gainItem(4032312, -1);
            }
            
            qm.gainItem(1142131, true);
            qm.changeJobById(2111);
            
            if (ServerConstants.USE_FULL_ARAN_SKILLSET) {
                qm.teachSkill(21110002, 0, 20, -1);   //full swing
            }

            qm.completeQuest();
        }
        
        qm.sendNext("Come on, keep training so you can get all your abilities back, and that way we can explore together once more!");    
        qm.dispose();
    }
}