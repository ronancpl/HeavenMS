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
/* @Author Lerk
 * @Author Ronan
 * 
 * Guild Quest Waiting Room - Entry Portal (map 990000000)
 */

function enter(pi) {
        var entryTime = pi.getPlayer().getEventInstance().getProperty("entryTimestamp");
        var timeNow = Date.now();
    
        var timeLeft = Math.ceil((entryTime - timeNow) / 1000);
    
        if(timeLeft <= 0) {
            pi.playPortalSound(); pi.warp(990000100, 0);
            return true;
        }
        else { //cannot proceed while allies can still enter
            pi.playerMessage(5, "The portal will open in about " + timeLeft + " seconds.");
            return false;
        }
}