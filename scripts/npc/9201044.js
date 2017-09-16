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
@       Author : Ronan
@
@	NPC = Amos (PQ)
@	Map = AmoriaPQ maps
@	Function = AmoriaPQ Host
@
@	Description: Used to find the combo to unlock the next door. Players stand on 5 different crates to guess the combo.
*/

importPackage(Packages.server.life);

var debug = false;
var autopass = false;

function spawnMobs(maxSpawn) {
    var spawnPosX;
    var spawnPosY;

    var mapObj = cm.getMap();
    if (stage == 2) {
        spawnPosX = [619, 299, 47, -140, -471];
        spawnPosY = [-840, -840, -840, -840, -840];

        for(var i = 0; i < 5; i++) {
            for(var j = 0; j < 2; j++) {
                var mobObj1 = MapleLifeFactory.getMonster(9400515);
                var mobObj2 = MapleLifeFactory.getMonster(9400516);
                var mobObj3 = MapleLifeFactory.getMonster(9400517);

                mapObj.spawnMonsterOnGroundBelow(mobObj1, new Packages.java.awt.Point(spawnPosX[i], spawnPosY[i]));
                mapObj.spawnMonsterOnGroundBelow(mobObj2, new Packages.java.awt.Point(spawnPosX[i], spawnPosY[i]));
                mapObj.spawnMonsterOnGroundBelow(mobObj3, new Packages.java.awt.Point(spawnPosX[i], spawnPosY[i]));
            }
        }
    } else {
        spawnPosX = [2303, 1832, 1656, 1379, 1171];
        spawnPosY = [240, 150, 300, 150, 240];

        for(var i = 0; i < maxSpawn; i++) {
            var rndMob = 9400519 + Math.floor(Math.random() * 4);
            var rndPos = Math.floor(Math.random() * 5);

            var mobObj = MapleLifeFactory.getMonster(rndMob);
            mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(spawnPosX[rndPos], spawnPosY[rndPos]));
        }
    }
}

function generateCombo1() {
	var positions = Array(0,0,0,0,0,0,0,0,0);
        var rndPicked = Math.floor(Math.random() * Math.pow(3, 5));
        
        while(rndPicked > 0) {
            (positions[rndPicked % 3])++;
            
            rndPicked = Math.floor(rndPicked / 3);
        }
	
	var returnString = "";
	for(var i = 0; i < positions.length; i++) {
		returnString += positions[i];
		if(i != positions.length - 1)
                        returnString += ",";
	}
	
	return returnString;
}

function generateCombo2() {
	var toPick = 5, rndPicked;
	var positions = Array(0,0,0,0,0,0,0,0,0);
	while(toPick > 0) {
		rndPicked = Math.floor(Math.random() * 9);
                
                if(positions[rndPicked] == 0) {
                        positions[rndPicked] = 1;
                        toPick--;
                }
	}
	
	var returnString = "";
	for(var i = 0; i < positions.length; i++) {
		returnString += positions[i];
		if(i != positions.length - 1)
                        returnString += ",";
	}
	
	return returnString;
}

