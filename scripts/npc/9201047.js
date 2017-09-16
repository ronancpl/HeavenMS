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
/* The Glimmer Man
	Amoria PQ Stg1/exit
 */

var status;
var curMap, stage;
 
function start() {
        curMap = cm.getMapId();
        stage = Math.floor((curMap - 670010200) / 100) + 1; 
    
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
    
                if(status == 0) {
                        if(cm.getMapId() != 670010200) {
                            cm.sendYesNo("So, are you going to leave this place?");
                        } else {
                            if(cm.isEventLeader()) {
                                var eim = cm.getEventInstance();
                                var st = eim.getIntProperty("statusStg" + stage);
                                
                                if(cm.haveItem(4031595, 1)) {
                                    cm.gainItem(4031595, -1);
                                    eim.setIntProperty("statusStg" + stage, 1);

                                    cm.sendOk("You retrieved the #t4031595#, splendid! You may report to Amos about your success on this task.");
                                } else if(st < 1 && cm.getMap().countMonsters() == 0) {
                                    eim.setIntProperty("statusStg" + stage, 1);
                                    
                                    var mapObj = cm.getMap();
                                    mapObj.toggleDrops();
                                    
                                    var mobObj = Packages.server.life.MapleLifeFactory.getMonster(9400518);
                                    mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(-245, 810));
                                    
                                    cm.sendOk("The fierry appeared! Defeat it to get the #b#t4031596##k!");
                                } else {
                                    if(st < 1) cm.sendOk("Your task is to recover a shard of the Magik Mirror. To do so, you will need a #b#t4031596##k, that drops on a fierry that appears when all other mobs are killed. To access the rooms the mobs are, pick the portal corresponding to your gender and kill all mobs there. Ladies take the left side, gentlemen the right side.");
                                    else cm.sendOk("Your task is to recover a shard of the Magik Mirror. Defeat the fierry to get the #b#t4031596##k.");
                                }
                            } else {
                                cm.sendOk("Your task is to recover a shard of the Magik Mirror. To do so, you will need a #b#t4031596##k, that drops on a fierry that appears when all other mobs are killed. To access the rooms the mobs are, pick the portal corresponding to your gender and kill all mobs there. Ladies take the left side, gentlemen the right side. #bYour leader#k must bring the #b#t4031595##k to have my pass.");
                            }
                            
                            cm.dispose();
                        }
                } else if(status == 1) {
                    cm.warp(670010000, "st00");
                    cm.dispose();
                }
        }
}