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
*	Author : Raz
*	Author : Ronan
*
*	NPC = 9103000 - Pierre
*	Map =  Ludibrium - Ludibrium Maze 16
*	NPC MapId = 809050015
*	Function = Gives LMPQ EXP reward
*
*/

var status = 0;
var qty = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && status == 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
                
                if (status == 0) {
                        if(cm.isEventLeader()) {
                                if(!cm.getEventInstance().isEventTeamTogether()) {
                                        cm.sendOk("One or more instance team members is missing, please wait for them to reach here first.");
                                        cm.dispose();
                                }
                                else if(cm.hasItem(4001106, 30)) {
                                        qty = cm.getItemQuantity(4001106);
                                        cm.sendYesNo("Splendid! You have retrieved " + qty + " #t4001106# from this run, now your team will receive the fair amount of EXP from this action. Are you ready to get transported out?");
                                }
                                else {
                                        cm.sendOk("Your party cannot finish this PQ yet, as you have not reached the minimum of 30 #t4001106#'s in hand yet.");
                                        cm.dispose();
                                }
                        }
                        else {
                                cm.sendOk("Let your party leader talk to me to end this quest.");
                                cm.dispose();
                        }
                } else if(status == 1) {
                        cm.removeAll(4001106);
                        cm.getEventInstance().giveEventPlayersExp(50 * qty);
                        cm.getEventInstance().clearPQ();
                        cm.dispose();
                }
        }
}