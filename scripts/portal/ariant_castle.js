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
	Portal - To get into Ariant Castle
-- By ---------------------------------------------------------------------------------------------
	Jayd
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Jayd
---------------------------------------------------------------------------------------------------
**/

function enter(pi) {
	if (pi.getPlayer().haveItem(4031582) == true) {
		pi.playPortalSound();
		pi.warp(260000301, 5);
		return true;
	} else {
		pi.playerMessage(5, "You can enter only if you have a Entry Pass to the Palace.");
		return false;
	}
}