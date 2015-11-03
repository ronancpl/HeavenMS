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
@	NPC = Sgt.Anderson
@	Map =  Abandoned Tower <Stage 1>
@	NPC MapId = 922010100
@	NPC Exit-MapId = 221024500
@
 */
//4001022 - PASS OF DIMENSION

var status = 0;

function start() {
    cm.sendYesNo("Are you sure you want to leave?");
}

function action(mode, type, selection) {
    if (mode == -1) //ExitChat
        cm.dispose();
    else if (mode == 0) {//No
        cm.sendOk("OK, Talk to me again if you want to leave here.");
        cm.dispose();
    } else {		    //Regular Talk
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.getPlayer().getMap().getId() == 109050001) {
            if(status == 0)
                cm.sendNext("See ya.");
            else if (status == 1){
                cm.warp(109060001);
                cm.dispose();
            }
        } else {
            if (status == 1)
                cm.sendNext("Ok, Bye!");
            else if (status == 2) {
                var eim = cm.getPlayer().getEventInstance();
                if (eim == null)
                    cm.sendOk("Wait, Hey! how'd you get here?\r\nOh well you can leave anyways");
                else {
                    if(isLeader()){
                        eim.disbandParty();
                        cm.removeFromParty(4001008, eim.getPlayers());
                    } else {
                        eim.leftParty(cm.getPlayer());
                        cm.removeAll(4001008);
                        cm.removeAll(4031059);
                        cm.removeAll(4001102);
                        cm.removeAll(4001108);
                    }
                    cm.dispose();
                }
            } else if (status == 3) {
                cm.warp(109050001);
                cm.removeAll(4001008);
                cm.removeAll(4031059);
                cm.removeAll(4001102);
                cm.removeAll(4001108);
                cm.dispose();
            }
        }
    }
}

function isLeader(){
    return cm.getParty() == null ? false : cm.isLeader();
}