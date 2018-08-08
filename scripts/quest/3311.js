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
            if(qm.getQuestProgress(3311, 0) == 1 && qm.getQuestProgress(3311, 1) == 1) {
                qm.sendNext("Hmm, so the Alcadno doctor wrote something about researching some vanguardist Neo Huroid machine, that could beat by far the existing one, and was about to prepare the last steps of his rehearsal? We don't have a word about him for about three weeks now, something must have gone wrong...");
                qm.gainExp(60000);
                qm.forceCompleteQuest();
            } else {
                qm.sendNext("Found nothing yet? Please check out Dr. De Lang's house properly, something there may give out a clue about what is going on.");
            }
            
            qm.dispose();
        }
    }
}