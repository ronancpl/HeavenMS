var status = 0;
var maps = [104000000, 102000000, 101000000, 103000000, 120000000];
var cost = [1000, 1000, 800, 1000, 800];
var selectedMap = -1;
var mesos;

function start() {
    cm.sendNext("你好，我驾驶普通出租车. 如果你想安全快速地在城镇之间移动, 那么就坐上来吧. 我们会很乐意以一个合适的价格将你送达目的地.");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status >= 2 && mode == 0) {
            cm.sendNext("这个城镇也有很多可看之处. 如果你想去其他城镇的话再来找我们吧.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            var selStr = "";
            if (cm.getJobId() == 0)
                selStr += "我们对新手玩家有 90% 的折扣.";
            selStr += "请选择目的地, 不同地点将会有不同的收费.#b";
            for (var i = 0; i < maps.length; i++)
                selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (cm.getJobId() == 0 ? cost[i] / 10 : cost[i]) + " mesos)#l";
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("你在这里已经没有事情可以做了, 对吧? 你确定要移动到 #b#m" + maps[selection] + "##k? 这会花费你 #b"+ (cm.getJobId() == 0 ? cost[selection] / 10 : cost[selection]) + " 金币#k.");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getJobId() == 0) {
            	mesos = cost[selectedMap] / 10;
            } else {
            	mesos = cost[selectedMap];
            }
            
            if (cm.getMeso() < mesos) {
                cm.sendNext("你没有足够的金币. 我很抱歉, 不过没有钱的话, 我没法让你乘坐出租车.");
                cm.dispose();
                return;
            }
            
            cm.gainMeso(-mesos);
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}