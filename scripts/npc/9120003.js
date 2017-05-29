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
-- Odin JavaScript --------------------------------------------------------------------------------
	Hikari - Showa Town(801000000)
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Information
        2.0 - Second Version by Moogra
        2.1 - Code revamped by Moogra
---------------------------------------------------------------------------------------------------
**/

var price = 300;

function start() {
    cm.sendYesNo("Would you like to enter the bathhouse? That'll be " + price + " mesos for you.");
}

function action(mode, type, selection) {
    if (mode < 1) {
        if (mode == 0)
            cm.sendOk("Please come back some other time.");
        cm.dispose();
        return;
    }
    if (cm.getMeso < price)
        cm.sendOk("Please check and see if you have " + price + " mesos to enter this place.");
    else {
        cm.gainMeso(-price);
        cm.warp(801000100 + 100 * cm.getPlayer().getGender());
    }
    cm.dispose();
}
