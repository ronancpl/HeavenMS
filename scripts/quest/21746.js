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
            qm.sendNext("If you want to know more about the Seal Rock of Mu Lung, you will need to pass my test. Prove your valor overpowering me in melee combat, only then I shall recognize you as a worthy knight.");
        } else {
            var mapobj = qm.getWarpMap(925040001);
            if(mapobj.countPlayers() == 0) {
                mapobj.resetPQ(1);
                
                qm.warp(925040001, 0);
                qm.forceStartQuest();
            }
            else {
                qm.sendOk("Someone is already attempting a challenge. Wait for them to finish before you enter.");
            }

            
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
            qm.sendNext("Oh, you brought the ink. Now let me pour it, cautiously.... Almost there, almost. ... ..... Kyaaa! Th-the letter. It says: 'I'll be there to take your Seal Rock of Mu Lung.'");
        } else if (status == 1) {
            qm.gainItem(4032342, -8);
            qm.gainItem(4220151, -1);
            qm.gainExp(10000);
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}