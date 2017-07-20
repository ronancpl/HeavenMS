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
@	NPC = Green Balloon
@	Map = Hidden-Street <Stage 5>
@	NPC MapId = 922010500
@	Function = LPQ - 5th Stage
@
 */

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
                                        cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k. This is the 2nd stage, but everyone has to cooperate. There are 6 portals here. One is guarded by undefeatable monsters, and one is very high. I'd like you and your party to go in each one and break the boxes inside. Bring back the drops -- there should be 24.");
                                        eim.setProperty("statusStg" + stage, 0);
                                }
                                else {       // check stage completion
                                        if (cm.haveItem(4001022, 24)) {
                                                cm.sendOk("Good job! You have collected all 24 #b#t4001022#'s.#k");
                                                cm.gainItem(4001022, -24);

                                                eim.setProperty("statusStg" + stage, 1);
                                                clearStage(stage, eim, curMap);
                                        } else {
                                                cm.sendNext("Sorry you don't have all 24 #b#t4001022#'s.#k");
                                        }
                                }
                        } else {
                                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                        }
                }
                
                cm.dispose();
        }
}