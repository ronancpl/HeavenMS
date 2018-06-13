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
/**
 * @author: Ronan
 * @npc: Abdula
 * @map: Multiple towns on Maplestory
 * @func: Job Skill / Mastery Book Drop Announcer
*/

var status;
var selected = 0;
var skillbook = [], masterybook = [], table = [];

function start() {
    status = -1;
    selected = 0;
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

        if (status == 0) {
            var greeting = "Hello, I'm #p9209000#, the Skill & Mastery Book announcer! ";
            
            if(cm.getPlayer().isCygnus()) {
                cm.sendOk(greeting + "There are no skill or mastery books available for Cygnus Knights.");
                cm.dispose();
                return;
            }
            
            var jobrank = cm.getJob().getId() % 10;
            if(jobrank < 2) {
                cm.sendOk(greeting + "Keep training yourself until you reach the #r4th job#k of your class. New opportunities for improvement will arrive when you reach that feat!");
                cm.dispose();
                return;
            }
            
            skillbook = cm.getAvailableSkillBooks();
            masterybook = cm.getAvailableMasteryBooks();

            if(skillbook.length == 0 && masterybook.length == 0) {
                cm.sendOk(greeting + "There are no books available to further improve your job skills for now. Either you #bmaxed out everything#k or #byou didn't reach the minimum requisites to use some skill books#k yet.");
                cm.dispose();
            } else if(skillbook.length > 0 && masterybook.length > 0) {
                var sendStr = greeting + "New opportunities for skill improvement have been located for you to improve your skills! Pick a type to take a look onto.\r\n\r\n#b";

                sendStr += "#L1# Skill Book#l\r\n";
                sendStr += "#L2# Mastery Book#l\r\n";

                cm.sendSimple(sendStr);
            } else if(skillbook.length > 0) {
                selected = 1;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill learns available for now.");
            } else {
                selected = 2;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill upgrades available.");
            }
            
        } else if(status == 1) {
            var sendStr = "The following books are currently available:\r\n\r\n";
            if(selected == 0) selected = selection;

            table = (selected == 1) ? skillbook : masterybook;
            for(var i = 0; i < table.length; i++) {
                sendStr += "  #L" + i + "# #i" + table[i] + "#  #t" + table[i] + "##l\r\n";
            }

            cm.sendSimple(sendStr);

        } else if(status == 2) {
            selected = selection;
            var mobList = cm.getNamesWhoDropsItem(table[selected]);

            var sendStr;
            if(mobList.length == 0) {
                sendStr = "No mobs drop '#b#t" + table[selected] + "##k'.";

            } else {
                sendStr = "The following mobs drop '#b#t" + table[selected] + "##k':\r\n\r\n";

                for(var i = 0; i < mobList.length; i++) {
                    sendStr += "  #L" + i + "# " + mobList[i] + "#l\r\n";
                }
            }

            cm.sendSimple(sendStr);
            cm.dispose();
        }
    }
}
