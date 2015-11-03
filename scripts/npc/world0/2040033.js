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
        Neru - Ludibrium : Ludibrium Pet Walkway (220000006)
-- By ---------------------------------------------------------------------------------------------
        Xterminator
-- Version Info -----------------------------------------------------------------------------------
        1.1 - Second Version by Moogra
        1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

function start() {
    if (cm.haveItem(4031128)) {
        cm.sendNext("Eh, that's my brother's letter! Probably scolding me for thinking I'm not working and stuff...Eh? Ahhh...you followed my brother's advice and trained your pet and got up here, huh? Nice!! Since you worked hard to get here, I'll boost your intimacy level with your pet.");
    } else {
        cm.sendOk("My brother told me to take care of the pet obstacle course, but ... since I'm so far away from him, I can't help but wanting to goof around ...hehe, since I don't see him in sight, might as well just chill for a few minutes.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
    } else if (cm.getPlayer().getNoPets() == 0)
        cm.sendNextPrev("Hmmm ... did you really get here with your pet? These obstacles are for pets. What are you here for without it?? Get outta here!");
    else {
        cm.gainItem(4031128, -1);
        cm.gainCloseness(4);
        cm.sendNextPrev("What do you think? Don't you think you have gotten much closer with your pet? If you have time, train your pet again on this obstacle course...of course, with my brother's permission.");
    }
    cm.dispose();
}