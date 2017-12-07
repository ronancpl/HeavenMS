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
	NPC Name: 		Parwen
	Description: 		Quest - Verifying the password
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

	if (mode == 1) {
	    status++;
	} else {
	    qm.sendNext("Come on, hurry up. Get your pen and paper out if you're not that smart!");
	    qm.dispose();
	    return;
	}

	if (status == 0) {
	    qm.sendNext("Oh! Finally you have come! I'm glad you are here in time. I have the master key for you to open the secret passage! Hahahaha! Isn't it amazing? Say it amazing!");
	} else if (status == 1) {
	    qm.sendAcceptDecline("All right, now, this key is very long and complex. I need you to memorize it very well. I won't say again, so you'd better write it down somewhere. Are you ready?");
	} else if (status == 2) {
	    var pass = generateString();
	    qm.sendOk("The key code is #b" + pass + "#k. Got that? Put the key into the door of the secret passage, and you will be able to walk around the passage freely.");
	    qm.forceStartQuest();
            qm.setStringQuestProgress(3360, 0, pass);
	    qm.dispose();
	}
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
	if (mode == 1)
	    status++;
	else
	    status--;
	if (status == 0) {
            if(qm.getQuestProgress(3360, 1) == 0) {
                qm.sendNext("What's up? You haven't opened the Secret Passage yet?");
            } else {
                qm.forceCompleteQuest();
            }
            
            qm.dispose();
	}
    }
}

function generateString() {
    var thestring = "";
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var rnum;
    for (var i = 0; i < 10; i++) {
	rnum = Math.floor(Math.random() * chars.length);
	thestring += chars.substring(rnum, rnum+1);
    }
    return thestring;
}
