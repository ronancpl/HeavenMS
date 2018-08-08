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
/* Sejan's Test
	Food delivery on Ariant
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
            var c = 0;
            
            for(var i = 0; i < 4; i++) {
                if(qm.getQuestProgress(3929, i) == 1) {
                    c++;
                }
            }
            
            if(c == 4) {
                qm.sendNext("You delivered all the food, good.");
                qm.gainExp(2000);
                qm.forceCompleteQuest();
            } else {
                var missed = (4 - qm.getItemQuantity(4031580)) - c;
                if(missed > 0) {
                    if(qm.canHold(4031580, missed)) {
                        qm.gainItem(4031580, missed);
                        qm.sendNext("Hey, what are you trying to pull on? To pass my test you must deliver all the foods to the Residential areas.");
                    } else {
                        qm.sendNext("You don't completed the task, neither has slots available on the inventory to get the food. Free a slot on your ETC please.");
                    }
                } else {
                    qm.sendNext("Hey, what are you trying to pull on? To pass my test you must to deliver all the foods to the Residential areas.");
                }
            }
            
            qm.dispose();
        }
    }
}
