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
/*
@	Author : Ronan
@
@	NPC = Nuris (9040001)
@	Map = Sharenian - Returning Path
@	NPC MapId = 990001100
@	NPC Exit-MapId = 101030104
@
 */

var status;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }

    if (status == 0) {
        var outText = "It seems you have finished exploring Sharenian Keep, yes? Are you going to return to the recruitment map now?";
        cm.sendYesNo(outText);
    } else if (mode == 1) {
        var eim = cm.getEventInstance();
        
        if(eim != null && eim.isEventCleared()) {
            if(!eim.giveEventReward(cm.getPlayer())) {
                    cm.sendNext("It seems you don't have a free slot in either your #rEquip#k, #rUse#k or #rEtc#k inventories. Please make some room first.");
            } else {
                    cm.warp(101030104);
            }
            
            cm.dispose();
        } else {
            cm.warp(101030104);
            cm.dispose();
        }
    }
}