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
        
        if (status == 0) {
            if(qm.getPlayer().getLevel() > 50) {
                qm.forceCompleteQuest();
                qm.dispose();
                return;
            }
            
            qm.sendNext("Okay... so here's our plan to defeat Scrooge and his dastardly plans. The Force of the Spirit I gave you is an item packed with mana. It's an item you'll definitely use at the map I am about to send you. In order to do that, you'll have to bring your party members with you as well. You should bring your party members here or form one right now!");
        } else if (status == 1) {
            qm.sendAcceptDecline("Would you like to move forward?");
        } else {
            var level = qm.getPlayer().getLevel();
            
            qm.warp(level <= 30 ? 889100000 : (level <= 40 ? 889100010 : 889100020));
            qm.dispose();
        }
    }
}
