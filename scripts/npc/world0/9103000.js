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
 *@Author RMZero213
 * Ludibrium Maze Party Quest
 * Do not release anywhere other than RaGEZONE. Give credit if used.
 */

var status = 0;
var minimumCouponsNeeded = 200;
function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == -1|| (mode == 0 && status == 0))
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (!isLeader()) {
                cm.sendOk("Give any coupons to the leader of the party and tell them to talk to me.");
                cm.dispose();
            } else {
                cm.sendYesNo("Do you have all the coupons of the party and would like to get out of here?");
			}
        } else if (status == 1) {
			if (cm.itemQuantity(4001106) < minimumCouponsNeeded){
				cm.sendOk("Sorry, but you do not have at least " + minimumCouponsNeeded + " coupons! Talk to me again when you've collected more!");
                cm.dispose();
				return;
			}
			var party = cm.getPartyMembers();
            for (var i = 0; i < party.size(); i++) {
                if (party.get(i).getMap().getId() != 809050015) {
                    cm.sendOk("A member of your party is not presently in the map.");
                    cm.dispose();
                    return;
                }
            }
			var members = cm.getPlayer().getEventInstance().getPlayers();
			//cm.removeFromParty(4001106, members);
			cm.gainItem(4001106, -200);
			cm.givePartyExp("LudiMazePQ");
			cm.warpParty(809050016);
            cm.dispose();
        }	
    }
}

function isLeader(){
    if(cm.getParty() == null)
        return false;
    else
        return cm.isLeader();
}