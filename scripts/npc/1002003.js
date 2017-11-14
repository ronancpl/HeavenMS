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
	NPC Name: 		Mr. Goldstein
	Map(s): 		Victoria Road : Lith Harbour (104000000)
	Description:		Extends Buddy List
*/
var status = 0;
	
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (status == 0 && mode == 0) {
		cm.sendNext("我明白...你没有我想象中的那么多朋友。哈哈哈，只是开个玩笑！无论如何，如果你改变注意了，请随时回来跟我谈生意。如果你有一大堆朋友，那么你知道的...呵呵...");
		cm.dispose();
		return;
	} else if (status >= 1 && mode == 0) {
		cm.sendNext("我明白...我不认为你没有我想象中的那么多朋友。如果是这样，那么你只是现在没有带够 240,000 金币？总之，如果你改变注意了，请随时回来跟我谈生意。当然，在你财务宽松的时候...呵呵...");
		cm.dispose();
		return;
	}	
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendYesNo("我希望我能跟昨天赚的一样多...嗯，你好！难道你不想扩展你的好友列表吗？你看起来像现充...嗯，你觉得怎么样？只要一些钱我就可以帮你做到这一点。不过记住，这个操作每次只对你账号中的一个角色有效，并不会影响其他角色。你想要扩展你的好友列表吗？");
	} else if (status == 1) {
		cm.sendYesNo("好的！其实这也并不是很贵。#b240,000 金币 我就会添加5个空位到你的好友列表#k。而且我从不分零售。一次购买终生受用。所以如果你需要更多空间的话，你也可以试试看。你觉得如何？你会为了它花费 240,000 金币吗？");
	} else if (status == 2) {
		var capacity = cm.getPlayer().getBuddylist().getCapacity();
		if (capacity >= 50 || cm.getMeso() < 240000){
			cm.sendNext("嘿... 你确定你有 #b240,000 金币#k？ 如果是的话确认一下你的好友栏是不是已经扩张到最大了。即使你再继续花钱，你的好友人数上限也就是 #b50#k。");
            cm.dispose();
		} else {
			var newcapacity = capacity + 5;
			cm.gainMeso(-240000);
			cm.getPlayer().setBuddyCapacity(newcapacity)		
			cm.sendOk("好的！你的好友列表已经多了5个空位。如果你的好友栏还需要更多的空间，你知道应该找谁。当然不是免费的...好吧...说了太多...");
			cm.dispose();
			}
		}
	}
}