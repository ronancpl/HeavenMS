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
		qm.sendNext("Ooook! Ook! Ook!");
	} else if (status == 1) {
		qm.sendNextPrev("Well, that hit the spot, but... I still don't understand what happened. Where's the ship? Hey, do you know what happened to me?", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("Oook! (The monkey nods. Does he really know what's going on? Couldn't hurt to ask.)");
	} else if (status == 3) {
		if (mode == 0) {//decline
			qm.sendNext("Ook! Ook! (The monkey looks very dissatisfied.)");
		} else {
			qm.forceStartQuest();
		}
		qm.dispose();
	}
}
	