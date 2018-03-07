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
@	Author : Twdtwd
@       Author : Ronan
@
@	NPC = Blue Balloon
@	Map = Hidden-Street <Stage 8>
@	NPC MapId = 922010800
@	Function = LPQ - 8 Stage
@
@	Description: Used to find the combo to unlock the next door. Players stand on 5 different crates to guess the combo.
*/

function generateCombo() {
	var countPicked = 0;
	var positions = Array(0,0,0,0,0,0,0,0,0);
	while(countPicked < 5) {
		var picked = Math.floor(Math.random() * positions.length);
		if(positions[picked] == 1) // Don't let it pick one its already picked.
			continue;
			
		positions[picked] = 1;
		countPicked++;
	}
	
	var returnString = "";
	for(var i = 0; i < positions.length; i++) {
		returnString += positions[i];
		if(i != positions.length - 1)
		returnString += ",";
	}
	
	return returnString;
	
}

var debug = false;
var status = 0;
var curMap, stage;

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);
    
    eim.linkToNextStage(stage, "lpq", curMap);  //opens the portal to the next map
}

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 922010100) / 100) + 1;
    
    status = -1;
    action(1, 0, 0);
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
                                        cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k. In this stage, line up 5 member of your party above those boxes in order to form the right combination to unlock the next stage. Only one player should stay above a box desired to pertain the combination.");
                                        
                                        var st = (debug) ? 2 : 0;
                                        eim.setProperty("statusStg" + stage, st);
                                }
                                else {       // check stage completion
                                        if(state == 2) {
                                                eim.setProperty("statusStg" + stage, 1);
                                                clearStage(stage, eim, curMap);
                                                cm.dispose();
                                                return;
                                        }
                                    
                                        objset = [0,0,0,0,0,0,0,0,0];
                                        var playersOnCombo = 0;
                                        var map = cm.getPlayer().getMap();
                                        var party = cm.getEventInstance().getPlayers();
                                        for (var i = 0; i < party.size(); i++) {
                                            for (var y = 0; y < map.getAreas().size(); y++) {
                                                if (map.getArea(y).contains(party.get(i).getPosition())) {
                                                    playersOnCombo++;
                                                    objset[y] = 1;
                                                    //cm.mapMessage(5, "Player found on " + (y + 1));
                                                    break;
                                                }
                                            }
                                        }

                                        if (playersOnCombo == 5 || cm.getPlayer().gmLevel() > 1) {
                                            var comboStr = eim.getProperty("stage" + stage + "combo");
                                            if(comboStr == null) {
                                                comboStr = generateCombo();
                                                eim.setProperty("stage" + stage + "combo", comboStr);
                                            }
                                            
                                            var combo = comboStr.split(',');
                                            var correctCombo = true;
                                            for (i = 0; i < objset.length && correctCombo; i++)
                                                if (parseInt(combo[i]) != objset[i]) {
                                                    //cm.mapMessage(5, "Combo failed on " + (i + 1));
                                                    correctCombo = false;
                                                }
                                            if (correctCombo || cm.getPlayer().gmLevel() > 1) {
                                                eim.setProperty("statusStg" + stage, 1);
                                                clearStage(stage, eim, curMap);
                                                cm.dispose();
                                            } else {
                                                eim.showWrongEffect();
                                                cm.dispose();
                                            }
                                        } else {
                                            cm.sendNext("It looks like you haven't found the 5 boxes just yet. Please think of a different combination of boxes. Only 5 are allowed to stand on boxes, and if you move it may not count as an answer, so please keep that in mind. Keep going!");
                                            cm.dispose();
                                        }
                                }
                        } else {
                                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                        }
                }
                
                cm.dispose();
        }
}