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
/* 	Xan
	Lian Hua Hua Skin Care
    by Moogra
*/
var skin = Array(0, 1, 2, 3, 4);
var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1)
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
            cm.sendSimple("Well, hello! Welcome to the Lian Hua Hua Skin-Care! Would you like to have a firm, tight, healthy looking skin like mine?  With #b#tCBD Skin Coupon##k, you can let us take care of the rest and have the kind of skin you've always wanted!\r\n\#L1#Sounds Good! (uses #i5153010# #t5153010#)#l");
        }
        else if (status == 1) {
            if (!cm.haveItem(5153010)) {
                cm.sendOk("It looks like you don't have the coupon you need to receive the treatment. I'm sorry but it looks like we cannot do it for you.");
                cm.dispose();
                return;
            }
            cm.sendStyle("With our specialized service, you can see the way you'll look after the treatment in advance. What kind of a skin-treatment would you like to do? Go ahead and choose the style of your liking...", skin);
        }
        else {
            cm.gainItem(5153010, -1);
            cm.setSkin(selection);
            cm.sendOk("Enjoy your new and improved skin!");
            
            cm.dispose();
        }
    }
}
