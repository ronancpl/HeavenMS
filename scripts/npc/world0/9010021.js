/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
/* 9010021 - Wolf Spirit Ryko 
    BossRushPQ recruiter
    @author Ronan
 */

var status;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            cm.sendNext("Heed me, human. I hail from a distant plane and came to this world to take a glimpse on the power level of the best of the dwellers of this world.");
	} else if(status == 1) {
            cm.sendNext("Oh, don't get me wrong, our civilization are of the type that makes researches for the sake of science, we have no regards for domination or disturbances whatsoever.");
        } else if(status == 2) {
            cm.sendNext("These contests involve #bsequential boss fights#k, with some resting spots between some sections. This will require some strategy time and enough supplies at hand, as these are not common fights.");
        } else if(status == 3) {
            cm.sendAcceptDecline("If you feel you are powerful enough, you can join others like you at where we are hosting the contests of power. ... So, what is your decision? Will you come to where the contests are being held right now?");
        } else if(status == 4) {
            cm.sendOk("Very well. Remember, there you can assemble a team or take on the fightings on your own, it's up to you. Good luck!");
        } else if(status == 5) {
            cm.warp(970030000);
            cm.dispose();
        }
    }
}