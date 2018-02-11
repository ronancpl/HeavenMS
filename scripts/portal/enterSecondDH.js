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
 * @purpose Warps to the second drill hall
 */
function enter(pi) {
    var maps = [108000600, 108000601, 108000602];
    if(pi.isQuestStarted(20201) || pi.isQuestStarted(20202) || pi.isQuestStarted(20203) || pi.isQuestStarted(20204) || pi.isQuestStarted(20205)) {
        pi.removeAll(4032096);
        pi.removeAll(4032097);
        pi.removeAll(4032098);
        pi.removeAll(4032099);
        pi.removeAll(4032100);
        
        var rand = Math.floor(Math.random() * maps.length);
        pi.playPortalSound(); pi.warp(maps[rand], 0);
        return true;
    } else {
        return false;
    }
}