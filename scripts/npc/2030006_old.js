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
status = -1;
//Need more questions.
quest = ["Which of these NPC's will you NOT see at Ellinia of Victoria Island#b\r\n#L0#Shane\r\n#L1#Francois\r\n#L2#Grendel the Really Old\r\n#L3#Arwen the Fairy\r\n#L4#Roel", "Which of these monsters will you NOT be facing at Ossyria...?#b\r\n#L0#White Fang\r\n#L1#Croco\r\n#L2#Yeti\r\n#L3#Lycanthrope\r\n#L4#Luster Pixie", "Which of these monsters have the highest level...?#b\r\n#L0#Octopus\r\n#L1#Ribbon Pig\r\n#L2#Green Mushroom\r\n#L3#Axe Stump\r\n#L4#Bubbling", "In MapleStory, which of these pairings of potion/results doesn't match...?#b\r\n#L0#Holy Water - Recover from the state of being cursed or sealed up.\r\n#L1#Sunrise Dew - Recover MP 3000\r\n#L2#Hamburger - Recover HP 400\r\n#L3#Salad - Recover MP 200\r\n#L4#Blue Potion - Recover MP 100", "Which of these NPC's have NOTHING to do with pets...?#b\r\n#L0#Cloy\r\n#L1#Mar the Fairy\r\n#L2#Trainer Frod\r\n#L3#Vicious\r\n#L4#Doofus"];
ans = [4, 1, 3, 1, 3];
rand = parseInt(Math.random() * quest.length);

function start() {
    if (cm.getPlayer().gotPartyQuestItem("JBQ") && !cm.haveItem(4031058))
        if (cm.haveItem(4005004)) {
            if(!cm.canHold(4031058)) {
                cm.sendNext("Have a free ETC slot available before accepting this trial.");
            } else {
                cm.sendNext("Alright... I'll be testing out your wisdom here. Answer all the questions correctly, and you will pass the test BUT, if you even lie to me once, then you'll have to start over again ok, here we go.");
                return;
            }
        } else
            cm.sendNext("Bring me a #b#t4005004##k to proceed with the questions.");
    cm.dispose();
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.gainItem(4005004, -1);
    if (status > 0)
        if (selection != ans[rand]) {
            cm.sendNext("You have failed the question.");
            cm.dispose();
            return;
        }
    while (quest[rand].equals("") && status <= 4)
        rand = parseInt(Math.random() * quest.length);
    if (status <= 4) {
        cm.sendSimple("Here's the " + (status + 1) + (status == 0 ? "st" : status == 1 ? "nd" : status == 2 ? "rd" : "th") + " question. " + quest[rand]);
        quest[rand] = "";
    } else {
        cm.sendOk("Alright. All your answers have been proven as the truth. Your wisdom has been proven.\r\nTake this necklace and go back.");
        cm.gainItem(4031058, 1);
        cm.dispose();
    }
}