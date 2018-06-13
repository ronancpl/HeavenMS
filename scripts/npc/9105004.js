/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
 * @Author Ronan
 * Snow Spirit
	Maplemas PQ coordinator
 */

importPackage(Packages.server.life);

var prizeTree = [[[2000002, 1002850], [20, 1]], [[2000006, 1012011], [20, 1]]];

var state;
var status;
var gift;
var pqType;
 
function start() {
        pqType = ((cm.getMapId() / 10) % 10) + 1;
        state = (cm.getMapId() % 10 > 0) ? 1 : 0;
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(state > 0) {
                    insidePqAction(mode, type, selection);
                } else {
                    recruitPqAction(mode, type, selection);
                }
        }
}

function recruitPqAction(mode, type, selection) {
        if (status == 0) {
                em = cm.getEventManager("HolidayPQ_" + pqType);
                if(em == null) {
                        cm.sendOk("The Holiday PQ " + pqType + " has encountered an error.");
                        cm.dispose();
                } else if(cm.isUsingOldPqNpcStyle()) {
                        action(1, 0, 0);
                        return;
                }

                cm.sendSimple("#e#b<Party Quest: Holiday>\r\n#k#n" + em.getProperty("party") + "\r\n\r\nHow about you and your party members collectively beating a quest? Here you'll find obstacles and problems where you won't be able to beat it without great teamwork. If you want to try it, please tell the #bleader of your party#k to talk to me.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
        } else if (status == 1) {
                if (selection == 0) {
                        if (cm.getParty() == null) {
                                cm.sendOk("You can participate in the party quest only if you are in a party.");
                                cm.dispose();
                        } else if(!cm.isLeader()) {
                                cm.sendOk("Your party leader must talk to me to start this party quest.");
                                cm.dispose();
                        } else {
                                var eli = em.getEligibleParty(cm.getParty());
                                if(eli.size() > 0) {
                                        if(!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), pqType)) {
                                                cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
                                        }
                                }
                                else {
                                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                                }

                                cm.dispose();
                        }
                } else if (selection == 1) {
                        cm.sendOk("Try using a Super Megaphone or asking your buddies or guild to join!");
                        cm.dispose();
                } else {
                        cm.sendOk("#e#b<Party Quest: Holiday>#k#n\r\n\r\nJoin in with your team to build up the Snowman that will protect Happyville from the misdoings of Scrooge. While inside, work out with your team to protect it at any means necessary while collecting Snow Vigor that will help on the build up of the Snowman.");
                        cm.dispose();
                }
        }
}

