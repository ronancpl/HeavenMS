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
            qm.sendAcceptDecline("Meerkats spreads rumors like wildfire... By blackmailing me and my cab service, they are taking costumers away from me day after day... Hey, tell no one about this, if you clean some #rMeerkats#k from my way, I'll tell you an info about the #rMushroom Castle#k. What do you say?");
        } else if (status == 1) {
            qm.sendNext("Great, they you have #r5 minutes#k to kill #b40 Meerkats#k within this time. Good luck!");
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
            qm.sendNext("You did it! ... Hey, #rMeerkats#k around here may listen to our conversation. I'm not going to talk about THAT right now.");
        } else {
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}