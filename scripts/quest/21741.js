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
            qm.sendNext("Have you been advancing your levels? I found an interesting piece of information about the Black Wings. This time, you'll have to travel quite a bit. Do you know a town called #bMu Lung#k? You'll have to head there.");
        } else if (status == 1) {
            qm.sendAcceptDecline("Apparently, #bMr. Do#k in Mu Lung somehow met with the Black Wings. I don't know the details. Please go and find out why the Black Wings contacted Mr. Do and what exactly happened between them.");
        } else {
            qm.sendNext("Mr. Do is known to be curt, so you are going to have to remain patient while talking to him. Talk to him with the #bI heard you met the Shadow Knight of the Black Wings#k keyword.");
            
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
