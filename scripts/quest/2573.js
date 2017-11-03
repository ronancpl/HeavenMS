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
		qm.sendNext("Greetings! Isn't this just the perfect weather for a journey? I'm Skipper, the captain of this fine ship. You must be a new Explorer, eh? Nice to meet you.");
	} else if (status == 1) {
		qm.sendAcceptDecline("We're not quite ready to leave, so feel free to look around the ship while we're waiting.");
	} else if (status == 2) {
		if (mode == 0) {//decline
			qm.sendNext("Hey, take it easy! Sometimes you just gotta wait.");
		} else {
			qm.sendNext("Looks like we're all set! I think this is going to be a great voyage. Let's get underway.");
			qm.warp(3000000, 0);
			qm.forceCompleteQuest();
		}
		qm.dispose();
	}
}