var status = -1;

function start(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendAcceptDecline("You're back! Great. I got the Ignition Device all hooked up, so we can get back to civilization. Nothing left to do here, right? Let's roll!");
	} else if (status == 1) {
		if (mode == 0) {//decline
			
		} else {
			qm.forceStartQuest();
			qm.warp(912060200);
		}
		qm.dispose();
	}
}

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
		qm.sendNext("");
	}	
}
	