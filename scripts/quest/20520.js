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
	Author : Biscuit
*/
var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
		if(type == 1 && mode == 0) {
			qm.dispose();
			return;
		}else{
			qm.dispose();
			return;
		}
	}
	
	if (status == 0) {
		qm.sendNext("Wow, you have already reach Level 50, yet why are you still walking around like that? I mean, you've reached Level 50, but you are still walking around with your own feet. That's unusual behavior for a Knight like you.");
	} else if (status == 1) {
		qm.sendAcceptDecline("Well, I suppose it's up to you, but by doing that, you also risk marring the pride and honor of the Empress. This is why I am here to give you a helpful pointer. It's called #bMonster Riding#k. Of course you're interested in this, right?");
	} else if (status == 2) {
		qm.forceStartQuest();
		qm.forceCompleteQuest();
		qm.sendOk("There's a special mount that only the Cygnus Knights can enjoy. If you are interested, visit #bEreve#k. I will give you more information on it.");
		qm.dispose();
	}
}