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

/*
Agit's hideout (leader of the Sand Bandits)
@author Ronan
*/

function enter(pi) {
        if(pi.isQuestCompleted(3928) && pi.isQuestCompleted(3931) && pi.isQuestCompleted(3934)) {
                pi.playPortalSound(); pi.warp(260000201, 1);
                return true;
        } else {
                pi.message("Access restricted to only members of the Sand Bandits team.");
                return false;
        }
}