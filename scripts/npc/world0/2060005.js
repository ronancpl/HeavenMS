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
function start() {
    if(cm.isQuestCompleted(6002))
        cm.sendOk("Thanks for saving the pork.");
    else if(cm.getClient().getChannelServer().getMapFactory().getMap(923010000).getCharacters().size() > 0)
        cm.sendOk("There is currently someone in this map, come back later.");
    else if(cm.isQuestStarted(6002))
        cm.warp(923010000);
    else cm.sendSimple("Only few adventurers, from a selected public, are eligible to protect the Watch Hog.");
    cm.dispose();
}