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
            qm.sendNext("Ok, meet me at #b#m260020700##k for your information. To reach there, follow #reast#k from here until you reach #rMagatia#k, I will be there. Now go.");
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
            if(qm.getMapId() == 260020000) {
                qm.sendNext("Eh you're still here? To reach #b#m260020700##k, follow #reast#k from here until you reach #rMagatia#k, I will be there. Now go.");
                qm.dispose();
                return;
            }
            
            qm.sendNext("Oh there you are. There're no Meerkat's nearby, so there probably is no eavesdropping around here. Very well, you must be fit to go to the #rMushroom Castle#k. Talk to me once you've got #blevel 30#k.");
        } else {
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}