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
	NPC Name: 		Pison
	Map(s): 		Victoria Road : Lith Harbor (104000000)
	Description: 		Florina Beach Tour Guide
 */
var status = 0;

function start() {
    cm.sendSimple("你有没有听过一个风景绝伦的海滩——#b黄金海滩#k，就在明珠港的旁边？我现在就能带你去那里，只需要 #b1500 枫币#k, 或者如果你有一张#b到黄金海滩的VIP票#k 那么就可以免费去..\r\n\r\n#L0##b 我愿意付 1500 枫币.#l\r\n#L1# 我有一张到黄金海滩的VIP票.#l\r\n#L2# 什么是到黄金海滩的VIP票#k?#l");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1)
        if((mode == 0 && type == 1) || mode == -1 || (mode == 0 && status == 1)){
            if(type == 1)
                cm.sendNext("你一定在这里还有什么事情没办完。你一定厌倦了所有的旅行和狩猎。去休息一下吧，如果你改变了主意，再来和我谈谈。");
            cm.dispose();
            return;
        } else
            status -= 2;
    if (selection == 0)
        status++;
    if(status == 1){
        if(selection == 1)
            cm.sendYesNo("那么你有#b到黄金海滩的VIP票#k？你可以永远凭着它前往黄金海滩。好的，不过注意你在那边也会碰到一些怪物。OK，你准备好前往黄金海滩了吗？");
        else if (selection == 2)
            cm.sendNext("你一定好奇什么是 #b到黄金海滩的VIP票#k。 哈哈，这是非常可以理解的。只要你有到黄金海滩的VIP票，你可以终生免费去往黄金海滩。它如此宝贵以至于我们都必须购买，不过不幸的是前几周在我度假时我把自己的VIP票弄丢了。");
    } else if (status == 2){
        if(type != 1 && selection != 0) {
            cm.sendNextPrev("我回来的时候没有它，这感觉真是太糟糕了。希望有人把它捡起来并且放到安全的地方。无论如何，这是我的故事，而且谁知道呢，或许你可以捡到它并且把它用好。如果您有任何问题，请随时咨询。");
			cm.dispose();
		} else{
            if (cm.getMeso() < 1500 && selection == 0)
                cm.sendNext("我觉得你缺了一点金币。有很多方式去获取金币，你知道的，比如说...出售你的装备...打败怪物...做任务...你知道我在说什么。");
            else if(!cm.haveItem(4031134) && selection != 0){
                cm.sendNext("嗯...所以你的 #b到黄金海滩的VIP票#k在哪？请再次确认。");
            }else{
                if(selection == 0)
                    cm.gainMeso(-1500);
                cm.getPlayer().saveLocation("FLORINA");
                cm.warp(110000000, "st00");
            }
            cm.dispose();
        }
    }
}