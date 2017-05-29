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
/* Olson the Toy Soldier
	2040002

map: 922000010
quest: 3230
escape: 2040028
*/

var status = 0;
var em;

function start() {
    if (cm.isQuestStarted(3230)) {
        em = cm.getEventManager("DollHouse");

        if (em.getProperty("noEntry") == "false") {
            cm.sendNext("The pendulum is hidden inside a dollhouse that looks different than the others.");
        }
        else {
            cm.sendOk("Someone else is already searching the area. Please wait until the area is cleared.");
            cm.dispose();
        }
    }
    else {
        cm.sendOk("We are not allowed to let the general public wander past this point.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (status == 1) 
            cm.sendYesNo("Are you ready to enter the dollhouse map?");
        else if (status == 2) {
            var eim = em.newInstance("DollHouse");
            eim.registerPlayer(cm.getPlayer());
            cm.dispose();
        }
    }
}