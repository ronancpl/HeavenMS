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
    if (!(pi.isQuestStarted(100200) || pi.isQuestCompleted(100200))) {
        pi.getPlayer().dropMessage(5,"You need approval from the masters to battle. You may not attempt the boss right now.");
        return false;
    }
    
    if (!pi.isQuestCompleted(100201)) {
        pi.getPlayer().dropMessage(5,"You haven't completed all the trials yet. You may not attempt the boss right now.");
        return false;
    }
    
    if (!pi.haveItem(4001017)) {    // thanks Conrad for pointing out missing checks for token item and unused reactor
        pi.getPlayer().dropMessage(5,"You do not have the Eye of Fire. You may not face the boss.");
        return false;
    }
    
    var react = pi.getMap().getReactorById(2118002);
    if (react != null && react.getState() > 0) {
        pi.getPlayer().dropMessage(5,"The entrance is currently blocked.");
        return false;
    }
    
    pi.playPortalSound(); pi.warp(211042400,"west00");
    return true;
}