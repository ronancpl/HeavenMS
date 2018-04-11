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
	Assistant Nancy
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
-- Content Improved by ----------------------------------------------------------------------------
        RonanLana (HeavenMS)
---------------------------------------------------------------------------------------------------
**/

var status;
var eim;
var hasEngage;
var hasRing;

function start() {
    eim = cm.getEventInstance();
    if(eim == null) {
        cm.warp(680000000,0);
        cm.dispose();
        return;
    }
    
    if(cm.getMapId() == 680000200) {
        if(eim.getIntProperty("weddingStage") == 0) {
            cm.sendNext("The guests are gathering here right now. Please wait awhile, the ceremony will start soon enough.");
        } else {
            cm.warp(680000210, "sp");
            cm.sendNext("Pick your seat over here and good show!");
        }
        
        cm.dispose();
    } else {
        if(cm.getPlayer().getId() != eim.getIntProperty("groomId") && cm.getPlayer().getId() != eim.getIntProperty("brideId")) {
            cm.sendNext("Sorry, only the marrying couple should be talking to me right now.");
            cm.dispose();
            return;
        }

        hasEngage = false;
        for(var i = 4031357; i <= 4031364; i++) {
            if(cm.haveItem(i)) {
                hasEngage = true;
                break;
            }
        }

        var rings = [1112806, 1112803, 1112807, 1112809];
        hasRing = false;
        for (i = 0; i < rings.length; i++) {
            if (cm.getPlayer().haveItemWithId(rings[i], true)) {
                hasRing = true;
            }
        }

        status = -1;
        action(1, 0, 0);
    }
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
    
    if (status == 0) {
        var hasGoldenLeaf = cm.haveItem(4000313);
        
        if (hasGoldenLeaf && hasEngage) {
            cm.sendOk("You can't leave yet! You need to click Pelvis Bebop and get his word before I can let you leave.");
            cm.dispose();
        } else if (hasGoldenLeaf && hasRing) {
            var choice = Array("Go to the Afterparty", "What should I be doing");
            var msg = "What can I help you with?#b";
            for (i = 0; i < choice.length; i++) {
                msg += "\r\n#L" + i + "#" + choice[i] + "#l";
            }
            cm.sendSimple(msg);
        } else {
            cm.sendNext("You don't seem to have a Gold Maple Leaf, engagement ring, or wedding ring. You must not belong here, so I will take you to Amoria.");
            selection = 20; // Random.
        }
    } else if (status == 1) {
        switch(selection) {
            case 0:
                if(eim.getIntProperty("isPremium") == 1) {
                    eim.warpEventTeam(680000300);
                    cm.sendOk("Enjoy! Cherish your Photos Forever!");
                } else {    // skip the party-time (premium only)
                    eim.warpEventTeam(680000500);
                    cm.sendOk("Congratulations for the newly-wed! I will escort you to the exit.");
                }
                
                cm.dispose();
                break;
                
            case 1:
                cm.sendOk("The superstars must receive the word of Pelvis Bebop to be united. When you are ready you can click me to go to the Afterparty.");
                cm.dispose();
                break;
                
            default:
                cm.warp(680000000,0);
                cm.dispose();
                break;
        }
    }
}
