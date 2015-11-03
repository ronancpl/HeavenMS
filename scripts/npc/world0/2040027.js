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
/*      Author: Xterminator, Moogra
	NPC Name: 		Fourth Eos Rock
	Map(s): 		Ludibrium : Eos Tower 1st Floor (221020000)
	Description: 		Brings you to 41st Floor
*/

function start() {
    if (cm.haveItem(4001020))
        cm.sendYesNo("You can use #bEos Rock Scroll#k to activate #bFourth Eos Rock#k. Will you head over to #bThird Eos Rock#k at the 41st floor?");
    else {
        cm.sendOk("There's a rock that will enable you to teleport to #bThird Eos Rock#k, but it cannot be activated without the scroll.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
    } else {
        cm.gainItem(4001020, -1);
        cm.warp(221021700, 3);
    }
    cm.dispose();
}