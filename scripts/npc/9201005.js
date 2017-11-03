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
/**
	Nicole
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

var status;
var x;
var hasEngageRing = false;

function start() {  
    status = -1;  
    action(1, 0, 0);  
}  

function action(mode, type, selection) {  
     if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then"); 
            cm.dispose();
			return;
    } else if (mode == 1) {
            status++;
        } else {
            status--;
        }
		var item = new Array(4031360, 4031358, 4031362, 4031364);
		for (x = 0; x < item.length && !hasEngageRing; x++) {
			if (cm.haveItem(item[x], 1))
				hasEngageRing = true;
		}
    if (status == 0) {
		var text = "I'm here to assist you on weddings !";
		var choice = new Array("How do I prepare a wedding?", "I have an engagement ring and need invites for my guests", "I am the bride/groom and I'd like to start the wedding", "I am the guest and I'd like to go into the wedding");
		for (x = 0; x < choice.length; x++) {
			text += "\r\n#L" + x + "##b" + choice[x] + "#l";
		}
		cm.sendSimple(text);
	} else if (status == 1) {
		switch(selection) {
			case 0:
				cm.sendOk("Moony makes the engagement ring. The engagement ring is required throughout the wedding so never lose it. To invite your guests into the wedding you need to show me your engagement ring and then I'll give you 15 Gold Maple Leaves. They need 1 each to enter the wedding. Enjoy!");
				cm.dispose();
				break;
			case 1:
				if (cm.haveItem(4000313)) {
					cm.sendOk("You already have a Gold Maple Leaf. Go give them to your guests before you go into the wedding.");
					cm.dispose();
                } else if (hasEngageRing) {
					cm.sendOk("You have received 15 Gold Maple Leaves.");
					cm.gainItem(4000313,15);
					cm.dispose();
				} else {
					cm.sendOk("You do not have an engagement ring.");
					cm.dispose();
				}
				break;
			case 2:
				if (hasEngageRing) {
					cm.warp(680000210, 2);
					cm.sendOk("Talk to High Priest John when you're ready to be married.");
					cm.dispose();
				} else {
					cm.sendOk("You do not have an engagement ring.");
					cm.dispose();
				}
				break;
			case 3:
				if (cm.haveItem(4000313)) {
					cm.warp(680000210, 0);
					cm.sendOk("Enjoy the wedding. Don't drop your Gold Maple Leaf or you won't be able to finish the whole wedding.");
					cm.dispose();
				} else {
					cm.sendOk("You do not have a Gold Maple Leaf.");
					cm.dispose();
				}
				break;
		}
	}
}