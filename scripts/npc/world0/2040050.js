/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Eurek the Alchemist - Multiple Place
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var menu = "";
var set;
var makeitem;
var access = true;
var reqitem = new Array();
var cost = 4000;
var makeditem = new Array(4006000,4006001);
var reqset = new Array([[[4000046,20],[4000027,20],[4021001,1]],
						[[4000025,20],[4000049,20],[4021006,1]],
						[[4000129,15],[4000130,15],[4021002,1]],
						[[4000074,15],[4000057,15],[4021005,1]],
						[[4000054,7],[4000053,7],[4021003,1]]],
						
						[[[4000046,20],[4000027,20],[4011001,1]],
						[[4000014,20],[4000049,20],[4011003,1]],
						[[4000132,15],[4000128,15],[4011005,1]],
						[[4000074,15],[4000069,15],[4011002,1]],
						[[4000080,7],[4000079,7],[4011004,1]]]);

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if(mode == -1 || (mode == 0 && (status ==1 || status == 2))) {
		cm.dispose();
		return;
	}
	if(mode == 0) {
		cm.sendNext("Not enough materials, huh? No worries. Just come see me once you gather up the necessary items. There are numerous ways to obtain them, whether it be hunting or purchasing it from others, so keep going.");
		cm.dispose();
	}
	if(mode == 1) {
		status++;
	}
	if(status == 0) {
		cm.sendNext("Alright, mix up the frog's tongue with the squirrel's tooth and ... oh yeah! Forgot to put in the sparkling white powder!! Man, that could have been really bad ... Whoa!! How long have you been standing there? I maaaay have been a little carried away with my work ... hehe.");
	} else if(status == 1) {
		cm.sendSimple("As you can see, I'm just a traveling alchemist. I may be in training, but I can still make a few things that you may need. Do you want to take a look?\r\n\r\n#L0##bMake Magic Rock#k#l\r\n#L1##bMake The Summoning Rock#k#l");
	} else if(status == 2) {
		set = selection;
		makeitem = makeditem[set];
		for(i=0; i < reqset[set].length; i++) {
			menu += "\r\n#L"+i+"##bMake it using #t"+reqset[set][i][0][0]+"# and #t"+reqset[set][i][1][0]+"##k#l";
		}
		cm.sendSimple("Haha... #b#t"+makeitem+"##k is a mystical rock that only I can make. Many travelers seems to need this for most powerful skills that require more than just MP and HP. There are 5 ways to make #t"+makeitem+"#. Which way do you want to make it?"+menu);
	} else if(status == 3) {
		set = reqset[set][selection];
		reqitem[0] = new Array(set[0][0],set[0][1]);
		reqitem[1] = new Array(set[1][0],set[1][1]);
		reqitem[2] = new Array(set[2][0],set[2][1]);
		menu = "";
		for(i=0; i < reqitem.length; i++) {
			menu += "\r\n#v"+reqitem[i][0]+"# #b"+reqitem[i][1]+" #t"+reqitem[i][0]+"#s#k";
		}
		menu += "\r\n#i4031138# #b"+cost+" mesos#k";
		cm.sendYesNo("To make #b5 #t"+makeitem+"##k, I'll need the following items. Most of them can be obtained through hunting, so it won't be terriblt difficult for you to get them. What do you think? Do you want some?\r\n"+menu);
	} else if(status == 4) {
		for(i=0; i < reqitem.length; i++) {
			if(!cm.haveItem(reqitem[i][0],reqitem[i][1]))
				access = false;
		}
		if(access == false || !cm.canHold(makeitem) || cm.getMeso() < cost) {
			cm.sendNext("Please check and see if you have all the items needed, or if your etc. inventory is full or not");
		} else {
			cm.sendOk("Here, take the 5 pieces of #b#t"+makeitem+"##k. Even I have to admit, this is a masterpiece. Alright, if you need my help down the road, by all means come back and talk to me!");
			cm.gainItem(reqitem[0][0],-reqitem[0][1]);
			cm.gainItem(reqitem[1][0],-reqitem[1][1]);
			cm.gainItem(reqitem[2][0],-reqitem[2][1]);
			cm.gainMeso(-cost);
			cm.gainItem(makeitem,5);
		}
		cm.dispose();
	}
}
