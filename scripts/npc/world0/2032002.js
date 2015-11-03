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
/* Aura
 * 
 * Adobis's Mission I: Unknown Dead Mine (280010000)
 * 
 * Zakum PQ NPC (the one and only)
*/

var status;
var selectedType;
var scrolls;

function start() {
    cm.sendSimple("...#b\r\n#L0#What am I supposed to do here?#l\r\n#L1#I brought items!#l\r\n#L2#I want to get out!#l");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            selectedType = selection;
            if (selection == 0) {
                cm.sendNext("To reveal the power of Zakum, you'll have to recreate its core. Hidden somewhere in this dungeon is a \"Fire Ore\" which is one of the necessary materials for that core. Find it, and bring it to me.\r\n\r\nOh, and could you do me a favour? There's also a number of Paper Documents lying under rocks around here. If you can get 30 of them, I can reward you for your efforts.")
                cm.dispose();
            }
            else if (selection == 1) {
                if (!cm.haveItem(4001018)) { //documents
                    cm.sendNext("Please bring the Fire Ore with you.")
                    cm.dispose();
                }
                else {
                    if (!cm.haveItem(4001015, 30)) { //documents
                        cm.sendYesNo("So, you brought the fire ore with you? In that case, I can give you and your party a piece of it that should be more than enough to make the core of Zakum. Make sure your whole party has room in their inventory before proceeding.");
                        scrolls = false;
                    } else {
                        cm.sendYesNo("So, you brought the fire ore and the documents with you? In that case, I can give you and your party a piece of it that should be more than enough to make the core of Zakum. As well, since you brought the documents with you, I can also give you a special item which will bring you to the mine's entrance at any time. Make sure your whole party has room in their inventory before proceeding.");
                        scrolls = true;
                    }
                }
            } else if (selection == 2)
                cm.sendYesNo("Are you sure you want to exit? If you're the party leader, your party will also be removed from the mines.")
        }
        else if (status == 2) {
            var eim = cm.getPlayer().getEventInstance();
            if (selectedType == 1) {
                var party = eim.getPlayers();
                cm.gainItem(4001018, -1);
                if (scrolls)
                    cm.gainItem(4001015, -30);
                cm.givePartyItems(4031061, 1, party);
                if (scrolls) {
                    cm.givePartyItems(2030007, 5, party);
                    cm.givePartyExp(20000, party);
                } else
                    cm.givePartyExp(12000, party);
                eim.finishPQ();
                cm.dispose();
            }
            else if (selectedType == 2) {
                if (cm.isLeader())
                    eim.disbandParty();
                else
                    eim.leaveParty(cm.getPlayer());
                cm.dispose();
            }
        }
    }
}