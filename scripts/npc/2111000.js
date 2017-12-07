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
/*
-- Odin JavaScript --------------------------------------------------------------------------------
	Carson - Magatia (GMS Like)
-- Version Info -----------------------------------------------------------------------------------
    1.2 - Improved by Ronan
    1.1 - Shortened by Moogra
	1.0 - First Version by Maple4U
---------------------------------------------------------------------------------------------------
*/
function start() {
    if(cm.isQuestStarted(3310) && !cm.haveItem(4031709, 1)) {
        cm.warp(926120100);
    } else {
        cm.sendNext("Alchemy....and Alchemist.....both of them are important. But more importantly, it is the Magatia that tolerate everything. The honor of Magatia should be protected by me.");
    }
    
    cm.dispose();
}
