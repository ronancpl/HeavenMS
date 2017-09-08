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
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
	NPC NAME: Cesar (1)
	NPC ID: 2101018
	Author: Vcoc
	Function: AriantPQ
*/

status = -1;
function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("You're not between level 20 and 30. Sorry, you may not participate.");
        cm.dispose();
        return;
    }
    action(1,0,0);
}

function action(mode, type, selection){
    status++;
    if (status == 4){
        cm.getPlayer().saveLocation("MIRROR");
        cm.warp(980010000, 3);
        cm.dispose();
    }
    if(mode != 1){
        if(mode == 0 && type == 0)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (status == 0)
        cm.sendNext("I have prepared a huge festival here at Ariant for the great fighters of MapleStory. It's called #bThe Ariant Coliseum Challenge#k.");
    else if (status == 1)
        cm.sendNextPrev("The Ariant Coliseum Challenge is a competition that matches the skills of monster combat against others. In this competition, your object isn't to hunt the monster;  rather, you need to #beliminate a set amount of HP from the monster, followed by absorbing it with a jewel#k. #bThe fighter that ends up with the most jewels will win the competition.#k");
    else if (status == 2)
        cm.sendSimple("If you are a strong and brave warrior from #bPerion#k, training under Dances With Balrogs, then are you interested in participating in The Ariant Coliseum Challenge?!\r\n#b#L0# I'd love to participate in this great competition.#l");
    else if (status == 3)
        cm.sendNext("Okay, now I'll send you to the battle arena. I'd like to see you emerge victorious!");
}