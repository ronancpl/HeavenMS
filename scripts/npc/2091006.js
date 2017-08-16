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
	Author: Traitor, XxOsirisxX, Moogra
*/

/**
 * Dojo Entrance NPC
 */
var status = -2;
var readNotice = 0;

function start() {
    cm.sendSimple("#e< Notice >#n\r\nIf there is anyone who has the courage to challenge the Mu Lung Dojo, come to the Mu Lung Dojo.  - Mu Gong -\r\n\r\n\r\n#b#L0#Challenge the Mu Lung Dojo.#l\r\n#L1#Read the notice in more detail.#l");
}

function action(mode, type, selection) {
    status++;
    if(mode == 0 && type == 0)
        status -= 2;
    if (mode >= 0) {
        if (selection == 1 || readNotice == 1) {
            if (status == -1) {
                readNotice = 1;
                cm.sendNext("#e< Notice : Take the challenge! >#n\r\nMy name is Mu Gong, the owner of the My Lung Dojo. Since long ago, I have been training in Mu Lung to the point where my skills have now reached the pinnacle. Starting today, I will take on any and all applicants for Mu Lung Dojo. The rights to the Mu Lung Dojo will be given only to the strongest person.\r\nIf there is anyone who wishes to learn from me, come take the challenge any time! If there is anyone who wishes to challenge me, you're welcome as well. I will make you fully aware of your own weakness.");
            } else if (status == 0)
                cm.sendPrev("PS:You can challenge me on your own. But if you don't have that kind of courage, go ahead and call all your friends.");
            else
                cm.dispose();
        } else {
            if (status == -1 && mode == 1) {
                cm.sendYesNo("(Once I had placed my hands on the bulletin board, a mysterious energy began to envelop me.)\r\n\r\nWould you like to go to Mu Lung Dojo?");
            } else if (status == 0) {
                if (mode == 0) {
                    cm.sendNext("#b(As I took my hand off the bulletin board, the mysterious energy that was covering my disappeared as well.)");
                } else {
                    cm.getPlayer().saveLocation("MIRROR");
                    cm.warp(925020000, 4);
                }
                cm.dispose();
            }
        }
    } else
        cm.dispose();
}