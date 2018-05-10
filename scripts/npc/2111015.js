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

importPackage(Packages.client);

var status;

function isPillUsed(ch) {
        return ch.getBuffSource(MapleBuffStat.HPREC) == 2022198;
}

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
                        if(cm.isQuestStarted(3314) && !cm.haveItem(2022198, 1) && !isPillUsed(cm.getPlayer())) {
                                if(cm.canHold(2022198, 1)) {
                                        cm.gainItem(2022198, 1);
                                        cm.sendOk("You took the pills that were laying on the desk.", 2);
                                } else {
                                        cm.sendOk("You don't have a USE slot available to get Russellon's pills.", 2);
                                }
                        }
                    
                        cm.dispose();
                }
        }
}
