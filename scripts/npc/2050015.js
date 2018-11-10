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
/* Meteorite
	@Author RonanLana (Ronan)
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
                        if(cm.isQuestStarted(3421)) {
                                var meteoriteId = cm.getNpc() - 2050014;
                                
                                var progress = cm.getQuestProgress(3421, 0);
                                if((progress >> meteoriteId) % 2 == 0 || (progress == 63 && !cm.haveItem(4031117, 6))) {
                                        if (cm.canHold(4031117, 1)) {
                                                progress |= (1 << meteoriteId);
                                                
                                                cm.gainItem(4031117, 1);
                                                cm.setQuestProgress(3421, 0, progress);
                                        } else {
                                                cm.getPlayer().dropMessage(1, "Have a ETC slot available for this item.");
                                        }
                                }
                        }
                        
                        cm.dispose();
                }
        }
}
