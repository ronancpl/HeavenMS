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
 * @npc: Hidden Documents
 * @func: Yulete lab 2 quest
*/

function start() {
    if(cm.isQuestStarted(3367)) {
        var c = cm.getQuestProgress(3367, 30);
        if(c == 30) {
            cm.sendNext("(All files have been organized. Report the found files to Yulete.)", 2);
            cm.dispose();
            return;
        }
        
        var book = (cm.getNpcObjectId() % 30);
        var prog = cm.getQuestProgress(3367, book);
        if(prog == 0) {
            c++;
            
            if(book < 20) {
                if(!cm.canHold(4031797, 1)) {
                    cm.sendNext("(You found a report file, but since your ETC is full you choose to put the file in the place you've found.)");
                    cm.dispose();
                    return;
                } else {
                    cm.gainItem(4031797, 1);
                    cm.setQuestProgress(3367, 31, cm.getQuestProgress(3367, 31) + 1);
                }
            }
            
            cm.sendNext("(Organized file. #r" + (30 - c) + "#k left.)", 2);
            
            cm.setQuestProgress(3367, book, 1);
            cm.setQuestProgress(3367, 30, c);
        }
    }
    
    cm.dispose();
}
