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
@       Author : Ronan
@
@	NPC = Sky-Blue Balloon
@	Map = Hidden-Street <Stage 7>
@	NPC MapId = 922010700
@	Function = LPQ - 7 Stage
@
@	Description: You need a ranged person here. The ranged person must kill the three Ratz, and they'll trigger something. What's next is for you to find out! Get me 3 passes!
*/

importPackage(Packages.server.life);

var status = 0;
var curMap, stage;

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 922010100) / 100) + 1;
    
    status = -1;
    action(1, 0, 0);
}

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);
    
    eim.linkToNextStage(stage, "lpq", curMap);  //opens the portal to the next map
}

function action(mode, type, selection) {
        if (mode == -1) {
            cm.dispose();
        } else if (mode == 0){
            cm.dispose();
        } else {
                if (mode == 1)
                        status++;
                else
                        status--;
                    
                var eim = cm.getPlayer().getEventInstance();
                
                if(eim.getProperty(stage.toString() + "stageclear") != null) {
                        cm.sendNext("Hurry, goto the next stage, the portal is open!");
                }
                else {
                        if (eim.isEventLeader(cm.getPlayer())) {
                                var state = eim.getIntProperty("statusStg" + stage);

                                if(state == -1) {           // preamble
                                        cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k. You need ranged personnel here. They must kill the three Ratz, which will trigger something. What's next is for you to find out! Get me 3 passes!");
                                        eim.setProperty("statusStg" + stage, 0);
                                }
                                else if(state == 0) {       // check stage completion
                                        if (cm.haveItem(4001022, 3)) {
                                                cm.sendOk("Good job! You have collected all 3 #b#t4001022#'s.#k");
                                                cm.gainItem(4001022, -3);

                                                eim.setProperty("statusStg" + stage, 1);
                                                clearStage(stage, eim, curMap);
                                        } else {
                                                cm.sendNext("Sorry you don't have all 3 #b#t4001022#'s.#k");
                                        }
                                }
                        } else {
                                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                        }
                }
                
                cm.dispose();
        }
}