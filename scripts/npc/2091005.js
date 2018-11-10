/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>
    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
* @Author: Moogra, XxOsirisxX, Ronan
* @NPC:    2091005
* @Name:   So Gong
* @Map(s): Dojo Hall
*/
importPackage(Packages.server.maps);
importPackage(Packages.constants);

var disabled = false;
var belts = Array(1132000, 1132001, 1132002, 1132003, 1132004);
var belt_level = Array(25, 35, 45, 60, 75);
var belt_on_inventory;
var belt_points;

var status = -1;
var selectedMenu = -1;
var dojoWarp = 0;

function start() {
    if (disabled) {
        cm.sendOk("My master has requested that the dojo be #rclosed#k at this time so I can't let you in.");
        cm.dispose();
        return;
    }
    
    belt_points = ServerConstants.USE_FAST_DOJO_UPGRADE ? Array(10, 90, 200, 460, 850) : Array(200, 1800, 4000, 9200, 17000);
    
    belt_on_inventory = new Array();
    for (var i = 0; i < belts.length; i++) {
        belt_on_inventory.push(cm.haveItemWithId(belts[i], true));
    }
                            
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.getPlayer().setDojoStage(dojoWarp);
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        
        if(status == 0) {
            if (isRestingSpot(cm.getPlayer().getMap().getId())) {
                var text = "I'm surprised you made it this far! But it won't be easy from here on out. You still want the challenge?\r\n\r\n#b#L0#I want to continue#l\r\n#L1#I want to leave#l\r\n";
                if (!cm.getPlayer().getDojoParty()) {
                    text += "#L2#I want to record my score up to this point#l";
                }
                cm.sendSimple(text);
            } else if (cm.getPlayer().getLevel() >= 25) {
                if (cm.getPlayer().getMap().getId() == 925020001) {
                    cm.sendSimple("My master is the strongest person in Mu Lung, and you want to challenge him? Fine, but you'll regret it later.\r\n\r\n#b#L0#I want to challenge him alone.#l\r\n#L1#I want to challenge him with a party.#l\r\n\r\n#L2#I want to receive a belt.#l\r\n#L3#I want to reset my training points.#l\r\n#L4#I want to receive a medal.#l\r\n#L5#What is a Mu Lung Dojo?#l");
                } else {
                    cm.sendYesNo("What, you're giving up? You just need to get to the next level! Do you really want to quit and leave?");
                }
            } else {
                cm.sendOk("Hey! Are you mocking my master? Who do you think you are to challenge him? This is a joke! You should at least be level #b25#k.");
                cm.dispose();
                return;
            }
        } else {
            if (cm.getPlayer().getMap().getId() == 925020001) {
                if (mode >= 0) {
                    if (status == 1)
                        selectedMenu = selection;
                    if (selectedMenu == 0) { //I want to challenge him alone.
                        if (!cm.getPlayer().hasEntered("dojang_Msg") && !cm.getPlayer().getFinishedDojoTutorial()) { //kind of hackish...
                            if (status == 1) {
                                cm.sendYesNo("Hey there! You! This is your first time, huh? Well, my master doesn't just meet with anyone. He's a busy man. And judging by your looks, I don't think he'd bother. Ha! But, today's your lucky day... I tell you what, if you can defeat me, I'll allow you to see my Master. So what do you say?");
                            } else if (status == 2) {
                                if (mode == 0) {
                                    cm.sendNext("Haha! Who are you trying to impress with a heart like that?\r\nGo back home where you belong!");
                                    cm.dispose();
                                    return;
                                } else {
                                    var avDojo = cm.getClient().getChannelServer().getAvailableDojo(true);

                                    if(avDojo < 0) {
                                        if(avDojo == -1) cm.sendOk("All Dojo's are being used already. Wait for awhile before trying again.");
                                        else cm.sendOk("Either your party is already using the Dojo or your party's allotted time on the Dojo has not expired yet. Wait for them to finish to enter.");
                                    }
                                    else {
                                        cm.getClient().getChannelServer().getMapFactory().getMap(925020010 + avDojo).resetMapObjects();
                                        cm.getClient().getChannelServer().resetDojo(925020010 + avDojo);
                                        
                                        cm.resetDojoEnergy();
                                        cm.warp(925020010 + avDojo, 0);
                                    }

                                    cm.dispose();
                                    return;
                                }
                            }
                        } else if (cm.getPlayer().getDojoStage() > 0) {
                            dojoWarp = cm.getPlayer().getDojoStage();
                            cm.getPlayer().setDojoStage(0);
                            cm.sendYesNo("The last time you took the challenge by yourself, you went up to level #b" + dojoWarp + "#k. I can take you there right now. Do you want to go there? (Select #rNo#k to erase this record.)");
                        } else {
                            var avDojo = cm.getClient().getChannelServer().getAvailableDojo(false);

                            if(avDojo < 0) {
                                if(avDojo == -1) cm.sendOk("All Dojo's are being used already. Wait for awhile before trying again.");
                                else cm.sendOk("Either your party is already using the Dojo or your party's allotted time on the Dojo has not expired yet. Wait for them to finish to enter.");
                                
                                cm.getPlayer().setDojoStage(dojoWarp);
                            } else {
                                var warpDojoMap = 925020000 + (dojoWarp + 1) * 100 + avDojo;
                                cm.getClient().getChannelServer().resetDojoMap(warpDojoMap);
                                cm.getClient().getChannelServer().resetDojo(warpDojoMap);
                                
                                cm.resetDojoEnergy();
                                cm.warp(warpDojoMap, 0);
                            }

                            cm.dispose();
                            return;
                        }
                    } else if (selectedMenu == 1) { //I want to challenge him with a party.
                        var party = cm.getPlayer().getParty();
                        if (party == null) {
                            cm.sendNext("Where do you think you're going? You're not even the party leader! Go tell your party leader to talk to me.");
                            cm.dispose();
                            return;
                        }
                        
                        if (party.getLeader().getId() != cm.getPlayer().getId()) {
                            cm.sendNext("Where do you think you're going? You're not even the party leader! Go tell your party leader to talk to me.");
                            cm.dispose();
                            return;
                        }

                        //else if (party.getMembers().size() == 1) {
                        //    cm.sendNext("You're going to take on the challenge as a one-man party?");
                        //}

                        else if (!isBetween(party, 30)) {
                            cm.sendNext("Your partys level ranges are too broad to enter. Please make sure all of your party members are within #r30 levels#k of each other.");
                            cm.dispose();
                            return;
                        } else {
                            var avDojo = cm.getClient().getChannelServer().getAvailableDojo(true, cm.getParty());

                            if(avDojo < 0) {
                                if(avDojo == -1) cm.sendOk("All Dojo's are being used already. Wait for awhile before trying again.");
                                else cm.sendOk("Either your party is already using the Dojo or your party's allotted time on the Dojo has not expired yet. Wait for them to finish to enter.");
                            } else {
                                cm.getClient().getChannelServer().resetDojoMap(925030100 + avDojo);
                                cm.getClient().getChannelServer().resetDojo(925030100 + avDojo);
                                
                                cm.resetPartyDojoEnergy();
                                cm.warpParty(925030100 + avDojo);
                            }

                            cm.dispose();
                            return;
                        }

                    } else if (selectedMenu == 2) { //I want to receive a belt.
                        if (!cm.canHold(belts[0])) {
                            cm.sendNext("Make room in your EQUIP inventory before trying to claim a belt!");
                            cm.dispose();
                            return;
                        }
                        if (mode < 1) {
                            cm.dispose();
                            return;
                        }
                        if (status == 1) {
                            var selStr = "You have #b" + cm.getPlayer().getDojoPoints() + "#k training points. Master prefers those with great talent. If you obtain more points than the average, you can receive a belt depending on your score.\r\n";
                            for (var i = 0; i < belts.length; i++) {
                                if (belt_on_inventory[i]) {
                                    selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "# (Already on inventory)";
                                } else
                                    selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "#";
                            }
                            cm.sendSimple(selStr);
                        } else if (status == 2) {
                            var belt = belts[selection];
                            var level = belt_level[selection];
                            var points = belt_points[selection];
                            
                            var oldbelt = (selection > 0) ? belts[selection - 1] : -1;
                            var haveOldbelt = (oldbelt == -1 || cm.haveItemWithId(oldbelt, false));
                            
                            if (selection > 0 && !belt_on_inventory[selection - 1]) {
                                sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                            } else if (cm.getPlayer().getDojoPoints() >= points) {
                                if (selection > 0 && !haveOldbelt) {
                                    sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                                } else if (cm.getPlayer().getLevel() > level) {
                                    if(selection > 0) cm.gainItem(oldbelt, -1);
                                    cm.gainItem(belt, 1);
                                    cm.getPlayer().setDojoPoints(cm.getPlayer().getDojoPoints() - points);
                                    cm.sendNext("There is the #i" + belt + "# #b#t" + belt + "##k. You have proven your valor to ascend on the Dojo ranks. Well done!");
                                } else
                                    sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                            } else
                                sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);

                            cm.dispose();
                            return;
                        }
                    } else if (selectedMenu == 3) { //I want to reset my training points.
                        if (status == 1) {
                            cm.sendYesNo("You do know that if you reset your training points, it returns to 0, right? Although, that's not always a bad thing. If you can start earning training points again after you reset, you can receive the belts once more. Do you want to reset your training points now?");
                        } else if (status == 2) {
                            if (mode == 0) {
                                cm.sendNext("Do you need to gather yourself or something? Come back after you take a deep breath.");
                            } else {
                                cm.getPlayer().setDojoPoints(0);
                                cm.sendNext("There! All your training points have been reset. Think of it as a new beginning and train hard!");
                            }
                            cm.dispose();
                            return;
                        }
                    } else if (selectedMenu == 4) { //I want to receive a medal.
                        if (status == 1 && cm.getPlayer().getVanquisherStage() <= 0) {
                            cm.sendYesNo("You haven't attempted the medal yet? If you defeat one type of monster in Mu Lung Dojo #b100 times#k you can receive a title called #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k. It looks like you haven't even earned the #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k... Do you want to try out for the #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k?");
                        } else if (status == 2 || cm.getPlayer().getVanquisherStage() > 0) {
                            if (mode == 0) {
                                cm.sendNext("If you don't want to, that's fine.");
                            } else {
                                if (cm.getPlayer().getDojoStage() > 37) {
                                    cm.sendNext("You have completed all medals challenges.");
                                } else if (cm.getPlayer().getVanquisherKills() < 100 && cm.getPlayer().getVanquisherStage() > 0)
                                    cm.sendNext("You still need #b" + (100 - cm.getPlayer().getVanquisherKills()) + "#k in order to obtain the #b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k. Please try a little harder. As a reminder, only the mosnters that have been summoned by our Master in Mu Lung Dojo are considered. Oh, and make sure you're not hunting the monsters and exiting!#r If you don't go to the next level after defeating the monster, it doesn't count as a win#k.");
                                else if (cm.getPlayer().getVanquisherStage() <= 0) {
                                    cm.getPlayer().setVanquisherStage(1);
                                } else {
                                    cm.sendNext("You have obtained #b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k.");
                                    cm.gainItem(1142033 + cm.getPlayer().getVanquisherStage(), 1);
                                    cm.getPlayer().setVanquisherStage(cm.c.getPlayer().getVanquisherStage() + 1);
                                    cm.getPlayer().setVanquisherKills(0);
                                }
                            }

                            cm.dispose();
                            return;
                        } else {
                            cm.dispose();
                            return;
                        }
                    } else if (selectedMenu == 5) { //What is a Mu Lung Dojo?
                        cm.sendNext("Our master is the strongest person in Mu Lung. The place he built is called the Mu Lung Dojo, a building that is #r38 stories#k tall! You can train yourself as you go up each level. Of course, it'll be hard for someone at your level to reach the top.");
                        cm.dispose();
                        return;
                    }
                } else {
                    cm.dispose();
                    return;
                }
            } else if (isRestingSpot(cm.getPlayer().getMap().getId())) {
                if (selectedMenu == -1)
                    selectedMenu = selection;
                
                if (selectedMenu == 0) {
                    var hasParty = (cm.getParty() != null);
                    
                    var firstEnter = false;
                    var avDojo = cm.getClient().getChannelServer().lookupPartyDojo(cm.getParty());
                    if(avDojo < 0) {
                        if(hasParty) {
                            if(!cm.isPartyLeader()) {
                                cm.sendOk("You are not the leader! Call your party leader to talk to me if you wish to continue.");
                                cm.dispose();
                                return;
                            }
                            
                            if(!isBetween(cm.getParty(), 35)) {
                                cm.sendOk("Your partys level ranges are too broad to enter. Please make sure all of your party members are within #r35 levels#k of each other.");
                                cm.dispose();
                                return;
                            }
                        }
                        
                        avDojo = cm.getClient().getChannelServer().getAvailableDojo(hasParty, cm.getParty());
                        firstEnter = true;
                    }

                    if(avDojo < 0) {
                        if(avDojo == -1) cm.sendOk("All Dojo's are being used already. Wait for awhile before trying again.");
                        else cm.sendOk("Your party already registered for the dojo. Wait for the end of the registration time to enter again.");
                    } else {
                        var baseStg = hasParty ? 925030000 : 925020000;
                        var nextStg = Math.floor((cm.getPlayer().getMap().getId() + 100) / 100) % 100;

                        var dojoWarpMap = baseStg + (nextStg * 100) + avDojo;
                        if(firstEnter) {
                            cm.getClient().getChannelServer().resetDojoMap(dojoWarpMap);
                            cm.getClient().getChannelServer().resetDojo(dojoWarpMap, nextStg - 1);
                        }
                        
                        //non-leader party members can progress whilst having the record saved if they don't command to enter the next stage
                        cm.getPlayer().setDojoStage(0);
                        
                        if(!hasParty || !cm.isLeader()) cm.warp(dojoWarpMap, 0);
                        else cm.warpParty(dojoWarpMap, 0);
                    }

                    cm.dispose();
                    return;
                } else if (selectedMenu == 1) { //I want to leave
                    if (status == 1) {
                        cm.sendYesNo("So, you're giving up? You're really going to leave?");
                    } else {
                        if (mode == 1) {
                            cm.warp(925020002);
                        }
                        cm.dispose();
                        return;
                    }
                } else if (selectedMenu == 2) { //I want to record my score up to this point
                    if (status == 1) {
                        cm.sendYesNo("If you record your score, you can start where you left off the next time. Isn't that convenient? Do you want to record your current score?");
                    } else {
                        if (mode == 0) {
                            cm.sendNext("You think you can go even higher? Good luck!");
                        } else if (cm.getPlayer().getDojoStage() == Math.floor(cm.getMapId() / 100) % 100) {
                            cm.sendOk("Your score have already been recorded. Next time you get to challenge the Dojo, you'll be able to come back to this point.");
                        } else {
                            cm.sendNext("I recorded your score. If you tell me the next time you go up, you'll be able to start where you left off. Note that you will have your #rrecord erased#k if you choose to #bcontinue challenging the Dojo#k, so choose carefully.");
                            cm.getPlayer().setDojoStage(Math.floor(cm.getMapId() / 100) % 100);
                        }
                        cm.dispose();
                        return;
                    }
                }
            } else {
                if (mode == 0) {
                    cm.sendNext("Stop changing your mind! Soon, you'll be crying, begging me to go back.");
                } else if (mode == 1) {
                    var dojoMapId = cm.getPlayer().getMap().getId();
                    
                    cm.warp(925020002, 0);
                    cm.getPlayer().message("Can you make up your mind please?");
                    
                    cm.getClient().getChannelServer().freeDojoSectionIfEmpty(dojoMapId);
                }
                cm.dispose();
            }
        }
    }
}

function sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points) {
    var beltReqStr = (oldbelt != -1) ? " you must have the #i" + oldbelt + "# belt in your inventory," : "";
    
    var pointsLeftStr = (points - cm.getPlayer().getDojoPoints() > 0) ? " you need #r" + (points - cm.getPlayer().getDojoPoints()) + "#k more training points" : "";
    var beltLeftStr = (!haveOldbelt) ? " you must have the needed belt unequipped and available in your EQP inventory" : "";
    var conjStr = (pointsLeftStr.length > 0 && beltLeftStr.length > 0) ? " and" : "";
        
    cm.sendNext("In order to receive #i" + belt + "# #b#t" + belt + "##k," + beltReqStr + " you have to be at least over level #b" + level + "#k and you need to have earned at least #b" + points + " training points#k.\r\n\r\nIf you want to obtain this belt," + beltLeftStr + conjStr + pointsLeftStr + ".");
}

function isRestingSpot(id) {
    return (Math.floor(id / 100) % 100) % 6 == 0 && id != 925020001;
}

function isBetween(party, range) {
    var lowest = cm.getPlayer().getLevel();
    var highest = lowest;
    for (var x = 0; x < party.getMembers().size(); x++) {
        var lvl = party.getMembers().get(x).getLevel();
        if (lvl > highest)
            highest = lvl;
        else if (lvl < lowest)
            lowest = lvl;
    }
    return (highest - lowest) <= range;
}
