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
/*     Eric
    Singapore VIP Hair/Color Changer
    MADE BY AAron and Cody from the FlowsionMS Forums
*/
var status = 0;
var beauty = 0;
var mhair = Array(30110, 30290, 30230, 30260, 30320, 30190, 30240, 30350, 30270, 30180);
var fhair = Array(31260, 31090, 31220, 31250, 31140, 31160, 31100, 31120, 31030, 31270, 31810);
var hairnew = Array();

function start() {
    cm.sendSimple("Welcome to the Quick-Hand Hair-Salon!. Do you, by any chance, have #b#t5150033##k or #b#t5151028 ##k? If so, how about letting me take care of your hair? Please what you want to do with it.\r\n#L1#Haircut: #i5150033##t5150033##l\r\n#L2#Dye your hair: #i5151028##t5151028##l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (selection == 1) {
            beauty = 1;
            hairnew = Array();
            if (cm.getPlayer().getGender() == 0)
                for(var i = 0; i < mhair.length; i++)
                    hairnew.push(mhair[i] + parseInt(cm.getPlayer().getHair()% 10));
            else
                for(var i = 0; i < fhair.length; i++)
                    hairnew.push(fhair[i] + parseInt(cm.getPlayer().getHair()% 10));
            cm.sendStyle("I can completely change the look of your hair. Aren't you ready for a change? With #b#t5150033##k, I'll take care of the rest for you. Choose the style of your liking!", hairnew);
        } else if (selection == 2) {
            beauty = 2;
            haircolor = Array();
            var current = parseInt(cm.getPlayer().getHair()/10)*10;
            for(var i = 0; i < 8; i++)
                haircolor.push(current + i);
            cm.sendStyle("I can completely change the look of your hair. Aren't you ready for a change? With #b#t5151028##k, I'll take care of the rest. Choose the color of your liking!", haircolor);
        } else if (status == 2) {
            if (beauty == 1){
                if (cm.haveItem(5150033)){
                    cm.gainItem(5150033, -1);
                    cm.setHair(hairnew[selection]);
                    cm.sendOk("Enjoy your new and improved hairstyle!");
                } else
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry.");
            }
            if (beauty == 2){
                if (cm.haveItem(5151028)){
                    cm.gainItem(5151028, -1);
                    cm.setHair(haircolor[selection]);
                    cm.sendOk("Enjoy your new and improved haircolor!");
                } else
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't dye your hair without it. I'm sorry.");
            }
            cm.dispose();
        }
    }
}
