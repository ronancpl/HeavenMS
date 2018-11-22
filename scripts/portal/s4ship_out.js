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
    var exit = pi.getEventInstance().getIntProperty("canLeave");
    if (exit == 0) {
        pi.message("You have to wait one minute before you can leave this place.");
        return false;
    } else if (exit == 2) {
        pi.playPortalSound();
        pi.warp(912010200);
        return true;
    } else {
        pi.playPortalSound();
        pi.warp(120000101);
        return true;
    }
}