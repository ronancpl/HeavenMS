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
            if(qm.haveItem(2430014, 1)) {
                qm.sendNext("It looks like you already have one #b#t2430014##k on your inventory.");
                qm.dispose();
                return;
            }
            
            qm.sendNext("You've used the #b#t2430014##k? Oh well, good thing I have a spare one right here.");
        } else if (status == 1) {
            if(!qm.canHold(2430014, 1)) {
                qm.sendNext("Please make a USE slot available to get it, alright?");
            } else {
                qm.gainItem(2430014, 1);
                qm.forceCompleteQuest();
            }
            
            qm.dispose();
        }
    }
}
