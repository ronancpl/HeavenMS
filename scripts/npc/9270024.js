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
/* 	Kelvin
	SingaPore VIP Face changer
	Made by aaron and cody 
*/
var status = 0;
var beauty = 0;
var mface = Array(20109, 20110, 20106, 20108, 20112, 20013);
var fface = Array(21021, 21009, 21010, 21006, 21008, 21012);
var facenew = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) 
        cm.dispose();
    else {
        if (mode == 0 && status >= 0) {
            cm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("Let's see...I can totally transform your face into something new. Don't you want to try it? For #b#t5152038##k, you can get the face of your liking. Take your time in choosing the face of your preference...\r\n\#L2#Let me get my dream face! (Uses #i5152038# #t5152038#)#l");
        } else if (status == 1) {
            if (!cm.haveItem(5152038)) {
                cm.sendOk("Hmm ... it looks like you don't have the coupon specifically for this place. Sorry to say this, but without the coupon, there's no plastic surgery for you...");
                cm.dispose();
                return;
            }
            
            facenew = Array();
            if (cm.getPlayer().getGender() == 0) {
                for(var i = 0; i < mface.length; i++)
                    facenew.push(mface[i] + cm.getPlayer().getFace()% 1000 - (cm.getPlayer().getFace()% 100));
            }
            if (cm.getPlayer().getGender() == 1) {
                for(var i = 0; i < fface.length; i++) {
                    facenew.push(fface[i] + cm.getPlayer().getFace()% 1000 - (cm.getPlayer().getFace()% 100));
                }
            }
            cm.sendStyle("Let's see... I can totally transform your face into something new. Don't you want to try it? For #b#t5152038##k, you can get the face of your liking. Take your time in choosing the face of your preference...", facenew);
        }
        else if (status == 2){
            cm.gainItem(5152038, -1);
            cm.setFace(facenew[selection]);
            cm.sendOk("Enjoy your new and improved face!");
            
            cm.dispose();
        }
    }
}
