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
/*
	Author: kevintjuh93
*/
function enter(pi) {
	if (pi.getPlayer().getJob().getId() == 2000) {
		if (pi.isQuestStarted(21015)) {
			pi.showInfoText("You must exit to the right in order to find Murupas.");
			return false;
		} else if (pi.isQuestStarted(21016)) {
			pi.showInfoText("You must exit to the right in order to find Murupias.");
			return false;
		} else if (pi.isQuestStarted(21017)) {
			pi.showInfoText("You must exit to the right in order to find MuruMurus.");
			return false;
		}
	}
	pi.playPortalSound();
	pi.warp(140010000, 2);
	return true;
}