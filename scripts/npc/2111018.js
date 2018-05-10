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
                        if(cm.isQuestStarted(3339)) {
                                var progress = cm.getQuestProgress(3339, 0);
                            
                                if(progress == 3) {
                                        cm.sendGetText("The pipe reacts as the water starts flowing. A secret compartment with a keypad shows up. #bPassword#k!");
                                } else if(progress == 2) {
                                        cm.setQuestProgress(3339, 0, 3);
                                        cm.sendGetText("The pipe reacts as the water starts flowing. A secret compartment with a keypad shows up. #bPassword#k!");
                                } else if(progress < 3) {
                                        cm.setQuestProgress(3339, 0, 0);
                                        cm.dispose();
                                } else {
                                        cm.warp(261000001, 1);
                                        cm.dispose();
                                }
                        } else {
                                if(cm.isQuestCompleted(3339)) {
                                        cm.warp(261000001, 1);
                                }
                                
                                cm.dispose();
                        }
                } else if(status == 1) {
                        if(cm.getText() == "my love Phyllia") {
                                cm.setQuestProgress(3339, 0, 4);
                                cm.warp(261000001, 1);
                                cm.dispose();
                        }
                        else{
                                cm.sendOk("#rWrong!");
                                cm.dispose();
                        }
                }
        }
}
