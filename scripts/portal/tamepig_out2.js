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
    if(!(pi.haveItem(4031507, 5) && pi.haveItem(4031508, 5) && pi.isQuestStarted(6002))) {
        pi.removeAll(4031507);
        pi.removeAll(4031508);
    }
    
    if (pi.getClient().getChannelServer().getMapFactory().getMap(923010000).getCharacters().size() < 1)
        pi.getClient().getChannelServer().getMapFactory().getMap(923010000).killAllMonsters();
    pi.warp(230000003, "out00");
    return true;
}