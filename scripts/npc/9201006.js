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
	Debbie
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

var status;

function start() {  
    status = -1;  
    action(1, 0, 0);  
}  

function action(mode, type, selection) {  
    if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then"); 
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
		
    switch (status) {
        case 0:
            cm.sendNext("I only warp out people who are here by accident.");
            break;
        case 1:
            var engagementRings = new Array(4031360, 4031358, 4031362, 4031364);
            var hasEngagement = false;
            for (var x = 0; x < engagementRings.length && !hasEngagement; x++) {
                if (cm.haveItem(engagementRings[x], 1))
                    hasEngagement = true;
            }
            if (cm.haveItem(4000313) && hasEngagement) {
                cm.sendOk("Please continue with the wedding.");
                cm.dispose();
            } else {
                cm.warp(680000000,0);
                cm.dispose();
            }
            break;
        case 2:
            cm.sendOk("You do not have the required item to continue through this wedding.");
            break;
    }
}
