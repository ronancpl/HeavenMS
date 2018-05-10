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
	Ardin - Sand Bandits team challenge
 */

var status = -1;

function start(mode, type, selection) {
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
            qm.sendNext("I didn't think you would be this strong. I feel like you have what it takes to become a member of the Sand Bandits. The most important aspect of being a member is power, and I think you have that. I also... want to test you one more time, just to make sure you're the right one. What do you think? Can you handle it?");
        } else if (status == 1) {
            qm.sendAcceptDecline("To truly see your strength, I'll have to face you myself. Don't worry, I'll summon my other self to face off against you. Are you ready?");
        } else if (status == 2) {
            qm.sendNext("Good, I like your confidence.");
        } else {
            if(qm.getWarpMap(926000000).getCharacters().size() > 0) {
                qm.sendOk("There is someone currently in this map, come back later.");
            } else {
                qm.warp(926000000);
                qm.forceStartQuest();
            }
            
            qm.dispose();
        }
    }
}
