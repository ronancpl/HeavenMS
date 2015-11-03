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
@	Author : Raz
@
@	NPC = Pink Balloon
@	Map = Hidden-Street <Stage B>
@	NPC MapId = 922011000
@	Function = LPQ - B Stage
@
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();//ExitChat
    }else if (mode == 0){
        cm.sendOk("Wise choice. Who wouldn't want free mesos from the #bBonus Stage#k.");
        cm.dispose();//No
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        var eim = cm.getPlayer().getEventInstance();
        if (status == 0)
            cm.sendYesNo("Would you like to leave the bonus?");
        else if (status == 1) {
            if(isLeader())
                cm.sendOk("Ok, Your loss");
            else{
                cm.sendOk("Ask your #bParty-Leader#k to come talk to me.");
                cm.dispose();
            }
        }else if (status == 2) {
            var map = eim.getMapInstance(922011100);
            var party = eim.getPlayers();
            cm.warpMembers(map, "st00", party);
            cm.dispose();
        }
    }
}
     
function isLeader(){
    if(cm.getParty() == null){
        return false;
    }else{
        return cm.isLeader();
    }
}
