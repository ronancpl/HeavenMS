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

importPackage(Packages.constants);

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
            qm.sendNext("Once you've got #b2nd job advancement#k, I'll tell you about the #bMushroom Castle#k.");
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
            if(GameConstants.getJobBranch(qm.getPlayer().getJob()) == 1) {
                qm.sendNext("Eh, didn't you get the #r2nd job advancement#k yet?");
                qm.dispose();
                return;
            }
            
            qm.sendNext("Okay you seem ready to go to the #bMushroom Castle#k. In #rHenesys#k, climb at the tree fort at #bwest#k then enter a portal over there. On the other area, #rgo west#k. From there, a portal will be readily available to access the #bMushroom Castle#k area.");
        } else {
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}