function insidePqAction(mode, type, selection) {
        var eim = cm.getEventInstance();
        var difficulty = eim.getIntProperty("level");
        var stg = eim.getIntProperty("statusStg1");

        var mapobj = eim.getInstanceMap(889100001 + 10 * (difficulty - 1));

        if(status == 0) {
                if(stg == -1) {
                        cm.sendNext("#b#h0##k... you're finally here. This is the place where the residents of Happyville build the giant snowman. But Scrooge's subordinates are attacking it right now. Now Hurry! Our mission is for you and your party to protect the snowman from Scrooge's men within the time limit. If you eliminate them, then they'll drop an item called Snow Vigor. Gather them up and drop them on the snowman, and you'll literally see it grow. Once it returns to its original size, then your task is complete. Just beware of one thing. Some of the subordinates may drop a fake Snow Vigor. A fake Snow Vigor will actually cause the snowman to melt even faster than usual. Best of luck to you.");
                } else if(stg == 0) {
                        if(cm.getMap().getMonsterById(9400321 + 5 * difficulty) == null) {
                                cm.sendNext("Please, defeat Scrooge's underlings and make the snowman grow, so that Scrooge has no other way to avoid showing himself up.");
                                cm.dispose();
                        } else {
                                cm.sendNext("Awesome! Just as I expected, you managed to defeat Scrooge's subordinates. Thank you so much! (Stands silent for a while...) Unfortunately, Scrooge doesn't seem like he's going to stop right here. One of his men have already told him what happened, which means... he'll show up soon. Please keep fighting, and again, best of luck to you.");
                        }
                } else {
                        if(!eim.isEventCleared()) {
                                cm.sendNext("Please defeat the Scrooge, so our Maplemas keeps safe from harm!");
                                cm.dispose();
                        } else {
                                cm.sendNext("Wow!! You defeated Scrooge! Thank you so much! You have managed to make this Maplemas safe and sound! Thanks!!");
                        }
                }
        } else if(status == 1) {
                if(stg == -1) {
                        if(!cm.isEventLeader()) {
                                cm.sendOk("Please let your party leader talk to me for further details on the mission.");
                                cm.dispose();
                                return;
                        }

                        mapobj.allowSummonState(true);
                        var snowman = MapleLifeFactory.getMonster(9400317 + (5 * difficulty));
                        mapobj.spawnMonsterOnGroundBelow(snowman, new java.awt.Point(-180, 15));
                        eim.setIntProperty("snowmanLevel", 1);
                        eim.dropMessage(5, "The snowman appeared on the field! Protect it using all means necessary!");

                        eim.setIntProperty("statusStg1", 0);
                        cm.dispose();
                        return;
                } else if(stg == 0) {
                        if(!cm.isEventLeader()) {
                                cm.sendOk("Please let your party leader talk to me for further details on the mission.");
                                cm.dispose();
                                return;
                        }

                        mapobj.broadcastStringMessage(5, "As the snowman grows to it's prime, the Scrooge appears!");
                        eim.getEm().getIv().invokeFunction("snowmanHeal", eim);

                        var boss = MapleLifeFactory.getMonster(9400318 + difficulty);
                        mapobj.spawnMonsterOnGroundBelow(boss, new java.awt.Point(-180, 15));
                        eim.setProperty("spawnedBoss", "true");

                        eim.setIntProperty("statusStg1", 1);
                        cm.dispose();
                } else {
                        gift = cm.haveItem(4032092, 1);
                        if(gift) {
                                var optStr = generateSelectionMenu(generatePrizeString());
                                cm.sendSimple("Oh, you brought a #b#t4032092##k with you? That's nice, hold on a bit... Here's your Maplemas gift. Please select the one you'd like to receive:\r\n\r\n" + optStr);
                        } else if(eim.gridCheck(cm.getPlayer()) == -1) {
                                cm.sendNext("Here's your Maplemas gift. Enjoy~");
                        } else {
                                cm.sendOk("Happy Maplemas!!");
                                cm.dispose();
                        }
                }

        } else if(status == 2) {
                if(gift) {
                        var selItems = prizeTree[selection];
                        if(cm.canHoldAll(selItems[0], selItems[1])) {
                                cm.gainItem(4032092, -1);
                                cm.gainItem(selItems[0][0], selItems[1][0]);

                                if(selection == 1) {
                                        var rnd = (Math.random() * 9) | 0;
                                        cm.gainItem(selItems[0][1] + rnd, selItems[1][1]);
                                } else {
                                        cm.gainItem(selItems[0][1], selItems[1][1]);
                                }
                        } else {
                                cm.sendOk("Please make sure you have room in your EQUIP and USE inventories before proceeding.");
                        }
                } else {
                        if(eim.giveEventReward(cm.getPlayer(), difficulty)) {
                                eim.gridInsert(cm.getPlayer(), 1);
                        } else {
                                cm.sendOk("Please make sure you have room in your EQUIP, USE and ETC inventories before proceeding.");
                        }
                }

                cm.dispose();
        }
}

function generatePrizeString() {
        var strTree = [];
        
        for(var i = 0; i < prizeTree.length; i++) {
                var items = prizeTree[i][0];
                var qtys = prizeTree[i][1];

                var strSel = "";
                for(var j = 0; j < items.length; j++) {
                        strSel += ("#i" + items[j] + "# #t" + items[j] + "#" + (qtys[j] > 1 ? (" : " + qtys[j]) : ""));
                }

                strTree.push(strSel);
        }
        
        return strTree;
}

function generateSelectionMenu(array) {
        var menu = "";
        for (var i = 0; i < array.length; i++) {
                menu += "#L" + i + "#" + array[i] + "#l\r\n";
        }
        return menu;
}