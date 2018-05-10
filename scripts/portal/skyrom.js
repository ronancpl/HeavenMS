/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
    if(pi.isQuestStarted(3935) && !pi.haveItem(4031574, 1)) {
        if(pi.getWarpMap(926000010).countPlayers() == 0) {
            pi.playPortalSound(); pi.warp(926000010);
            return true;
        } else {
            pi.message("Someone is already trying this map.");
            return false;
        }
    } else {
        return false;
    }
}