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
/*	
	Author : Generic
	NPC Name: 		Kiku
	Map(s): 		Empress' Road : Training Forest I
	Description: 		Quest - Kiku the Training Instructor
	Quest ID : 		20002
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode > 0)
            status++;
        else
            status--;
        if (status == 0)
            qm.sendNext("What? Neinheart sent you here? Ahh, you must be a rookie. Welcome, welcome. My name is Kiku, and my job is to train and mold kids like you into bonafide knights. Hmmm.. Why are you looking at me like that... Ahh, you must have never seen Piyos before.");
        else if (status == 1)
            qm.sendNext("We belong to a race called Piyos. You've talked to Shinsoo before, right? The one that stands next to the Empress. Yeah, Shinsoo is a Piyo, too. He may be of a different class, but... oh well. Piyos are only found in Ereve, so you may find us a bit odd at first, but you'll get used to us.");
        else if (status == 2)
            qm.sendAcceptDecline("Ah, I don't know if you are aware of this, but you won't find any monsters here in Ereve. Any form of evil will not be able to set foot on this island. Don't worry, you'll still have your opportunity to train here. Shinsoo created a fantasy creature called Mimi, which will be used as your training partners. Shall we begin?");
        else if (status == 3) {
            qm.gainExp(60);
            qm.gainItem(2000020, 10); // Red Potion for Noblesse * 10
            qm.gainItem(2000021, 10); // Blue Potion for Noblesse * 10
            qm.gainItem(1002869, 1);  // Elegant Noblesse Hat * 1
            qm.sendOk("Ha, I like your enthusiasm, but you must prepare yourself for the training first before we start things off. Make sure that you are equipped with weapons, and that your skills are calibrated and ready to be used. I also gave you some potions, so have it ready just in case. Let me know when you're ready. You're going to wish that you didn't sign up to become a Cygnus Knight.");
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}