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
/* Steal queen's silk
 */

importPackage(Packages.client);

function isTigunMorphed(ch) {
        return ch.getBuffSource(MapleBuffStat.MORPH) == 2210005;
}

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
        
        if(!isTigunMorphed(qm.getPlayer())) {
            qm.sendNext("What's this? I can't simply give the Queen's silk to anyone, claiming they will hand it at once to the queen. Get out of my sights.");
            qm.dispose();
            return;
        }
        
        if (status == 0) {
            qm.sendNext("Tigun, what are you doing here?");
        } else if (status == 1) {
            qm.sendNext("The Queen wants her silk right now? Alright, I have them here. Hold on a moment.");
            
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
        
        if(!isTigunMorphed(qm.getPlayer())) {
            qm.sendNext("What's this? I can't simply give the Queen's silk to anyone, claiming they will hand it at once to the queen. Get out of my sights.");
            qm.dispose();
            return;
        }
        
        if (status == 0) {
            if(qm.canHold(4031571, 1)) {
                qm.gainItem(4031571);
                
                qm.sendNext("There you go. Please deliver to the queen as soon as possible, Tigun, she gets really mad if things get delayed.");
                qm.forceCompleteQuest();
            } else {
                qm.sendNext("Hey, you're lacking space to hold this, man. I will stay with it while you arrange your backpack...");
            }
            
            qm.dispose();
        }
    }
}