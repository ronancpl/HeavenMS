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
/**
 * @Author Ronan
 */

function doorCross(pi) {
        pi.playPortalSound(); pi.warp(261030000, "sp_" + ((pi.getMapId() == 261010000) ? "jenu" : "alca"));
        return true;
}

function enter(pi) {
        if(pi.isQuestCompleted(3360)) {
                return doorCross(pi);
        } else if(pi.isQuestStarted(3360)) {
                if(pi.getQuestProgress(3360, 1) == 0) {
                        pi.openNpc(2111024, "MagatiaPassword");
                        return false;
                } else {
                        return doorCross(pi);
                }
        } else {
                pi.message("This door is locked.");
                return false;
        }
}