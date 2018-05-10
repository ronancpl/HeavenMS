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
            qm.sendNext("Hey, Aran. You seem pretty strong, since that time from when you got freed from the glacier. Suitable enough to #bride a wolf#k, if you ask me.");
        } else if (status == 1) {
            qm.sendAcceptDecline("Picked your interest, huh? Very well, first you must make your way to #bAqua#k, there is a person there who makes #rfood for wolf cubs#k. Bring one portion to me, and I shall deem you able to tame and take care of one. What do you say, will you try for it?");
        } else if (status == 2) {
            qm.sendNext("Alright. The one you must meet is #bNanuke#k, she is on top of a #rsnowy whale#k, somewhere in the ocean. Good luck!");
            qm.forceStartQuest();
            
            qm.dispose();
        }
    }
}