var status = -1;

function end(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendNext("Oh, did you bring the #t4032451#? Here, give it to me. I'll give you the Incubator then.");
	} else if (status == 1) {
		qm.sendYesNo("Alright, here you go. I have no idea how you use it, but it's yours... \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 360 exp");
	} else if (status == 2) {
		if (mode == 0) {//decline
			qm.sendNext("Hm? That's strange. The Incubator wasn't installed properly. Try again.");
		} else {
			qm.gainItem(4032451, -1);
			qm.forceCompleteQuest();
			qm.gainExp(360);
			qm.sendImage("UI/tutorial/evan/9/0");
		}
		qm.dispose();
	}
}