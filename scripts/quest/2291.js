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
            if(!qm.haveItem(4032521, 10)) {
                qm.sendNext("Hey, you didn't get #b10 #t4032521##k yet, did you?");
                qm.dispose();
                return;
            }
            
            qm.sendNext("You got the #b#i4032521##k with you, great. Let me show you the way.");
        } else if (status == 1) {
            qm.gainItem(4032521, -10);
            
            var rock = qm.getEventManager("RockSpiritVIP");
            rock.newInstance("RockSpiritVIP");
            rock.setProperty("player", qm.getPlayer().getName());
            rock.startInstance(qm.getPlayer());
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}