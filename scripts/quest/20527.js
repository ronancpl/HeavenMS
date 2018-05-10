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
            var mount = qm.getPlayer().getMount();
            
            if(mount != null && mount.getLevel() >= 3) {
                qm.sendNext("Alright, I'll get you started in how to train Mimio, the next step for Mimianas. When you're ready, talk to me again.");
                qm.forceCompleteQuest();
            } else {
                qm.sendNext("It looks like your Mimiana haven't reached #rlevel 3#k yet. Please train it a bit more before trying to advance it.");
            }
            
            qm.dispose();
        }
    }
}
