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
	Author : Traitor
	NPC Name: 		Neinheart
	Map(s): 		Ereve
	Description: 		Quest - Time to Choose
	Quest ID : 		20100
*/

var status = -1;

function start(mode, type, selection) {
    if (mode > 0)
        status++;
    else {
        qm.dispose();
        return;
    }
    if (status == 0)
        qm.sendAcceptDecline("Ahhh, you're back. I can see that you're at level 10 now. It looks like you're flashing a glimmer of hope towards becoming a Knight. The basic training has now ended, and it's time for you to make the decision.");
    else if (status == 1) {
        qm.sendOk("Now look to the left. The leaders of the Knights will be waiting for you. There will be 5 paths for you to choose from. All you need to do is choose one of them. All 5 of them will lead you to the path of a Knight, so... I suggest you pay attention to what each path offers, and select the one you'd most like to take.");
        qm.forceStartQuest();
        qm.forceCompleteQuest();
        qm.dispose();
    }
}