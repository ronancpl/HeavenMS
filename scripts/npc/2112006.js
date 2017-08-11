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

/**
 * @author: Ronan
 * @npc: Romeo
 * @func: MagatiaPQ area NPC
*/

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
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
                    
                var eim = cm.getEventInstance();
                
                if(!eim.isEventCleared()) {
                        if(status == 0) {
                                if(eim.getIntProperty("npcShocked") == 0 && cm.haveItem(4001131, 1)) {
                                        cm.gainItem(4001131, -1);
                                        eim.setIntProperty("npcShocked", 1);
                                        
                                        cm.sendNext("Oh? You got a letter for me? On times like this, what should it be... Gasp! Something big is going on, guys. Rally yourselves, from now on things will be harder than ever!");
                                        eim.dropMessage(6, "Romeo seemed very much in shock after reading Juliet's Letter.");
                                        
                                        cm.dispose();
                                        return;
                                } else if (eim.getIntProperty("statusStg4") == 1) {
                                        var door = cm.getMap().getReactorByName("rnj3_out3");
                                    
                                        if(door.getState() == 0) {
                                                cm.sendNext("Let me open the door for you.");
                                                door.hitReactor(cm.getClient());
                                        } else {
                                                cm.sendNext("Please hurry, Juliet is in trouble.");
                                        }
                                        
                                        cm.dispose();
                                        return;
                                } else if (cm.haveItem(4001134, 1) && cm.haveItem(4001135, 1)) {
                                        if (cm.isEventLeader()) {
                                                cm.gainItem(4001134, -1);
                                                cm.gainItem(4001135, -1);
                                                cm.sendNext("Great! You got both Alcadno and Zenumist files at hand. Now we can proceed.");

                                                eim.showClearEffect();
                                                eim.giveEventPlayersStageReward(4);
                                                eim.setIntProperty("statusStg4", 1);
                                                
                                                cm.getMap().killAllMonsters();
                                                cm.getMap().getReactorByName("rnj3_out3").hitReactor(cm.getClient());
                                        } else {
                                                cm.sendOk("Please let your leader pass the files to me.");
                                        }

                                        cm.dispose();
                                        return;
                                } else {
                                        cm.sendYesNo("We must keep fighting to save Juliet, please keep your pace. If you are not feeling so well to continue, your companions and I will understand... So, are you going to retreat?");
                                }
                        } else {
                                cm.warp(926100700);
                                cm.dispose();
                        }
                } else {
                        if(status == 0) {
                                if(eim.getIntProperty("escortFail") == 0) {
                                        cm.sendNext("Finally, Juliet is safe! Thanks to your efforts, we could save her from the clutches of Yulete, who will now be judged for his rebellion against Magatia. From now on, as he will start rehabilitation, we will keep an eye on his endeavours, making sure he will cause no more troubles on the future.");
                                }
                                else {
                                        cm.sendNext("Juliet is safe now, although the battle took it's toll on her... Thanks to your efforts, we could save her from the clutches of Yulete, who will now be judged for his rebellion against Magatia. Thank you.");
                                        status = 2;
                                }
                        } else if(status == 1) {
                                cm.sendNext("Now, please receive this gift as an act of acceptation for our gratitude.");
                        } else if(status == 2) {
                                if(cm.canHold(4001159)) {
                                        cm.gainItem(4001159, 1);
                                        
                                        if(eim.getIntProperty("normalClear") == 1) cm.warp(926100600);
                                        else cm.warp(926100500);
                                } else {
                                        cm.sendOk("Make sure you have a space on your ETC inventory.");
                                }
                                
                                cm.dispose();
                        } else {
                                cm.warp(926100600);
                                cm.dispose();
                        }
                }
        }
}