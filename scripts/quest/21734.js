/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if(status == 0) {
            qm.sendNext("Hello there, Aran. We received a report that the Puppeteer, one of the members of the Black Wing, is currently based #bsomewhere on the deep forest of Sleepywood#k. Your mission is to enter the place and defeat him there, once for all.");
        } else {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if(status == 0) {
            qm.sendNext("You made it, Aran! The Puppeteer now will not disturb the peace at Victoria Island any longer. Furthermore, now we could clearly investigate the doings of the Black Wing here, at Victoria.");
        } else if(status == 1) {
            qm.sendNext("They were after the #bcrystal seal of Victoria#k. These seals are what repels the Black Mage to further taking the continents into his grasp at once. Each continent has one, Victoria's now is safe and sound.");
        } else if(status == 2) {
            qm.sendNext("For your bravery inputted on these series of missions, I will now reward you properly. Behold, the #rCombo Drain#k Skill: that let's you heal back a portion of damage dealt to the monsters.");
        } else {
            qm.gainExp(12500);
            qm.teachSkill(21100005, 0, 20, -1); // combo drain
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}