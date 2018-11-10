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
	Map(s): 		Empress' Road : Training Forest III
	Description: 		Takes you to Entrance to Drill Hall
*/

var jobtype = 4;

function enter(pi) {
	if(pi.isQuestStarted(20301) || pi.isQuestStarted(20302) || pi.isQuestStarted(20303) || pi.isQuestStarted(20304) || pi.isQuestStarted(20305)) {
		var map = pi.getClient().getChannelServer().getMapFactory().getMap(108010600 + (10 * jobtype));
                if(map.countPlayers() > 0) {
                        pi.message("Someone else is already searching the area.");
                        return false;
                }
                
                if(pi.haveItem(4032101 + jobtype, 1)) {
                        pi.message("You have already challenged the Master of Disguise, report your success to the Chief Knight.");
                        return false;
                }
                
		pi.playPortalSound(); pi.warp(108010600 + (10 * jobtype), "east00");
	} else {
		pi.playPortalSound(); pi.warp(130020000, "east00");
	}
	return true;
}