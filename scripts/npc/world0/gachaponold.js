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
 * Gachapon Script - Henesys, currently with Ellinia items
 * @author Moogra
 * @NPC : Gachapon - Henesys
 * @NPC ID : 9100101
 * TODO: FINISH REAL TEXT, use sendSimpleNext for text selection
*/

var status = 0;
var remoteGachapon = false;
var ticketId = 5220000;

function start() {
	if(remoteGachapon)
		ticketId = 5451000;
		
    if (cm.haveItem(ticketId)) {
        cm.sendYesNo("You may use Gachapon. Would you like to use your Gachapon ticket?");
    } else {
        cm.sendSimple("Welcome to the " + cm.getPlayer().getMap().getMapName() + " Gachapon. How may I help you?\r\n\r\n#L0#What is Gachapon?#l\r\n#L1#Where can you buy Gachapon tickets?#l");
    }
}

function action(mode, type, selection){
	if(mode == -1) {
		cm.dispose();
	} else if (mode == 1 && cm.haveItem(ticketId)) {
		if(cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
			cm.gainItem(ticketId, -1);
			cm.doGachapon(cm.getNpc());
		} else {
			cm.sendOk("Please have at least one slot in your #rEQUIP, USE, SET-UP, #kand #bETC#k inventories free.");
		}
		cm.dispose();
    } else {
        if (mode > 0) {
            status++;
            if (selection == 0) {
                cm.sendNext("Play Gachapon to earn rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be the winner of a random mix of items.");
            } else if (selection == 1) {
                cm.sendNext("Gachapon Tickets are available in the #rCash Shop#k and can be purchased using NX or Maple Points. Click on the red SHOP at the lower right hand corner of the screen to visit the #rCash Shop#k where you can purchase tickets.");
            } else if (status == 2) {
                cm.sendNext("You'll find a variety of items from the " + cm.getPlayer().getMap().getMapName() + " Gachapon, but you'll most likely find several related items and scrolls since " + cm.getPlayer().getMap().getMapName() + " is known as the town.");
                cm.dispose();
            } else {
				
			}
        } else {
			cm.dispose();
		}
    }
}