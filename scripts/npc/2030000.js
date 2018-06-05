/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Jeff - El Nath : El Nath : Ice Valley II (211040200)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;

function start() {
    if(cm.haveItem(4031450, 1)) {
        cm.warp(921100100);
        cm.dispose();
        return;
    }
    
    cm.sendNext("Hey, you look like you want to go farther and deeper past this place. Over there, though, you'll find yourself surrounded by aggressive, dangerous monsters, so even if you feel that you're ready to go, please be careful. Long ago, a few brave men from our town went in wanting to eliminate anyone threatening the town, but never came back out...");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status == 1 && mode == 0 && cm.getLevel() > 49) {
            cm.sendNext("Even if your level's high it's hard to actually go in there, but if you ever change your mind, please find me. After all, my job is to protect this place.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.getLevel() > 49)
                cm.sendYesNo("If you are thinking of going in, I suggest you change your mind. But if you really want to go in... I'm only letting in the ones that are strong enough to stay alive in there. I do not wish to see anyone else die. Let's see... Hmmm...! You look pretty strong. All right, do you want to go in?");
             else 
                cm.sendPrev("If you are thinking of going in, I suggest you change your mind. But if you really want to go in... I'm only letting in the ones that are strong enough to stay alive in there. I do not wish to see anyone else die. Let's see... Hmmm... You haven't reached Level 50 yet. I can't let you in, then, so forget it.");
        } else if (status == 2) {
            if (cm.getLevel() >= 50) 
                cm.warp(211040300, 5);
            cm.dispose();
        }
    }
}