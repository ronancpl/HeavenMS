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
* @Author : iAkira, Kevintjuh93
**/
var status = 0; 
var selected = 0;

function start() {
	if (cm.getPlayer().getMapId() == 100000000) {
		cm.sendNext("There! Did you see that? You didn't? A UFO  just passed... there!! Look, someone is getting dragged into the UFO... arrrrrrgh, it's Gaga! #rGaga just got kidnapped by a UFO!#k");
	} else if (cm.getPlayer().getMapId() == 922240200) {
		cm.sendSimple("Did you have something to say...? #b\b\r\n#L0#I want to rescue Gaga.#l\r\n#L1#I want to go to the Space Mine.#l");
	} else if (cm.getPlayer().getMapId() >= 922240000 && cm.getPlayer().getMapId() <= 922240019) {
		cm.sendYesNo("Don't worry if you fail. You'll have 3 chances. Do you still want to give up?"); 
	} else if (cm.getPlayer().getMapId() >= 922240100 && cm.getPlayer().getMapId() <= 922240119) {
		var text = "You went through so much trouble to rescue Gaga, but it looks like we're back to square one. ";				
		var rgaga = cm.getPlayer().getEvents().getGagaRescue();
		if (rgaga.getCompleted() == 10 || rgaga.getCompleted() == 20) {
			text += "Please don't give up untill Gaga is rescued. To show you my appreciation for what you've accomplished thus far, I've given you a Spaceship. It's rather worn out, but it should still be operational. Check your #bSkill Window#k.";
			rgaga.giveSkill(cm.getPlayer());
		} else 
			text += "Let's go back now.";
					
		cm.sendNext(text); 
	}
}

function action(m,t,s) { 
	if (m > 0) {
		status++; 
		if (cm.getPlayer().getMapId() == 100000000) { // warper completed
			if (status == 1) {
				if (cm.getPlayer().getLevel() >= 12) 
					cm.sendYesNo("What do we do now? It's just a rumor yet, but... I've heard that scary things happen to you if you get kidnapped by aliens... may be that's what happenning to Gaga right now! Please, please rescue Gaga! \r\n #bGaga may be a bit indetermined and clueless, but#k he has a really good heart. I can't let something terrible happen to him. Right! Grandpa from the moon might know how to rescue him! I will send you to the moon, so please go meet Grandpa and rescue Gaga");
				else 
					cm.sendOk("Oh! it seems you don't reach the level requirements to save Gaga. Please come back when you are level 12 or higher.");
          
			} else if (status == 2)
				cm.sendNext("Thank you so much. Please rescue Gaga! Grandpa from the moon will help you.");
			else if (status == 3) {
				cm.warp(922240200, 0); 
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 922240200) {
			if (status == 1) {
				if(s == 0) {
					selected = 1;
					cm.sendNext("Welcome! I heard what happened from Baby Moon Bunny I'm glad you came since I was Planning on requesting some help. Gaga is a friend of mine who has helped me before and often stops by to say hello. Unfortunaley, he was kidnapped by aliens."); 
				} else {
					selected = 2;
					cm.sendYesNo("At the Space Mine, you can find special ores called #bKrypto Crystals#k that contains the mysterious power of space. #bKrypto Crystals#l are usually emerald in color, but will turn brown if hit with the Spaceship's #bSpace Beam#k. Remember, in order to thwart this alien conspracy, #b10 Brown Krypto Crystal's and 10 Emerald Krypto Crystal's are needed. But since even #b1 Krypto Crystal#k can be of help, brign me as many as possible. Oh, and one more thing! The Space Mines are protected by the Space Mateons. They are extemely strong due to the power of the #Krypto Crystals#k, so don't try to defeat them. Simply concentrate on quickly collecting the crystals."); 
				} 
			} else if (status == 2) {
				if(selected == 1) {
					cm.sendYesNo("If we just leave Gaga with the aliens, something terrible will happen to him! I'll let you borrow a spaceship that the Moon Bunnies use for traveling so that you can rescue Gaga.#b Although he might appear a bit indecieve, slow, and immature at times#k, he's really a nice young man. Do you want to go rescue him now?");
				} else if(selected == 2) { 
					cm.sendOk("Not coded yet, f4."); 
					cm.dispose();
				}
			} else  if (status == 3) {
				var number = -1;
				for (var i = 0; i < 20; i++) {
					var mapFactory = cm.getClient().getChannelServer().getMapFactory();
					if (mapFactory.getMap(922240000 + i).getCharacters().isEmpty()) {
						number = i;
						break;
					}	    
				}
				if (number > -1) 
					cm.warp(922240000 + number);
				else 
					cm.sendOk("There are currently no empty maps, please try again later.");
				
				cm.dispose();
			}
		} else if ((cm.getPlayer().getMapId() >= 922240000 && cm.getPlayer().getMapId() <= 922240019) || (cm.getPlayer().getMapId() >= 922240100 && cm.getPlayer().getMapId() <= 922240119)) {
			cm.warp(922240200, 0);
			cm.dispose();
		}
	} else if (m < 1) {
		if(m == 0) {
			if (cm.getPlayer().getMapId() == 922240200)  {
				cm.sendOk("That's a shame, come back when your ready.");
			}
		}
		cm.dispose();
	}
}