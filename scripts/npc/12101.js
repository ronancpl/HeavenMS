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
/* Author: Xterminator
	NPC Name: 		Rain
	Map(s): 		Maple Road : Amherst (1010000)
	Description: 		Talks about Amherst
*/
var status = -1;

function start() {
    cm.sendNext("这里是 #b彩虹村#k，位于彩虹岛的最北边。 你知道彩虹岛是给新手准备的，对吧？ 周边只有弱小的怪物我很开心。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0 && status == 2){
            status -= 2;
            start();
        }else if(mode == 0)
            status-= 2;
        else
            cm.dispose();
    }else{
        if (status == 1)
            cm.sendNextPrev("如果你想变得更强，去 #b南港#k 乘坐那艘巨大的船前往 #b金银岛#k 吧。 跟它的规模相比，这座小岛不值一提。");
        else if (status == 2)
            cm.sendPrev("在金银岛你可以选择职业。 是叫 #b勇士部落#k... 吗？ 我听说那里住着很多战士，是一个贫瘠且与世隔离的村庄。 一个高原...那到底会是一个怎样的地方呢？");
        else if (status == 3)
            cm.dispose();
    }
}