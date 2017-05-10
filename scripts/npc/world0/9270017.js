/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Xinga - Pilot
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Information
	2.0 - Second Version by Jayd
---------------------------------------------------------------------------------------------------
**/

var status = 0;

function start() {
    cm.sendYesNo("The plane will be taking off soon, will you leave now? You will have to buy the plane ticket again to come in here.");
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
            cm.sendOk("Please hold on for a sec, and plane will be taking off. Thanks for your patience.");
        cm.dispose();
        return;
    }
    status++;
    if (status == 1) {
        cm.sendNext("The ticket is not refundable, hope to see you again!");
    } else if(status == 2){
		cm.warp(103000000);
		cm.dispose();
    }
}