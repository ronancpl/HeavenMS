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

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1){
        if (mode == 0 && type == 1)
            qm.sendNext("Do you not want to put in the work to get the ultimate weapon?");
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendNext("Hmm.. What's a young person like you doing in this secluded place?");
    } else if(status == 1) {
        qm.sendNextPrev("I've come to get the best Polearm there is!", 2);
    } else if(status == 2) {
        qm.sendNextPrev("The best Polearm? You should be able to purchase it in some town or other place..");
    } else if(status == 3) {
        qm.sendNextPrev("I hear you are the best blacksmith in all of Maple World! I want nothing less than a weapon made by you!", 2);
    } else if(status == 4) {
        qm.sendAcceptDecline("I'm too old to make weapons now, but.. I do have a Polearm that I made way back when. It's still in excellent shape. But I can't give it to you because that Polearm is extremely sharp, so sharp it could hurt its master. Do you still want it?");
    } else if(status == 5) {
        qm.sendOk("Well, if you say so.. I can't object to that. I'll tell you what. I'll give you a quick test, and if you pass it, the Giant Polearm is yours. Head over to the #bTraining Center#k and take on the #rScarred Bears#k that are there. Your job is to bring back #b30 Sign of Acceptances#k.");
    } else {
        qm.startQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1){
        if (mode == 0 && type == 1)
            qm.sendNext("Hm? Are you hesitant to take it now after all that? Well, give it more thought if you'd like. It'll be yours in the end anyways.");
        qm.dispose();
        return;
    }
    if (status == 0) {
        if(qm.haveItem(4032311, 30))
            qm.sendNext("Oh, have you brought me the #t4032311#? You're stronger than I thought! But more importantly, I am impressed with the amount of courage you displayed when you agreed to take this dangerous weapon without any hesitation. You deserve it. The #p1201001# is yours.");
        else{
            qm.sendNext("Go for the 30 #t4032311#.");
            qm.dispose();
        }
    }else if (status == 1)
        qm.sendNextPrev("#b(After a long time passed, #p1203000# handed you the #p1201001#, which was carefully wrapped in cloth.)");
    else if (status == 2)
        qm.sendYesNo("Here, this is #p1201002#, the Polearm you've asked for. Please take good care of it.");
    else if (status == 3){
        //qm.showVideo("Polearm");
        qm.completeQuest();
        qm.removeAll(4032311);
        qm.dispose();
    }
}