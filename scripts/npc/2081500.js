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
 *@Author:  Moogra
 *@NPC:     4th Job Pirate Advancement NPC
 *@Purpose: Handles 4th job.
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
    
                if(status == 0) {
                        if(cm.getLevel() < 120 || Math.floor(cm.getJobId() / 100) != 5) {
                                cm.sendOk("Please don't bother me right now, I am trying to concentrate.");
                                cm.dispose();
                        } else if (!cm.isQuestCompleted(6944)) {
                                cm.sendOk("You have not yet passed my trials. I can not advance you until you do so.");
                                cm.dispose();
                        } else if ( cm.getJobId() % 100 % 10 != 2) {
                                cm.sendYesNo("You did a marvellous job passing my test. Are you ready to advance to your 4th job?");
                        } else {
                                cm.sendSimple("If I must, I can teach you the art of your class.\r\n#b#L0#Teach me the skills of my class.#l");
                                //cm.dispose();
                        }
                } else if(status == 1) {
                        if (mode >= 1 && cm.getJobId() % 100 % 10 != 2) {
                                cm.changeJobById(cm.getJobId() + 1);
                                if(cm.getJobId() == 512) {
                                        cm.teachSkill(5121001, 0, 10, -1);
                                        cm.teachSkill(5121002, 0, 10, -1);
                                        cm.teachSkill(5121007, 0, 10, -1);
                                        cm.teachSkill(5121009, 0, 10, -1);
                                } else if(cm.getJobId() == 522) {
                                        cm.teachSkill(5220001, 0, 10, -1);
                                        cm.teachSkill(5220002, 0, 10, -1);
                                        cm.teachSkill(5221004, 0, 10, -1);
                                        cm.teachSkill(5220011, 0, 10, -1);
                                }
                        } else if(mode >= 1 && cm.getJobId() % 100 % 10 == 2) {
                                if(cm.getJobId() == 512) {
                                        if(cm.getPlayer().getSkillLevel(5121003) == 0)
                                                cm.teachSkill(5121003, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5121004) == 0)
                                                cm.teachSkill(5121004, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5121005) == 0)
                                                cm.teachSkill(5121005, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5121010) == 0)
                                                cm.teachSkill(5121010, 0, 10, -1);
                                } else if(cm.getJobId() == 522) {
                                        if(cm.getPlayer().getSkillLevel(5221006) == 0)
                                                cm.teachSkill(5221006, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5221007) == 0)
                                                cm.teachSkill(5221007, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5221008) == 0)
                                                cm.teachSkill(5221008, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5221009) == 0)
                                                cm.teachSkill(5221009, 0, 10, -1);
                                        if(cm.getPlayer().getSkillLevel(5221003) == 0)
                                                cm.teachSkill(5221003, 0, 10, -1);
                                }
                                cm.sendOk("It is done. Leave me now.");
                        }
                        
                        cm.dispose();
                }
        }
}
