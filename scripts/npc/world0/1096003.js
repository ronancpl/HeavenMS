var status = -1;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		cm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		cm.sendDirectionInfo(4, 1096003);//else you will crash sending sendNext
		cm.sendDirectionInfo(3, 4);
		cm.sendNext("Ook! Ook!");
		cm.showIntro("Effect/Direction4.img/cannonshooter/face00");
	} else if (status == 1) {
		cm.unlockUI();
		cm.dispose();
	}
}