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
            qm.sendNext("So we have lost #btwo seal stones#k so far, from the neighboring areas of #rOrbis#k and #rMu Lung#k... Things are starting to get out of control, it seems.");
        } else if (status == 1) {
            qm.sendNext("Aran, your next objective will be to use the #btime gate to Ellin#k again. This time you will be retrieving the long lost #rSeal Stone of Ellin Forest#k. According to informations our network have gathered, #b#p2131002##k of that time have a clue about that gem, #rfind her#k. Please be successful on this task, our world is relying on you more than ever!");
        } else {
            qm.gainExp(500);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
