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
/**
 * @author DiscoveryMS (Twdtwd), Ronan
 * @purpose Warps to the Cold Cave for Resurection.
 */
function enter(pi) {
    if(pi.haveItem(4001108)) {
        if(pi.getWarpMap(923000100).countPlayers() == 0) {
            pi.resetMapObjects(923000100);
            pi.playPortalSound(); pi.warp(923000100, 0);
            
            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }
    
    pi.getPlayer().message("A mysterious force won't let you in.");
    return false;
}