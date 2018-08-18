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
    NPC ID: 1052013 
    NPC NAME: Computer
    @author Ronan
*/

var status;
var pqArea;
 
function start() {
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
                
                if(cm.getMapId() != 193000000) {
                        var eim = cm.getEventInstance();
                    
                        if(status == 0) {
                                if(!eim.isEventCleared()) {
                                        var couponsNeeded = eim.getIntProperty("couponsNeeded");
                                    
                                        if(cm.isEventLeader()) {
                                                if(cm.haveItem(4001007, couponsNeeded)) {
                                                        cm.sendNext("Your team collected all the needed coupons, good work!");
                                                        cm.gainItem(4001007, couponsNeeded);
                                                        eim.clearPQ();

                                                        cm.dispose();
                                                        return;
                                                } else {
                                                        cm.sendYesNo("Your team must collect #r" + couponsNeeded + "#k coupons to complete this instance. Talk to me when you have the right amount in hands... Or you want to #bquit now#k? Note that if you quit now #ryour team will be forced to quit#k as well.");
                                                }
                                        } else {
                                                cm.sendYesNo("Your team must collect #r" + couponsNeeded + "#k coupons to complete this instance. Let your leader talk to me with the right amount in hands... Or you want to #bquit now#k? Note that if you quit now your team #rmay become undermanned#k to further continue this instance.");
                                        }
                                } else {
                                        if(!eim.giveEventReward(cm.getPlayer())) {
                                                cm.sendOk("Please make a room on your ETC inventory to receive the prize.");
                                                cm.dispose();
                                        } else {
                                                cm.warp(193000000);
                                                cm.dispose();
                                        }
                                }
                        } else if(status == 1) {
                                cm.warp(193000000);
                                cm.dispose();
                        }
                } else {
                        var levels = ["#m190000000#", "#m191000000#", "#m192000000#", "#m195000000#", "#m196000000#", "#m197000000#"];
                        if (status == 0) {
                                var sendStr = "Premium Road is a place of multiple areas with monsters of most various types gathered together, an ideal place for grinding EXP and erasers for the #p1052014#. Select the area you are willing to face:\r\n\r\n#b";
                                for(var i = 0; i < 6; i++) {
                                    sendStr += "#L" + i + "#" + levels[i] + "#l\r\n";
                                }

                                cm.sendSimple(sendStr);
                        } else if (status == 1) {
                                pqArea = selection + 1;
                            
                                em = cm.getEventManager("CafePQ_" + pqArea);
                                if(em == null) {
                                        cm.sendOk("The CafePQ_" + pqArea + "has encountered an error.");
                                        cm.dispose();
                                        return;
                                } else if(cm.isUsingOldPqNpcStyle()) {
                                        status = 1;
                                        action(1, 0, 0);
                                        return;
                                }

                                cm.sendSimple("#e#b<Party Quest: Premium Road - " + levels[selection] + ">\r\n#k#n" + em.getProperty("party") + "\r\n\r\nThe #p1052014# operates differently than the common ones. They do not use mesos or gachapon tickets, rather #rERASERS#k, that can be obtained by completing the missions held on the Premium Road. To go there, you must find partners and attend to a Party Quest. When teamed up and ready, have your #bparty leader#k talk to me.#b\r\n#L0#I want to participate in the party quest.\r\n#L1#I want to find party members.\r\n#L2#I would like to hear more details.");
                        } else if (status == 2) {
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
                                                        if(!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
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
                                        cm.sendOk("#e#b<Party Quest: Premium Road>#k#n\r\nOn the maps ahead, you will face many common-leveled mobs to face on. Grind all the required coupons from them and give it to me. All members will then receive a eraser, corresponding with the level faced. Insert on the machine #bmany of the same eraser or multiple different ones#k to have a better chance on greater prizes.");
                                        cm.dispose();
                                }
                        }
                }
        }
}
