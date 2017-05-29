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
status = -1;

function start() {
    action(1,0,0);
}

function action(mode, type, selection) {
    status++;
    if(mode != 1){
        if(mode == 0 && status == 4)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (cm.getPlayer().getMapId() == 800000000) {
        if (status == 0) 
            cm.sendSimple("How's the traveling? Are you enjoying it?#b\r\n#L0#Yes, I'm done with traveling. Can I go back to #m" + cm.getPlayer().getSavedLocation("WORLDTOUR") + "#?\r\n#L1#No, I'd like to continue exploring this place.");
        else if (status == 1) {
            if (selection == 0) {
                cm.sendNext("Alright. I'll take you back to where you were before the visit to Japan. If you ever feel like traveling again down the road, please let me know!");
            } else if (selection == 1) {
                cm.sendOk("OK. If you ever change your mind, please let me know.");
                cm.dispose();
            }
        } else if (status == 2) {
            var map = cm.getPlayer().getSavedLocation("WORLDTOUR");
            if (map == undefined)
                map = 104000000;
            cm.warp(map, parseInt(Math.random() * 5));
            cm.dispose();
        }
    } else {
        if (status == 0) 
            cm.sendNext("If you're tired of the monotonous daily life, how about getting out for a change? there's nothing quite like soaking up a new culture, learning something new by the minute! It's time for you to get out and travel. We, at the Maple Travel Agency recommend you going on a #bWorld Tour#k! Are you worried about the travel expense? You shouldn't be! We, the #bMaple Travel Agency#k, havecarefully come up with a plan to let you travel for ONLY #b3,000 mesos#k!");
        else if (status == 1) 
            cm.sendSimple("We currently offer this place for you traveling pleasure: #bMushroom Shrine of Japan#k. I'll be there serving you as the travel guide. Rest assured, the number of destinations will be increase over time. Now, would you like to head over to the Mushroom Shrine?#b\r\n#L0#Yes, take me to Mushroom Shrine (Japan)");
        else if (status == 2)
            cm.sendNext("Would you like to travel to #bMushroom Shrine of Japan#k? If you desire to feel the essence of Japan, there's nothing like visiting the Shrine, a Japanese cultural melting pot. Mushroom Shrine is a mythical place that serves the incomparable Mushroom God from ancient times.");
        else if (status == 3) {
            if(cm.getMeso() < 3000){
                cm.sendNext("You don't have enough mesos to take the travel.");
                cm.dispose();
                return;
            }
            cm.sendNextPrev("Check out the female shaman serving the Mushroom God, and I strongly recommend trying Takoyaki, Yakisoba, and other delocious food sold in the streets of Japan. Now, let's head over to #bMushroom Shrine#k, a mythical place if there ever was one.");
        } else if (status == 4) {
            cm.gainMeso(-3000);
            cm.getPlayer().saveLocation("WORLDTOUR");
            cm.warp(800000000);
            cm.dispose();
        }
    }
}