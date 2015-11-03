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
        Author : Generic (http://cronusemu.net)
        NPC Name:               Dalair
        Map(s):                 Every town
        Description:            Quest - Title Challenge - Celebrity!
        Quest ID :              29002
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendNext("Come back when you're ready.");
        qm.dispose();
    } else {
        if (mode > 0)
            status++;
        else
            status--;
        if (status == 0) {// Picture of Celebrity Medal(+blue text "Celebrity Medal"
            qm.sendAcceptDecline("#v1142003# #e#b#t1142003##k \r\n- Time Limit 30 Days \r\n- Popularity 1000Increase \r\n#nDo you want to test your skills to see if you're worthy of this title?");
        } else if (status == 1) {
            qm.sendNext("I'll give you 30 days to reach your goal.  Once you're finished, come back and see me.  Remember that you have to come back and see me within the time limit in order for it to be approved.  Also, unless you complete this challenge or quit first, you can't try out for another title.");
            qm.forceStartQuest();
        }

    }
        
}