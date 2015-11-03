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
 *9201023 - Nana(K)
 *@author Jvlaple
 */
var status = -1; 


function start() {
	cm.sendOk("Your skills have been added!");
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
	      if (cm.getPlayer().getJob().getId() > 2000) {
	    	  if (cm.getPlayer().getSkillLevel(21001003) < 1)
	    	  	cm.teachSkill(21001003, 0, 20,-1); // Pole Arm Booster
	    	  if (cm.getPlayer().getSkillLevel(21100004) < 1)  
	    		  cm.teachSkill(21100004,0, 20,-1); // Combo Smash
	    	  if (cm.getPlayer().getSkillLevel(21100005) < 1)
		        cm.teachSkill(21100005,0, 20,-1); // Combo Drain
	    	  if (cm.getPlayer().getSkillLevel(21100000) < 1)
		        cm.teachSkill(21100000,0, 20,-1); // Pole Arm Mastery
	    	  if (cm.getPlayer().getSkillLevel(21100002) < 1)
		        cm.teachSkill(21100002,0, 30,-1); // Final Charge
	    	  if (cm.getPlayer().getSkillLevel(21110002) < 1)
		        cm.teachSkill(21110002,0, 20,-1); // Full Swing
	    	  if (cm.getPlayer().getSkillLevel(21110002) > 1)
			        cm.teachSkill(21110007, cm.getPlayer().getSkillLevel(21110002), 20,-1); // Full Swing
	    	  if (cm.getPlayer().getSkillLevel(21110002) > 1)
			        cm.teachSkill(21110008, cm.getPlayer().getSkillLevel(21110002), 20,-1); // Full Swing
	      } else if (cm.getPlayer().getJob().getId() == 232) {
	    	  if (cm.getPlayer().getSkillLevel(2321008) < 1)
	    		  cm.teachSkill(2321008, 0, 10,-1); // Genesis 
	    	  if (cm.getPlayer().getSkillLevel(2321006) < 1)
		    	  cm.teachSkill(2321006, 0, 10,-1); // res 	    	  
	      } else if (cm.getPlayer().getJob().getId() == 112) { 
	    	  if (cm.getPlayer().getSkillLevel(1121006) < 1)
	    		  cm.teachSkill(1121006, 0, 10,-1); // pali rush
	    	  if (cm.getPlayer().getSkillLevel(1121002) < 1)
		    	  cm.teachSkill(1121002, 0, 10,-1); // power stance	 	    	  
	      } else if (cm.getPlayer().getJob().getId() == 122) { 
	    	  if (cm.getPlayer().getSkillLevel(1221007) < 1)
	    		  cm.teachSkill(1221007, 0, 10,-1); // hero rush	  
	    	  if (cm.getPlayer().getSkillLevel(1221002) < 1)
		    	  cm.teachSkill(1221002, 0, 10,-1); // power stance	  	    	  
	      } else if (cm.getPlayer().getJob().getId() == 132) { 
	    	  if (cm.getPlayer().getSkillLevel(1320006) < 1)
	    	  cm.teachSkill(1320006, 0, 10,-1); // Berzerk
	    	  if (cm.getPlayer().getSkillLevel(1321003) < 1)
	    	  cm.teachSkill(1321003, 0, 10,-1); // DrK Rush
	    	  if (cm.getPlayer().getSkillLevel(1321002) < 1)
		    	  cm.teachSkill(1321002, 0, 10,-1); // power stance	 	    	  
	      } else if (cm.getPlayer().getJob().getId() == 312) { 
	    	  if (cm.getPlayer().getSkillLevel(3121008) < 1)
	    	  cm.teachSkill(3121008, 0, 10,-1); // Concentrate	    	  
	      } else if (cm.getPlayer().getJob().getId() == 512) { 
	    	  if (cm.getPlayer().getSkillLevel(5121003) < 1)
		    	  cm.teachSkill(5121003, 0, 10,-1); // super transformation
	    	  if (cm.getPlayer().getSkillLevel(5121004) < 1)
		    	  cm.teachSkill(5121004, 0, 10,-1); // demo
	    	  if (cm.getPlayer().getSkillLevel(5121005) < 1)
		    	  cm.teachSkill(5121005, 0, 10,-1); // snatch
	      } else if (cm.getPlayer().getJob().getId() == 522) { 
	    	  if (cm.getPlayer().getSkillLevel(5221006) < 1)
		    	  cm.teachSkill(5221006, 0, 10,-1); // ship
	    	  if (cm.getPlayer().getSkillLevel(5221007) < 1)
		    	  cm.teachSkill(5221007, 0, 10,-1); // cannon
	    	  if (cm.getPlayer().getSkillLevel(5221008) < 1)
		    	  cm.teachSkill(5221008, 0, 10,-1); // torpedo
	    	  if (cm.getPlayer().getSkillLevel(5221009) < 1)
		    	  cm.teachSkill(5221009, 0, 10,-1); // hypno 
	    	  if (cm.getPlayer().getSkillLevel(5221003) < 1)
		    	  cm.teachSkill(5221003, 0, 10,-1); // air strike	    	  
	      } else if (cm.getPlayer().getJob().getId() == 422) { 
	    	  if (cm.getPlayer().getSkillLevel(4221001) < 1)
		    	  cm.teachSkill(4221001, 0, 10,-1); // assassinate
	    	  if (cm.getPlayer().getSkillLevel(4221006) < 1)
		    	  cm.teachSkill(4221006, 0, 10,-1); // smoke screen  
	      } else if (cm.getPlayer().getJob().getId() == 222) { 
	    	  if (cm.getPlayer().getSkillLevel(2221007) < 1)
		    	  cm.teachSkill(2221007, 0, 10,-1); // blizzard
	      } else if (cm.getPlayer().getJob().getId() == 212) { 
	    	  if (cm.getPlayer().getSkillLevel(2121007) < 1)
		    	  cm.teachSkill(2121007, 0, 10,-1); // meteor	    	  
	    	  
	      }
	    	  cm.dispose();
        /*
        if (cm.getPlayer().getMarriageQuestLevel() == 1 || cm.getPlayer().getMarriageQuestLevel() == 52) {
            if (!cm.haveItem(4000015, 40)) {
                if (status == 0) {
                    cm.sendNext("Hey, you look like you need proofs of love? I can get them for you.");
                } else if (status == 1) {
                    cm.sendNext("All you have to do is bring me 40 #bHorned Mushroom Caps#k.");
                    cm.dispose();
                }
            } else {
                if (status == 0) {
                    cm.sendNext("Wow, you were quick! Heres the proof of love...");
                    cm.gainItem(4000015, -40)
                    cm.gainItem(4031367, 1);
                    cm.dispose();
                }
            }
        } else {
            cm.sendOk("Hi, I'm Nana the love fairy... Hows it going?");
            cm.dispose();
        }*/
    }
}