var status = -1;
var map = 910060000;
var num = 5;
var maxp = 5;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status <= 1) {
	    cm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
	if (cm.isQuestStarted(22515) || cm.isQuestStarted(22516) || cm.isQuestStarted(22517) || cm.isQuestStarted(22518)) {
		cm.sendYesNo("Would you like to go in the special Spore Training Center?");
		status = 1;
	}
	var selStr = "Would you like to go into the Training Center?";
	for (var i = 0; i < num; i++) {
		selStr += "\r\n#b#L" + i + "#Training Center " + i + " (" + cm.getPlayerCount(map + i) + "/" + maxp + ")#l#k";
	}
	cm.sendSimple(selStr);
    } else if (status == 1) {
	if (selection < 0 || selection >= num) {
		cm.dispose();
	} else if (cm.getPlayerCount(map + selection) >= maxp) {
		cm.sendNext("This training center is full.");
		status = -1;
	} else {
		cm.warp(map + selection, 0);
		cm.dispose();
	}
    } else if (status == 2) {
	cm.warp(910060100,0);
	cm.dispose();
    }
}