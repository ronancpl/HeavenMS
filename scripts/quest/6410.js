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
/* 
	Hypnotize skill quest
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
            if (qm.getQuestProgress(6410, 0) == 0) {
                qm.sendOk("You must save #r#p2095000##k first!");
                qm.dispose();
            } else {
                qm.sendNext("Again, thank you so much for rescuing me. I don't know how to repay you for all this... both Shulynch and you are the nicest people I have encountered. If you approach the mobs the same way you approached me, they may all end up becoming friends with you, as well. Please never lose the kindness you have in you.");
            }
        } else if (status == 1) {
            qm.sendNext("(Friends with the mobs... never lose the kindness.)\r\n\r\n  #s5221009#    #b#q5221009##k");
        } else if (status == 2) {
            qm.gainExp(1200000);
            qm.teachSkill(5221009, 0, 10, -1);
            
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}