var status = 0;
var curMap, stage;

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    if(stage > 1) {
        eim.showClearEffect(true);
        eim.linkToNextStage(stage, "apq", curMap);  //opens the portal to the next map
    }
    else {
        cm.getMap().getPortal("go01").setPortalState(false);
        
        var val = Math.floor(Math.random() * 3);
        eim.showClearEffect(670010200, "gate" + val, 2);
        
        cm.getMap().getPortal("go0" + val).setPortalState(true);
        eim.linkPortalToScript(stage, "go0" + val, "apq0" + val, curMap);
    }
}

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 670010200) / 100) + 1;
    
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
                        cm.sendNext("The portal is already open, advance for the trials that awaits you there.");
                }
                else {
                        if (eim.isEventLeader(cm.getPlayer())) {
                                var state = eim.getIntProperty("statusStg" + stage);

                                if(state == -1) {           // preamble
                                        if(stage == 1) cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k of the Amorian Challenge. In this stage, talk to #p9201047#, he will pass to you further details of the mission. After shattering the Magik Mirror down there, return the shard to #p9201047# and come here to gain access to the next stage.");
                                        else if(stage == 2) cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k of the Amorian Challenge. In this stage, let 5 of your party members climb up the platforms in such a way to try for a combination to unlock the portal to the next level. When you feel ready, talk to me and I'll let you know the situation. However, be prepared, as in the case the portal does not get unlocked after a few tries, monsters will spawn.");
                                        else if(stage == 3) cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k of the Amorian Challenge. In this stage, let 5 of your party members climb up the platforms, one on each, in such a way to try for a combination to unlock the portal to the next level. When you feel ready, talk to me and I'll let you know the situation. Take hint: upon failing, count the number of slimes appearing on the scene, that will tell how many of you had their position right.");

                                        var st = (autopass) ? 2 : 0;
                                        eim.setProperty("statusStg" + stage, st);
                                }
                                else {       // check stage completion
                                        if(state == 2) {
                                                eim.setProperty("statusStg" + stage, 1);
                                                clearStage(stage, eim, curMap);
                                                cm.dispose();
                                                return;
                                        }

                                        var map = cm.getPlayer().getMap();
                                        if(stage == 1) {
                                                if(eim.getIntProperty("statusStg" + stage) == 1) {
                                                    clearStage(stage, eim, curMap);
                                                } else {
                                                    cm.sendOk("Talk with #p9201047# for more info on this stage.");
                                                }
                                        } else if(stage == 2 || stage == 3) {
                                                if(map.countMonsters() == 0) {
                                                        objset = [0,0,0,0,0,0,0,0,0];
                                                        var playersOnCombo = 0;
                                                        var party = cm.getEventInstance().getPlayers();
                                                        for (var i = 0; i < party.size(); i++) {
                                                            for (var y = 0; y < map.getAreas().size(); y++) {
                                                                if (map.getArea(y).contains(party.get(i).getPosition())) {
                                                                    playersOnCombo++;
                                                                    objset[y] += 1;
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        if (playersOnCombo == 5/* || cm.getPlayer().gmLevel() > 1*/ || debug) {
                                                            var comboStr = eim.getProperty("stage" + stage + "combo");
                                                            if (comboStr == null || comboStr == "") {
                                                                if (stage == 2) {
                                                                    comboStr = generateCombo1();
                                                                } else {
                                                                    comboStr = generateCombo2();
                                                                }

                                                                eim.setProperty("stage" + stage + "combo", comboStr);
                                                                if(debug) print("generated " + comboStr + " for stg" + stage + "\n");
                                                            }

                                                            var combo = comboStr.split(',');
                                                            var correctCombo = true;
                                                            var guessedRight = objset.length;
                                                            var playersRight = 0;

                                                            if(!debug) {
                                                                for (i = 0; i < objset.length; i++) {
                                                                    if (parseInt(combo[i]) != objset[i]) {
                                                                        correctCombo = false;
                                                                        guessedRight--;
                                                                    } else {
                                                                        if(objset[i] > 0) playersRight++;
                                                                    }
                                                                }
                                                            } else {
                                                                for (i = 0; i < objset.length; i++) {
                                                                    var ci = cm.getPlayer().countItem(4000000 + i);

                                                                    if (ci != parseInt(combo[i])) {
                                                                        correctCombo = false;
                                                                        guessedRight--;
                                                                    } else {
                                                                        if(ci > 0) playersRight++;
                                                                    }
                                                                }
                                                            }


                                                            if (correctCombo/* || cm.getPlayer().gmLevel() > 1*/) {
                                                                eim.setProperty("statusStg" + stage, 1);
                                                                clearStage(stage, eim, curMap);
                                                                cm.dispose();
                                                            } else {
                                                                var miss = eim.getIntProperty("missCount") + 1;
                                                                var maxMiss = (stage == 2) ? 7 : 1;

                                                                if (miss < maxMiss) {   //already implies stage 2
                                                                    eim.setIntProperty("missCount", miss);

                                                                    if(guessedRight == 6) { //6 unused slots on this stage
                                                                        cm.sendNext("All ropes weigh differently. Think your next course of action, then try again.");
                                                                        cm.mapMessage(5, "Amos: Hmm... All ropes weigh differently.");
                                                                    }
                                                                    else {
                                                                        cm.sendNext("One rope weigh the same. Think your next course of action, then try again.");
                                                                        cm.mapMessage(5, "Amos: Hmm... One rope weigh the same.");
                                                                    }
                                                                } else {
                                                                    spawnMobs(playersRight);
                                                                    eim.setIntProperty("missCount", 0);
                                                                    if(stage == 2) {
                                                                        eim.setProperty("stage2combo", "");

                                                                        cm.sendNext("You have failed to discover the right combination, now it shall be reset. Start over again!");
                                                                        cm.mapMessage(5, "Amos: You have failed to discover the right combination, now it shall be reset. Start over again!");
                                                                    }
                                                                }

                                                                eim.showWrongEffect();
                                                                cm.dispose();
                                                            }
                                                        } else {
                                                            if(stage == 2) cm.sendNext("It looks like you guys haven't found the ways of this trial yet. Think on an arrangement of 5 members on the platforms. Remember, exactly 5 are allowed to stand on the platforms, and if you move it may not count as an answer, so please keep that in mind. Keep going!");
                                                            else cm.sendNext("It looks like you guys haven't found the ways of this trial yet. Think on an arrangement of party members on different platforms. Remember, exactly 5 are allowed to stand on the platforms, and if you move it may not count as an answer, so please keep that in mind. Keep going!");

                                                            cm.dispose();
                                                        }
                                                } else {
                                                        cm.sendNext("Defeat all mobs before trying for a combination.");
                                                }
                                        }
                                }
                        } else {
                                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                        }
                }
                
                cm.dispose();
        }
}