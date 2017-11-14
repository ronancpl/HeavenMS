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
	NPC Name: 		Shanks
	Map(s): 		Maple Road : Southperry (60000)
	Description: 		Brings you to Victoria Island
*/
var status = 0;

function start() {
    cm.sendYesNo("如果你想离开这里，你需要付给我 #e150 金币#n  我会带你去 #b金银岛#k. 但关键的是，你一旦离开，你就无法再回来这里。你是不是想要去金银岛？");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0 && type != 1)
            status -= 2;
        else if(type == 1 || (mode == -1 && type != 1)){
            if(mode == 0)
                cm.sendOk("嗯... 我猜你还有什么别的事情要在这里做吧？");
            cm.dispose();
            return;
        }
    }
    if (status == 1) {
        if (cm.haveItem(4031801))
            cm.sendNext("好, 现在给我 150 金币... 嗯, 那是什么？ 是彩虹村村长路卡斯的推荐信？ 嘿, 你应该告诉我. 伟大的冒险家。我，香克斯，能看出你的作为冒险者的强大潜力。 我怎么能够收你的旅费！");
        else
            cm.sendNext("对这里感到厌倦了吗？那么... 先给我 #e150 金币#n 吧...");
    } else if (status == 2) {
        if (cm.haveItem(4031801))
            cm.sendNextPrev("既然你有推荐信，我也不会向你收任何费用的。好了。我们现在就向金银岛出发。船可能会有点震荡，坐好了...");
        else
        if (cm.getLevel() > 6) {
            if (cm.getMeso() < 150) {
                cm.sendOk("什么？你想去金银岛但你没有钱？你是一个怪人...");
                cm.dispose();
            } else
                cm.sendNext("很好! #e150#n 金币! 那么，我们现在就向金银岛出发吧!");
        } else {
            cm.sendOk("来让我看看，我不认为你有足够的资格去明珠港。你等级至少应该在7级或者7级以上。");
            cm.dispose();
        }
    } else if (status == 3) {
        if (cm.haveItem(4031801)) {
            cm.gainItem(4031801, -1);
        } else {
            cm.gainMeso(-150);
        }
        cm.warp(104000000, 0);
        cm.dispose();
    }
}