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
	Weaver - Ludibrium : Ludibrium Pet Walkway (220000006)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

function start() {
    cm.sendYesNo("This is the road where you can go take a walk with your pet. You can walk around with it, or you can train your pet to go through obstacles here. If you aren't too close with your pet yet, that may present a problem and he will not follow your command as much... So, what do you think? Wanna train your pet?");
}

function action(mode, type, selection) {
    if (mode == -1){
    } else if (mode == 0) {
        cm.sendNext("Hmmm ... too busy to do it right now? If you feel like doing it, though, come back and find me.");
    } else if (mode == 1) {
        if (cm.haveItem(4031128))
            cm.sendNext("Get that letter, jump over obstacles with your pet, and take that letter to my brother Trainer Frod. Give him the letter and something good is going to happen to your pet.");
        else {
            cm.gainItem(4031128, 1);
            cm.sendOk("Ok, here's the letter. He wouldn't know I sent you if you just went there straight, so go through the obstacles with your pet, go to the very top, and then talk to Trainer Frod to give him the letter. It won't be hard if you pay attention to your pet while going through obstacles. Good luck!");
        }
    }
    cm.dispose();
}