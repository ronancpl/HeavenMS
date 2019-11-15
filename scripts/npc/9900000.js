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
 * @Name         NimaKIN
 * @Author:      Signalize
 * @Author:      MainlandHero - repurposed to be the style setter in-game for mesos
 * @NPC:         9900000
 * @Purpose:     Hair/Face/Eye Changer
 * @Map:         180000000
 */
var status = 0;
var beauty = 0;
var haircolor = Array();
var skin = [0, 1, 2, 3, 4, 5, 9, 10];
var fhair= [31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31400, 31410, 31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31670, 31660, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31780, 31790, 31800, 31810, 31820, 31830, 31840, 31850, 31860, 31870, 31880, 31890, 31910, 31920, 31930, 31940, 31950, 34010, 34020, 34030, 34050, 34110];
var hair = [30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30830, 30840, 30860, 30870, 30880, 30890, 30900, 30910, 30920, 30930, 30940, 30950, 30990, 33000, 33040, 33100];
var hairnew = Array();
var face = [20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20015, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024, 20025, 20026, 20027, 20028, 20029, 20031, 20032];
var fface = [21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21021, 21022, 21023, 21024, 21025, 21026, 21027, 21029, 21030];
var facenew = Array();
var colors = Array();
var price = 100000;

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {  // thanks Conrad for noticing NPC crashing the player when trying to display inexistent cosmetics
        array.push(itemid);
    }
}

function start() {
    if(cm.getPlayer().gmLevel() < 1) {
        cm.sendOk("Hey wassup?");
        cm.dispose();
        return;
    }
    
	if(cm.getPlayer().isMale()){
		cm.sendSimple("Hey there, you can change your look for " + price + " mesos. What would you like to change?\r\n#L0#Skin#l\r\n#L1#Male Hair#l\r\n#L2#Hair Color#l\r\n#L3#Male Regular Eyes#l\r\n#L4#Eye Color#l");
	}else{
		cm.sendSimple("Hey there, you can change your look for " + price + " mesos. What would you like to change?\r\n#L0#Skin#l\r\n#L5#Female Hair#l\r\n#L2#Hair Color#l\r\n#L6#Female Eyes#l\r\n#L4#Eye Color#l");
	}
}

function action(mode, type, selection) {
    status++;
    if (mode != 1 || cm.getPlayer().gmLevel() < 1){
        cm.dispose();
        return;
    }
    if (status == 1) {
        beauty = selection + 1;
		if(cm.getMeso() > price){
			if (selection == 0)
				cm.sendStyle("Pick one?", skin);
			else if (selection == 1 || selection == 5) {
				for each(var i in selection == 1 ? hair : fhair)
					pushIfItemExists(hairnew, i);
				cm.sendStyle("Pick one?", hairnew);
			} else if (selection == 2) {
				var baseHair = parseInt(cm.getPlayer().getHair() / 10) * 10;
				for(var k = 0; k < 8; k++)
					pushIfItemExists(haircolor, baseHair + k);
				cm.sendStyle("Pick one?", haircolor);
			} else if (selection == 3 || selection == 6) {
				for each(var j in selection == 3 ? face : fface)
					pushIfItemExists(facenew, j);
				cm.sendStyle("Pick one?", facenew);
			} else if (selection == 4) {
				var baseFace = parseInt(cm.getPlayer().getFace() / 1000) * 1000 + parseInt(cm.getPlayer().getFace() % 100);
				for(var i = 0; i < 9; i++)
					pushIfItemExists(colors, baseFace + (i*100));
				cm.sendStyle("Pick one?", colors);
			}
		} else {
			cm.sendNext("You don't have enough mesos. Sorry to say this, but without " + price + " mesos, you won't be able to change your look!");
            cm.dispose();
		}
        
    } else if (status == 2){
        if (beauty == 1){
            cm.setSkin(skin[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 2 || beauty == 6){
            cm.setHair(hairnew[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 3){
            cm.setHair(haircolor[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 4 || beauty == 7){
            cm.setFace(facenew[selection]);
			cm.gainMeso(-price);
		}
        if (beauty == 5){
            cm.setFace(colors[selection]);
			cm.gainMeso(-price);
		}
        cm.dispose();
    }
}