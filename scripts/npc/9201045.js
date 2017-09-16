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
@	Description: Last stages of the Amorian Challenge
*/

var debug = false;
var status = 0;
var curMap, stage;

function isAllGatesOpen() {
    var map = cm.getPlayer().getMap();
    
    for(var i = 0; i < 7; i++) {
        var gate = map.getReactorByName("gate0" + i);
        if(gate.getState() != 4) {
            return false;
        }
    }
    
    return true;
}

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    
    eim.showClearEffect(true);
    eim.linkToNextStage(stage, "apq", curMap);  //opens the portal to the next map
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
                        if(stage < 5) cm.sendNext("The portal is already open, advance for the trials that awaits you there.");
                        else if(stage == 5) eim.warpEventTeamToMapSpawnPoint(670010700, 0);
                        else {
                                if(cm.isEventLeader()) {
                                        if(eim.getIntProperty("marriedGroup") == 0) {
                                                eim.restartEventTimer(1 * 60 * 1000);
                                                eim.warpEventTeam(670010800);
                                        } else {
                                                eim.setIntProperty("marriedGroup", 0);

                                                eim.restartEventTimer(2 * 60 * 1000);
                                                eim.warpEventTeamToMapSpawnPoint(670010750, 1);
                                        }
                                } else {
                                        cm.sendNext("Wait for the leader's command to start the bonus phase.");
                                }
                        }
                }
                else {
                        if(stage != 6) {
                                if (eim.isEventLeader(cm.getPlayer())) {
                                        var state = eim.getIntProperty("statusStg" + stage);

                                        if(state == -1) {           // preamble
                                                if(stage == 4) cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k of the Amorian Challenge. In this stage, collect me #b50 #t4031597##k from the mobs around here.");
                                                else if(stage == 5) cm.sendOk("Hi. Welcome to the #bstage " + stage + "#k of the Amorian Challenge. That was quite the run to reach here, eh? Well, that was your task this stage here, anyway: survival! Firstly, have anyone alive gathered here before challenging the boss.");

                                                var st = (debug) ? 2 : 0;
                                                eim.setProperty("statusStg" + stage, st);
                                        }
                                        else {       // check stage completion
                                                if(stage == 4) {
                                                        if(cm.haveItem(4031597, 50)) {
                                                            cm.gainItem(4031597, -50);

                                                            var tl = eim.getTimeLeft();
                                                            if(tl >= 5 * 60 * 1000) {
                                                                eim.setProperty("timeLeft", tl);
                                                                eim.restartEventTimer(4 * 60 * 1000);
                                                            }

                                                            cm.sendNext("Well done! Let me open the gate for you now.");
                                                            cm.mapMessage(5, "Amos: The time runs short now. Your objective is to open the gates and gather together on the other side of the next map. Good luck!");
                                                            clearStage(stage, eim, curMap);
                                                        } else {
                                                            cm.sendNext("Hey, didn't you pay heed? I demand #r50 #t4031597##k for the success of this trial.");
                                                        }

                                                } else if(stage == 5) {
                                                        var pass = true;

                                                        if(eim.isEventTeamTogether()) {
                                                            var party = cm.getEventInstance().getPlayers();
                                                            var area = cm.getMap().getArea(2);

                                                            for (var i = 0; i < party.size(); i++) {
                                                                    var chr = party.get(i);

                                                                    if (chr.isAlive() && !area.contains(chr.getPosition())) {
                                                                        pass = false;
                                                                        break;
                                                                    }
                                                            }
                                                        } else {
                                                            pass = false;
                                                        }

                                                        if(pass) {
                                                                if(isAllGatesOpen()) {
                                                                    var tl = eim.getProperty("timeLeft");
                                                                    if(tl != null) {
                                                                        var tr = eim.getTimeLeft();

                                                                        var tl = parseFloat(tl);
                                                                        eim.restartEventTimer(tl - (4 * 60 * 1000 - tr));
                                                                    }

                                                                    cm.sendNext("Okay, your team is already gathered. Talk to me when you guys feel ready to fight the #rGeist Balrog#k.");

                                                                    cm.mapMessage(5, "Amos: Now only the boss fight remains! Once inside, talk to me only if you want to join the boss fight, you will be transported to action immediately.");
                                                                    clearStage(stage, eim, curMap);
                                                                } else {
                                                                    cm.sendNext("You guys reached here by teleporting, eh? I can tell it. This is a shame, all gates needs to be open to fulfill this stage. If you still have the time, backtrack your steps and take down those gates.");
                                                                }
                                                        } else {
                                                                cm.sendNext("Your team has not gathered nearby yet. Give them some time to reach here.");
                                                        }
                                                }
                                        }
                                } else {
                                        cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                                }
                        } else {
                                var area = cm.getMap().getArea(0);
                                if (area.contains(cm.getPlayer().getPosition())) {
                                        if(cm.getPlayer().isAlive()) {
                                                cm.warp(670010700, "st01");
                                        } else {
                                                cm.sendNext("Oy stand back... You are already dead.");
                                        }
                                } else {
                                        if(cm.isEventLeader()) {
                                                if(cm.haveItem(4031594, 1)) {
                                                        cm.gainItem(4031594, -1);
                                                        cm.sendNext("Congratulations! Your party defeated the Geist Balrog, thus #bcompleting the Amorian Challenge#k! Talk to me again to start the bonus stage.");

                                                        clearStage(stage, eim, curMap);
                                                        eim.clearPQ();
                                                } else {
                                                        cm.sendNext("How is it? Are you going to retrieve me the #b#t4031594##k? That's your last trial, hold on!")
                                                }
                                        } else {
                                                cm.sendNext("Please tell your #bParty-Leader#k to come talk to me.");
                                        }
                                }
                        }
                }
                
                cm.dispose();
        }
}