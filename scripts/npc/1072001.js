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

/* Magician Job Instructor
	Magician 2nd Job Advancement
	Victoria Road : The Forest North of Ellinia (101020000)
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
    
                if(status == 0) {
                        if (cm.isQuestCompleted(100007)) {
                            cm.sendOk("You're truly a hero!");
                            cm.dispose();
                        } else if(cm.isQuestCompleted(100006)) {
                            cm.sendNext("Alright I'll let you in! Defeat the monsters inside, collect 30 Dark Marbles, then strike up a conversation with a colleague of mine inside. He'll give you #bThe Proof of a Hero#k, the proof that you've passed the test. Best of luck to you.");
                            status = 4;
                        } else if (cm.isQuestStarted(100006)) {
                            cm.sendNext("Hmmm...it is definitely the letter from #bGrendell the Really Old#k...so you came all the way here to take the test and make the 2nd job advancement as a magician. Alright, I'll explain the test to you. Don't sweat it too much, it's not that complicated.");
                        } else {
                            cm.sendOk("I can show you the way once your ready for it.");
                            cm.dispose();
                        }
                }
                else if(status == 1)
                        cm.sendNextPrev("I'll send you to a hidden map. You'll see monsters you don't normally see. They look the same like the regular ones, but with a totally different attitude. They neither boost your experience level nor provide you with item.");
                else if (status == 2)
                        cm.sendNextPrev("You'll be able to acquire a marble called #b#t4031013##k while knocking down those monsters. It is a special marble made out of their sinister, evil minds. Collect 30 of those, and then go talk to a colleague of mine in there. That's how you pass the test.");
                else if (status == 3)
                        cm.sendYesNo("Once you go inside, you can't leave until you take care of your mission. If you die, your experience level will decrease.. So you better really buckle up and get ready...well, do you want to go for it now?");
                else if (status == 4) {
                        cm.sendNext("Alright I'll let you in! Defeat the monsters inside, collect 30 Dark Marbles, then strike up a conversation with a colleague of mine inside. He'll give you #bThe Proof of a Hero#k, the proof that you've passed the test. Best of luck to you.");
                        cm.completeQuest(100006);            
                        cm.startQuest(100007);
                        cm.gainItem(4031009, -1);
                }
                else if (status == 5) {
                        cm.warp(108000200, 0);
                        cm.dispose();
                }
                else cm.dispose();
        }
}
