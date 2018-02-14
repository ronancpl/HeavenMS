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
MiniDungeon - Skele
*/ 
var baseid = 240040511;
var dungeonid = 240040800;
var dungeons = 19;

function enter(pi) {
    if (pi.getMapId() == baseid) {
        if (pi.getParty() != null) {
            if (pi.isLeader()) {
                for (var i = 0; i < dungeons; i++) {
                    if(pi.startDungeonInstance(dungeonid + i)) {
                        pi.playPortalSound();
                        pi.warpParty(dungeonid + i);
                        return true;
                    }
                }
            } else {
                pi.playerMessage(5, "Only solo or party leaders are supposed to enter the Mini-Dungeon.");
                return false;
            }
        } else {
            for (var i = 0; i < dungeons; i++) {
                if(pi.startDungeonInstance(dungeonid + i)) {
                    pi.playPortalSound();
                    pi.warp(dungeonid + i);
                    return true;
                }
            }
        }
        pi.playerMessage(5, "All of the Mini-Dungeons are in use right now, please try again later.");
        return false;
    } else {
    	pi.playPortalSound();
    	pi.warp(baseid, "MD00");
    	return true;
    }
}