var status = 0;
var maps = [100000000, 102000000, 101000000, 103000000, 120000000];
var cost = [1000, 1000, 800, 1000, 800];
var selectedMap = -1;
var mesos;

function start() {
	if (cm.hasItem(4032313,1)) {
		cm.sendOk("我看到你有去射手村的优惠票了。只需片刻我就能送你过去！");
	} else {
		cm.sendNext("你好，我驾驶普通出租车。如果你想安全又快速地到达其他城镇，那么来乘坐我们的出租车吧。价格低廉，我们乐意将您送往目的地。");
	}
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status >= 2 && mode == 0) {
            cm.sendNext("这个城镇还有很多值得一看。如果你想去其他城镇请回来告诉我们。");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
        	if (cm.hasItem(4032313,1)) {
        		cm.gainItem(4032313, -1);
                cm.warp(maps[0], 0);
                cm.dispose();
                return;
        	}
            var selStr = "";
            if (cm.getJobId() == 0)
                selStr += "我们对新手有 90% 的特别折扣。";
            selStr += "选择你的目的地，费用会随着目的地变化。#b";
            for (var i = 0; i < maps.length; i++)
                selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (cm.getJobId() == 0 ? cost[i] / 10 : cost[i]) + " 金币)#l";
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("你在这没有其他事情了，是吧？你确定要前往 #b#m" + maps[selection] + "##k？ 这将花费 #b"+ (cm.getJobId() == 0 ? cost[selection] / 10 : cost[selection]) + " 金币#k.");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getJobId() == 0) {
            	mesos = cost[selectedMap] / 10;
            } else {
            	mesos = cost[selectedMap];
            }
            
            if (cm.getMeso() < mesos) {
                cm.sendNext("你没有足够的金币。很抱歉，但是没有足够的钱你无法乘坐出租车。");
                cm.dispose();
                return;
            }
            
            cm.gainMeso(-mesos);
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}