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
function enter(pi) {
	if (pi.getPlayer().getMapId() == 240060000) {
		if(pi.getEventInstance().getIntProperty("defeatedHead") >= 1) {
                        pi.playPortalSound(); pi.warp(240060100, 0);
                        return true;
                } else {
                        pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
                        return false;
                }
	} else if (pi.getPlayer().getMapId() == 240060100) {
		if(pi.getEventInstance().getIntProperty("defeatedHead") >= 2) {
                        pi.playPortalSound(); pi.warp(240060200, 0);
                        return true;
                } else {
                        pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
                        return false;
                }
	}
	return false;
}