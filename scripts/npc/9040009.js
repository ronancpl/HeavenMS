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
var status;
var stage;

function clearStage(stage, eim) {
        eim.setProperty("stage" + stage + "clear", "true");
        eim.showClearEffect(true);

        eim.giveEventPlayersStageReward(stage);
}

function start() {
    status = -1;
    action (1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        
        var eim = cm.getPlayer().getEventInstance();
        if (eim == null) {
            cm.warp(990001100);
        } else {
            if(eim.getProperty("stage1clear") == "true") {
                cm.sendOk("Excellent work. You may proceed to the next stage.");
                cm.dispose();
                return;
            }
            
            if (cm.isEventLeader()) {
                if (status == 0) {
                    if (eim.getProperty("stage1status") == null || eim.getProperty("stage1status").equals("waiting")) {
                        if (eim.getProperty("stage1phase") == null) {
                            stage = 1;
                            eim.setProperty("stage1phase",stage);
                        } else {
                            stage = parseInt(eim.getProperty("stage1phase"));
                        }
                        
                        if (stage == 1) {
                            cm.sendOk("In this challenge, I shall show a pattern on the statues around me. When I give the word, repeat the pattern to me to proceed.");
                        } else {
                            cm.sendOk("I shall now present a more difficult puzzle for you. Good luck.");
                        }
                    } else if (eim.getProperty("stage1status").equals("active")) {
                        stage = parseInt(eim.getProperty("stage1phase"));
                        
                        if (eim.getProperty("stage1combo").equals(eim.getProperty("stage1guess"))) {
                            if (stage == 3) {
                                cm.getPlayer().getMap().getReactorByName("statuegate").forceHitReactor(1);
                                clearStage(1, eim);
                                cm.getGuild().gainGP(15);

                                cm.sendOk("Excellent work. You may proceed to the next stage.");
                            } else {
                                cm.sendOk("Very good. You still have more to complete, however. Talk to me again when you're ready.");
                                eim.setProperty("stage1phase", stage + 1);
                                cm.mapMessage(5, "You have completed part " + stage + " of the Gatekeeper Test.");
                            }

                        } else {
                            eim.showWrongEffect();
                            cm.sendOk("You have failed this test.");
                            cm.mapMessage(5, "You have failed the Gatekeeper Test.");
                            eim.setProperty("stage1phase","1");
                        }
                        eim.setProperty("stage1status", "waiting");
                        cm.dispose();
                    } else {
                        cm.sendOk("The statues are working on the pattern. Please wait.");
                        cm.dispose();
                    }
                }
                else if (status == 1) {
                    var reactors = getReactors();
                    var combo = makeCombo(reactors);
                    cm.mapMessage(5, "Please wait while the combination is revealed.");
                    var delay = 5000;
                    for (var i = 0; i < combo.length; i++) {
                        cm.getPlayer().getMap().getReactorByOid(combo[i]).delayedHitReactor(cm.getClient(), delay + 3500*i);
                    }
                    eim.setProperty("stage1status", "display");
                    eim.setProperty("stage1combo","");
                    cm.dispose();
                }
            } else {
                cm.sendOk("I need the leader of this instance to speak with me, nobody else.");
                cm.dispose();
            }
        }
    }
}

//method for getting the statue reactors on the map by oid
function getReactors() {
    var reactors = new Array();
        
    var iter = cm.getPlayer().getMap().getReactors().iterator();
    while (iter.hasNext()) {
        var mo = iter.next();
        if (!mo.getName().equals("statuegate")) {
            reactors.push(mo.getObjectId());
        }
    }
    
    return reactors;
}

function makeCombo(reactors) {
    var combo = new Array();
    while (combo.length < (stage + 3)) {
        var chosenReactor = reactors[Math.floor(Math.random() * reactors.length)];
        var repeat = false;
        if (combo.length > 0) {
            for (var i = 0; i < combo.length; i++) {
                if (combo[i] == chosenReactor) {
                    repeat = true;
                    break;
                }
            }
        }
        if (!repeat) {
            combo.push(chosenReactor);
        }
    }
    return combo;
}