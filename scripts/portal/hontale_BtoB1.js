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
@Author Jvlaple
*/

function enter(pi) {
    if (pi.getMap().countPlayers() == 1) {
        pi.getPlayer().dropMessage(6, "As the last player on this map, you are compelled to wait for the incoming keys.");
        return false;
    }else {
        if(pi.haveItem(4001087)) {
            pi.getPlayer().dropMessage(6, "You cannot pass to the next map holding the 1st Crystal Key in your inventory.");
            return false;
        }
        pi.playPortalSound(); pi.warp(240050101, 0);
        return true;
    }
}