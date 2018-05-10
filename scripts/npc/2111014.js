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

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        if(cm.isQuestStarted(3311)) {
                                cm.setQuestProgress(3311, 1, 1);
                                cm.sendOk("The diary of Dr. De Lang. A lot of formulas and pompous scientific texts can be found all way through the pages, but it is worth noting that in the last entry (3 weeks ago), it is written that he concluded the researches on an improvement on the blueprints for the Neo Huroids, thus making the last preparations to show it to the 'society'... No words after this...", 2);
                        } else if(cm.isQuestStarted(3322) && !cm.haveItem(4031697, 1)) {
                                if(cm.canHold(4031697, 1))
                                        cm.gainItem(4031697, 1);
                                else
                                        cm.sendNext("Your inventory is full, make sure a ETC slot is available for the item.");
                        }
                    
                        cm.dispose();
                }
        }
}
