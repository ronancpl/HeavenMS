/*
        This file is part of the OdinMS Maple Story Server
        Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
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

/* Bowman Job Instructor
Hunter Job Advancement
Warning Street : The Road to the Dungeon (106010000)
*/

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if (status == 0) {
                        if (cm.isQuestCompleted(100001)) {
                            cm.sendOk("You're truly a hero!");
                            cm.dispose();
                        } else if(cm.isQuestCompleted(100000)) {
                            cm.sendNext("Alright I'll let you in! Defeat the monsters inside, collect 30 Dark Marbles, then strike up a conversation with a colleague of mine inside. He'll give you #bThe Proof of a Hero#k, the proof that you've passed the test. Best of luck to you.");
                            status = 3;
                        } else if (cm.isQuestStarted(100000)) {
                            cm.sendNext("Oh, isn't this a letter from #bAthena#k?");
                        } else {
                            cm.sendOk("I can show you the way once your ready for it.");
                            cm.dispose();
                        }
                }
                
                else if (status == 1)
                    cm.sendNextPrev("So you want to prove your skills? Very well...");
                else if (status == 2)
                    cm.sendAcceptDecline("I will give you a chance if you're ready.");
                else if (status == 3) {
                    cm.completeQuest(100000);
                    cm.startQuest(100001);
                    cm.gainItem(4031010, -1);
                    cm.sendOk("You will have to collect me #b30 #t4031013##k. Good luck.")
                } else if (status == 4) {
                    cm.warp(108000100);
                    cm.dispose();
                }
                else {
                    cm.dispose();
                }
        }
}