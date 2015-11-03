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
    Zakum Entrance
*/
 
function enter(pi) {
    if (!pi.haveItem(4001017)) {
        pi.getPlayer().dropMessage(6,"You do not have the Eye of Fire.  You may not face the boss.");
        return false;
    } 
	//var exped = pi.getEventManager("ZakumBattle").getInstance("ZakumBattle_" + pi.getPlayer().getClient().getChannel());
	//if (exped != null) {
	//	pi.getPlayer().dropMessage(6,"The battle at Zakum's Altar is currently underway.");
    //    return false;
	//}
    pi.warp(211042400,"west00");
    return true;
}