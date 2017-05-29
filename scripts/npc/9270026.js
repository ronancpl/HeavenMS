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
/* 	Sixx
	Singa REG/VIP Eye Color Changer
*/

var status = 0;
var beauty = 0;
var colors = Array();

function start() {
    cm.sendSimple("Hi, there! I'm Sixx, in charge of Da Yan Jing Lens Shop here at CBD! With #b#t5152039##k or #b#t5152040##k, you can let us take care of the rest and have the kind of beautiful look you've always craved! Remember, the first thing everyone notices about you are the eyes, and we can help you find the cosmetic lens that most fits you! Now, what would you like to use?\r\n#L1#Cosmetic Lenses: #i5152039##t5152039##l\r\n#L2#Cosmetic Lenses: #i5152040##t5152040##l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (status == 1) {
            if (selection == 1) {
                beauty = 1;
                var current = cm.getPlayer().getFace()% 100 + 20000 + cm.getPlayer().getGender() * 1000;
                cm.sendYesNo("If you use the regular coupon, you'll be awarded a random pair of cosmetic lenses. Are you going to use a #b#t5152039##k and really make the change to your eyes?");
            } else {
                beauty = 2;
                var current = cm.getPlayer().getFace()% 100 + 20000 + cm.getPlayer().getGender() * 1000;
                colors = [current , current + 100, current + 200, current + 300, current +400, current + 500, current + 600, current + 700];
                cm.sendStyle("With our specialized machine, you can see yourself after the treatment in advance. What kind of lens would you like to wear? Choose the style of your liking.", colors);
            }
        }
        else if (status == 2) {
            if (beauty == 1){
                if (cm.haveItem(5152039)){
                    cm.gainItem(5152039, -1);
                    cm.setFace(Math.floor(Math.random() * 8) * 100 + current);
                    cm.sendOk("Enjoy your new and improved cosmetic lenses!");
                } else
                    cm.sendOk("I'm sorry, but I don't think you have our cosmetic lens coupon with you right now. Without the coupon, I'm afraid I can't do it for you..");
            } else if (beauty == 2){
                if (cm.haveItem(5152040)){
                    cm.gainItem(5152040, -1);
                    cm.setFace(colors[selection]);
                    cm.sendOk("Enjoy your new and improved cosmetic lenses!");
                } else 
                    cm.sendOk("I'm sorry, but I don't think you have our cosmetic lens coupon with you right now. Without the coupon, I'm afraid I can't do it for you..");
            }
            cm.dispose();
        }
    }
}
