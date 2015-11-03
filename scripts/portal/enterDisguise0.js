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
/* 
	Map(s): 		Empress' Road : Crossroads of Ereve
	Description: 		Takes you to Training Forest I
*/

function enter(pi) {
	if(pi.isQuestStarted(20301) || pi.isQuestStarted(20302) || pi.isQuestStarted(20303) || pi.isQuestStarted(20304) || pi.isQuestStarted(20305)) {
		if(pi.hasItem(4032179)) {
			pi.playPortalSound();
			pi.warp(130010000, "east00");
		} else {
			pi.getPlayer().dropMessage(5, "Due to the lock down you can not enter without a permit.");
			return false;
		}
	} else {
		pi.playPortalSound();
		pi.warp(130010000, "east00");
	}
	return true;
}