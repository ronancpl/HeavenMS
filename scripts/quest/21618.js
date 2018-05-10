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
/* Aran lv 200 mount quest
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
        
        if (status == 0) {
            qm.sendNext("Oh, this befriended wolf of yours... I sense some hidden powers hidden behind his furs, you see. Wat'cha say, master, if I awaken it's hidden power?", 9);
        } else if (status == 1) {
            qm.sendNextPrev("Wait, can you do that?", 3);
        } else if (status == 2) {
            qm.sendAcceptDecline("Astonished, huh? Does all that time frozen in the glacier hindered your senses as well? Why, of course! Tell me when you're ready!", 9);
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
        
        if (status == 0) {
            if(!qm.haveItemWithId(1902017, false)) {
                qm.sendNext("You will have to unequip the wolf first before going for the evolution.");
                qm.dispose();
                return;
            }
            
            qm.sendNext("Step aside, behold the mighty prowess of Maha!!");
        } else {
            qm.gainItem(1902017, -1);
            qm.gainItem(1902018, 1);
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}