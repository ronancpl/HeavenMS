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

    Edited by: Kevin
*/
function enter(pi) {
    if(pi.isQuestStarted(20718)){
        var cml = pi.getEventManager("Cygnus_Magic_Library");
        cml.setProperty("player", pi.getPlayer().getName());
        cml.startInstance(pi.getPlayer());
        pi.playPortalSound();
    }
    else{
        pi.playPortalSound();
        pi.warp(101000003, 8);
    }
    return true;
}