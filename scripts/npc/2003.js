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
	NPC Name: 		Robin
	Map(s): 		Maple Road : Snail Hunting Ground I (40000)
	Description: 		Beginner Helper
*/
var status;
var sel;

function start() {
    status = -1;
    sel = -1;
    cm.sendSimple("我可以告诉你些冒险者的技巧哦!!\r\n#L0##b要怎么移动？#l\r\n#L1#我要如何击退怪物？#l\r\n#L2#我要怎么捡起物品？#l\r\n#L3#当我死掉会发生什么事情？#l\r\n#L4#我何时能选择职业？#l\r\n#L5#告诉我有关这个岛屿的事情！#l\r\n#L6#我要怎么做才能成为战士？#l\r\n#L7#我要怎么做才能成为弓箭手？#l\r\n#L8#我要怎么做才能成为魔法师？#l\r\n#L9#我要怎么做才能成为飞侠？#l\r\n#L10#怎么提升能力值？(S)#l\r\n#L11#我要怎么确认我捡起来的物品呢？#l\r\n#L12#我要怎么装备物品？#l\r\n#L13#我要怎么确认我身上已经装备的物品？#l\r\n#L14#什么是技能？(K)#l\r\n#L15#我要怎么前往金银岛？#l\r\n#L16#金币是什么？#l#k");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(mode == 0 && type != 4)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        if(sel == -1)
            sel = selection;
        if (sel == 0)
            cm.sendNext("好，我来教你如何移动。 使用 #b方向左键#k 就能在平台上移动了，按下 #bAlt#k 可以进行跳跃。 有些鞋子能提升你的速度以及跳跃力。");
        else if (sel == 1)
            cm.sendNext("好，击退怪物很简单，每个怪物有自己的血条，你可以使用武器将他们杀死。当然，如果怪物等级越高，你越难击退它们。");
        else if (sel == 2)
            cm.sendNext("接下来告诉你如何剪取物品，当你击退怪物时，会有机会掉落宝物以及金币，当地上有物品时，按下#bZ#k 或是 数字键盘上的 #b0#k 来捡取物品。");
        else if (sel == 3)
            cm.sendNext("你好奇地找出当你死会发生什么吗？ 当你的HP归零时，你会变成幽灵。 而地上会出现一块墓碑，而你无法移动，但是你还是可以聊天。");
        else if (sel == 4)
            cm.sendNext("什么时候你可以选择你的职业？哈哈哈，别紧张，我的朋友啊～每个职业都有等级的限制。通常在8级和10级之间会进行。");
        else if (sel == 5)
            cm.sendNext("你想要了解这个岛屿吗？ 这里是彩虹岛，这座岛屿浮在天空上。由于浮在天空上，强大的怪物们无法靠近。这里非常和平，非常适合新手。");
        else if (sel == 6)
            cm.sendNext("你想成为#b战士#k？ 嗯...那我建议你到金银岛，寻找一个叫做#r勇士部落#k的战士村庄并且去找#b武术教练#k。 他会教你如何成为一个战士。 喔对了，有件很重要的事，你必须达到等级10才能成为战士！");
        else if (sel == 7)
            cm.sendNext("你想成为#b弓箭手#k？ 那你必须得去金银岛进行转职。去一个叫做#r射手村#k的弓箭手村落与美丽的#b赫丽娜#k对话并且学习作为弓箭手的一切。 喔对了，有件很重要的事，你必须达到等级10才能成为弓箭手！");
        else if (sel == 8)
            cm.sendNext("你想成为#b魔法师#k? 如果你意已决，那你首先得去金银岛。 然后前往一个叫做 #r魔法密林#k 的魔法师之乡，魔法图书馆位于它的顶端。 在那里，你会见到魔法师会长 #b汉斯#k，他会教你成为魔法师的知识。");
        else if (sel == 9)
            cm.sendNext("你想成为#b飞侠#k? 如果你确定的话，那么你得先去金银岛。 接着前往一个名为 #r废弃都市#k 的飞侠城镇, 在这座城镇的阴暗处，你会见到飞侠的隐蔽之所。在那里，你会遇见 #b达克鲁#k，他将指导你成为一名飞侠。 喔对了，有件很重要的事，你必须达到等级10才能成为飞侠！");
        else if (sel == 10)
            cm.sendNext("你想知道如何提升角色的能力值？ 首先按 #bS#k 键检查你的能力值窗口。 每次你升级的时候你会获得5点能力值奖励。 请自行分配能力值，这很简单。");
        else if (sel == 11)
            cm.sendNext("你想知道如何检查捡取的物品，对吗？当你打败一个怪物，它会掉落一些物品在地面上，你可以按 #bZ#k 键去捡取这些物品。 捡取到的物品会保存在你的背包中，你可以很容易地按 #bI#k 查看他们。");
        else if (sel == 12)
            cm.sendNext("你想知道如何穿戴物品，是吗？ 按 #bI#k 键来检查你的物品栏。 把你的鼠标放置在物品上方并且双击来穿戴它们。 如果你发现自己无法使用物品，可能是因为你没有满足所要求的等级或者能力值。 你也可以通过拖曳物品到装备栏（#bE#k）中来使用他们。 在装备栏中双击物品可以脱卸。");
        else if (sel == 13)
            cm.sendNext("你想看你装备好的物品，是吗？ 按 #bE#k 键来打开装备栏，你会发现现在你所穿戴的物品。 双击它们即可脱卸，卸下的物品将会回到你的背包中。");
        else if (sel == 14)
            cm.sendNext("在你转职后获取的特殊‘能力’叫做技能。 你只能获取自身对应职业的技能。 现在你还没有到达那个阶段，所以你现在还没有任何职业技能。 不过记住按 #bK#k 键就可以打开技能书。 之后这会帮到你。");
        else if (sel == 15)
            cm.sendNext("怎样去金银岛？ 在这片岛屿的东部有一个叫做 #b南港#k 的港口。 你会在那里看到一艘浮空的船只。 船前方站着船长，试着跟他对话吧。");
        else if (sel == 16)
            cm.sendNext("这是冒险岛中的货币，你可以通过金币购买物品。 有多种方式可以获取金币。 你可以狩猎怪物、售卖物品或者完成任务等等...");
    } else if (status == 1) {
        if (sel == 0)
            cm.sendNextPrev("为了攻击怪物，你必须装备武器。 一旦武器在身，按 #bCtrl#k 键去使用武器。 适时出手能让你轻易打败怪物。");
        else if (sel == 1)
            cm.sendNextPrev("一旦转职成功，你将会获得不同类型的技能，并且你可以为技能设置快捷键。 如果它是攻击技能，你不需要再使用 #bCtrl#k 来攻击，只需按你所设置的攻击技能快捷键即可。");
        else if (sel == 2)
            cm.sendNextPrev("不过记住，一旦你的背包满仓，你将无法获取更多的物品。 所以你可以出售你不需要的物品来腾出空间。 在你转职之后背包空间会随之扩张。");
        else if (sel == 3)  
            cm.sendNextPrev("当你还是新手的时候，死亡并不会造成太大损失。 但是当你转职后，事情就变得不一样了。 当你死亡时你将会损失一定比例的经验值，因此请尽一切代价保证自己远离危险和死亡。");
        else if (sel == 4) 
            cm.sendNextPrev("等级并不是唯一决定你进步的事物。 你同样需要提升能力值。 比如说，为了成为一个战士，你的力量必须高于35，等等。 你明白我的意思吗？ 保证你提升的能力值与你的职业有直接的联系。");
        else if (sel == 5)
            cm.sendNextPrev("不过，如果你想变成一名强大的玩家，请不要考虑在此逗留太长时间。 在这你无法获得职业。 在这片土地下有着一片更广阔的天空，名为金银岛。 你会在那里玩的很开心。");
        else if (sel == 8)
            cm.sendNextPrev("哦顺便一提，和其他职业不同，魔法师在8级就可以转职。 因此这个职业会提升更早，但同样也说明成为一个真正强大的魔法师需要更多的历练。 在选择自己的道路之前尽情思索吧。");
        else if (sel == 10)
            cm.sendNextPrev("将你的鼠标移动到能力上方会出现一小段说明。比如，战士需要力量，弓箭手需要敏捷，魔法师需要智力，飞侠需要运气。你并不需要知道它们本身，你只需要考虑如何合理分配点数强化自己。");
        else if (sel == 15)
            cm.sendNextPrev("哦对！在我走之前还有最后一件事。如果你不确定你在哪里，请按 #bW#k。世界地图将会展开并且显示你所在的位置。所以你不必担心迷路。");
        else
            start();
    }else
        start();
}