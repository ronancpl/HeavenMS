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
		cm.sendNext("There! Did you see that? You didn't? A UFO just passed... there!! Look, someone is getting dragged into the UFO... arrrrrrgh, it's Gaga! #rGaga just got kidnapped by a UFO!#k");
	}
}

function action(m,t,s) { 
	if (m > 0) {
		status++; 
		if (cm.getPlayer().getMapId() == 100000000) { // warper completed
			if (status == 1) {
				if (cm.getPlayer().getLevel() >= 12) 
					cm.sendYesNo("What do we do now? It's just a rumor yet, but... I've heard that scary things happen to you if you get kidnapped by aliens... may be that's what happenning to Gaga right now! Please, please rescue Gaga! \r\n #bGaga may be a bit indetermined and clueless, but#k he has a really good heart. I can't let something terrible happen to him. Right! Grandpa from the moon might know how to rescue him! I will send you to the moon, so please go meet Grandpa and rescue Gaga!!!");
				else 
					cm.sendOk("Oh! It seems you don't reach the level requirements to save Gaga. Please come back when you are level 12 or higher.");
          
			} else if (status == 2)
				cm.sendNext("Thank you so much. Please rescue Gaga! Grandpa from the moon will help you.");
			else if (status == 3) {
				cm.warp(922240200, 0); 
				cm.dispose();
			}
		}
	} else if (m < 1) {
		cm.dispose();
	}
}