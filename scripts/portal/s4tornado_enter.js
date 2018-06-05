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
 * @author BubblesDev v83 (Moogra)
 * @purpose Warps to the Junior Balrog map for the Rush Skill.
 */
function enter(pi) {
    if(pi.isQuestStarted(6230) || pi.isQuestStarted(6231) || pi.haveItem(4001110)) {
        if(pi.getWarpMap(922020200).countPlayers() == 0) {
            pi.resetMapObjects(922020200);
            pi.playPortalSound(); pi.warp(922020200, 0);
            
            return true;
        } else {
            pi.getPlayer().message("Some other player is currently inside.");
            return false;
        }
    }
    
    return false;
}