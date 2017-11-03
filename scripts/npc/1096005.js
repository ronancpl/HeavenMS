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
		cm.sendDirectionInfo(4, 1096005);//else you will crash sending sendNext
		cm.sendNext("All right! Let's go!");
	} else if (status == 1) {
		cm.removeNPC(579711);
		cm.removeNPC2(579711);
		cm.updateInfo("fire", "0");
		cm.playSound("cannonshooter/fire");
		cm.sendDirectionInfo("Effect/Direction4.img/effect/cannonshooter/flying/0", 7000, 0, 0, -1, -1);
		cm.sendDirectionInfo("Effect/Direction4.img/effect/cannonshooter/flying1/0", 7000, 0, 0, -1, -1);
		cm.sendDirectionInfo(1, 800);
	} else if (status == 2) {
		cm.warp(912060300, 0);
	} else if (status == 3) {
		cm.sendDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/1", 9000, 0, 0, 0, -1);
		cm.sendDirectionInfo(1, 1500);
	} else if (status == 4) {
		cm.sendDirectionInfo("Effect/Direction4.img/effect/cannonshooter/balloon/2", 9000, 0, 0, 0, -1);
		cm.showIntro("Effect/Direction4.img/cannonshooter/face04");
		cm.showIntro("Effect/Direction4.img/cannonshooter/out01");
		cm.dispose();
	}
}