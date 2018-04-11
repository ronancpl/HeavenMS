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
        cm.sendOk("Goodbye then.");
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
    
    var eim = cm.getEventInstance();
    if(eim == null) {
        cm.warp(680000000,0);
        cm.dispose();
        return;
    }
    
    var isMarrying = (cm.getPlayer().getId() == eim.getIntProperty("groomId") || cm.getPlayer().getId() == eim.getIntProperty("brideId"));
		
    switch (status) {
        case 0:
            var hasEngagement = false;
            for (var x = 4031357; x <= 4031364; x++) {
                if (cm.haveItem(x, 1)) {
                    hasEngagement = true;
                    break;
                }
            }
            
            if (cm.haveItem(4000313) && isMarrying) {
                if(eim.getIntProperty("weddingStage") == 3) {
                    cm.sendOk("Congratulations on your wedding. Please talk to #b#p9201007##k to start the afterparty.");
                    cm.dispose();
                } else if(hasEngagement) {
                    cm.sendOk("Please continue with the wedding.");
                    cm.dispose();
                } else {
                    cm.sendOk("You do not have the required item to continue through this wedding. Unfortunately, it's over...");
                }
            } else {
                if(eim.getIntProperty("weddingStage") == 3) {
                    if(!isMarrying) {
                        cm.sendYesNo("The couple #rhas just married#k, and soon #bthey will start the afterparty#k. You should wait here for them. Are you really ready to #rquit this wedding#k and return to #bAmoria#k?");
                    } else {
                        cm.sendOk("Congratulations on your wedding. Please talk to #b#p9201007##k to start the afterparty.");
                        cm.dispose();
                    }
                } else {
                    cm.sendYesNo("Are you sure you want to #rquit this wedding#k and return to #bAmoria#k?");
                }
            }
            break;
            
        case 1:
            cm.warp(680000000,0);
            cm.dispose();
            break;
    }
}
