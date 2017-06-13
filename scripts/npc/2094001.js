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

var status = -1;

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
                    
                if(cm.getMapId() == 925100500) {
                        if (status == 0) {
                                if(cm.isEventLeader()) {
                                        cm.sendOk("I have been saved thanks to your efforts! Thank you, guys!");
                                }
                                else {
                                        cm.sendOk("I have been saved thanks to your efforts! Thank you, guys! Let your party leader talk to me first before I give you your rewards...");
                                        cm.dispose();
                                }
                        }
                        else {
                                cm.getEventInstance().clearPQ();
                                cm.dispose();
                        }
                }
                else {
                        if (status == 0) {
                                cm.sendSimple("Thank you for saving me! How can I help you?\r\n#b#L0#Get me out of here.\r\n#L1#Give me Pirate Hat.");
                        } else if (status == 1) {
                                if (selection == 0) {
                                        if (!cm.canHold(4001158, 1)) {
                                                cm.sendOk("Please make room in ETC.");
                                                cm.dispose();
                                                return;
                                        }
                                        cm.gainItem(4001158, 1);
                                        cm.warp(251010404,0);
                                } else {
                                        if (cm.haveItem(1003267, 1)) {
                                                cm.sendOk("You have the best hat.");
                                        } else if (cm.haveItem(1002573, 1)) {
                                                if (cm.haveItem(4001158, 20)) {	
                                                        if (cm.canHold(1003267,1)) {
                                                                cm.gainItem(1002573, -1);
                                                                cm.gainItem(4001158, -20);
                                                                cm.gainItem(1003267,1);
                                                                cm.sendOk("I have given you the hat.");
                                                        } else {
                                                                cm.sendOk("Please make room in your EQUIP inventory before receiving the hat.");
                                                        }
                                                } else {
                                                        cm.sendOk("You need 20 #t4001158# to get the next hat.");
                                                }
                                        } else if (cm.haveItem(1002572, 1)) {
                                                if (cm.haveItem(4001158, 20)) {
                                                        if (cm.canHold(1002573,1)) {
                                                                cm.gainItem(1002572, -1);
                                                                cm.gainItem(4001158, -20);
                                                                cm.gainItem(1002573,1);
                                                                cm.sendOk("I have given you the hat.");
                                                        } else {
                                                                cm.sendOk("Please make room in your EQUIP inventory before receiving the hat.");
                                                        }
                                                } else {
                                                        cm.sendOk("You need 20 #t4001158# to get the next hat.");
                                                }
                                        } else {
                                                if (cm.haveItem(4001158, 20)) {	
                                                        if (cm.canHold(1002572,1)) {
                                                                cm.gainItem(4001158, -20);
                                                                cm.gainItem(1002572,1);
                                                                cm.sendOk("I have given you the hat.");
                                                        } else {
                                                                cm.sendOk("Please make room in your EQUIP inventory before receiving the hat.");
                                                        }
                                                } else {
                                                        cm.sendOk("You need 20 #t4001158# to get the next hat.");
                                                }
                                        }
                                }

                                cm.dispose();
                        }
                }
                
        }
}
