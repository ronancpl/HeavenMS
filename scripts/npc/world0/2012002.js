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
 * @Author: Moogra
 * @NPC ID: 2012002
 * @NPC   : Erin (On Orbis Boat )
 */

var status = 0;

function start() {
    cm.sendYesNo("Do you wish to leave the boat?");
}

function action(mode, type, selection) {
if (mode == 0 && status == 1) {
	cm.sendOk("Good choice");
	cm.dispose();
}
    if (mode > 0)
        status++;
    else
        cm.dispose();
    
    if (status == 1) {
        cm.sendNext ("Alright, see you next time. Take care.");
    }
    else if (status == 2) {
        cm.warp(200000111, 0);// back to Orbis jetty
        cm.dispose();
    }
}
