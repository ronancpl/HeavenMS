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
/* victora
by Angel (get31720 ragezone)
 */

var wui = 0;

function start() {
    cm.sendSimple ("Welcome to the Cathedral. What would you like to do? \r\n#L0##bI need invitations for my guests#k #l\r\n#L1##bI'd like to prepare a wedding#k #l\r\n#L2##bCan you explain how I should prepare a wedding?#k #l\r\n#L3##bI am either the groom or bride and I'd like to go in#k #l\r\n#L4##bI am a guest and I'd like to go in#k #l");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        if (cm.haveItem(4214002)) { 
            cm.sendNext("Alright here are you invitations make sure your guest have them or they can't come in!"); 
            cm.gainItem(4031395,10); 
     
        } else { 
            cm.sendOk("Sorry but please make sure you have your premium wedding receipt or you won't be able to have your wedding"); 
            status = 9; 
        } 
    } else if (selection == 1) {
        if (cm.haveItem(5251003)) { 
            cm.sendNext("Alright, I'll give you your premium wedding receipt and make sure you don't lose it! If you lose your receipt you won't be able to get invitations or enter the cathedral!"); 
            cm.gainItem(4214002,1);
        } else if (selection == 2) {
            cm.sendNext("Have both the bride and groom buy a premium cathedral wedding ticket from the cash shop. Then ask me to prepare your wedding and i'll give you a wedding receipt. Talk to me if you want invitations so other guests can join. When you're ready just have everyone come to me and i'll let you or the guests in. Inside Debbie will warp you out to Amoria if you chose to leave. Nicole will warp you to the next map.");
        } else if (selection == 3) {
            if (cm.haveItem(4214002)) { 
                cm.sendNext("Okay go on in. Once you're ready click the Priest and he'll get you married."); 
                cm.warp(680000210, 2); 
            } else { 
                cm.sendOk("Sorry but you don't have a wedding receipt."); 
                status = 9; 
            } 
        } else if (selection == 4) {
            if (cm.haveItem(4031395)) { 
                cm.sendNext("Okay go on in. Once the bride and groom is ready click Nicole on the bottom to warp to the next map. Or use Debbie to leave to Amoria."); 
                cm.warp(680000210,0); 
     
            } else { 
                cm.sendOk("Sorry but you don't have a premium wedding invitation."); 
                status = 9; 
            } 
            cm.dispose();
        }
    }
}