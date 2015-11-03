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
/* NPC:     Thomas Swift
 * Maps:    100000000, 680000000
 * Author:  Moogra
 * Purpose: Amoria warper.
*/

status = -1;

function start() {
    if (cm.getPlayer().getMapId() == 100000000)
        cm.sendYesNo("I can take you to the Amoria Village. Are you ready to go?");
    else
        cm.sendYesNo("I can take you back to Henesys. Are you ready to go?");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0)
            cm.sendOk("Ok, feel free to hang around until you're ready to go!");
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.sendNext("I hope you had a great time! See you around!");
    else if (status == 1) {
        if (cm.getPlayer().getMapId() == 100000000)
            cm.warp(680000000, 0);
        else
            cm.warp(100000000, 5);
        cm.dispose();
    }
}