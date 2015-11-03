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
function act() {
    if(rm.getPlayer().getEventInstance() != null){
		rm.getPlayer().getEventInstance().setProperty("summoned", "true");
		rm.getPlayer().getEventInstance().setProperty("canEnter", "false");
	}
    rm.changeMusic("Bgm06/FinalFight");
    rm.spawnFakeMonster(8800000);
    for (i=8800003; i<8800011; i++)
        rm.spawnMonster(i);
    rm.createMapMonitor(280030000,"ps00");
    rm.mapMessage(5, "Zakum is summoned by the force of Eye of Fire.");
}
