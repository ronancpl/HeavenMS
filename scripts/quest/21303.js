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
            qm.sendNext("Aaaargh... Yeti's #b#t4032339##k has just been stolen! How frustrating, Yeti worked hard to get it, just to have it stolen by that #rThief Crow#k...", 9);
        } else if (status == 1) {
            qm.sendNextPrev("Hey, I was just passing by and could not refrain from hearing you just now. I can lend you my strength, where did the thief go?", 3);
        } else if (status == 2) {
            qm.sendNextPrev("Oh, how nice of you... Thief has passed #rthrough the gate at west#k. Bring back the #b#t4032339##k, Yeti needs it to give to beloved one.", 9);
        } else if (status == 3) {
            qm.sendNextPrev("Ok, wait there. I will return it back to you in no time!", 3);
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
