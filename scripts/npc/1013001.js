var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
	    cm.sendNext("You, who are destined to be a Dragon Master... You have finally arrived.", 1);
	} else if (status == 1) {
	    cm.sendNextPrev("Go and fulfill your duties as the Dragon Master...", 1);
	} else if (status == 2) {
	    cm.warp(900090101, 0);
	    cm.dispose();
	}
}