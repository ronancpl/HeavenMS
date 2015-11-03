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
		qm.sendNext("Haha. I had a good laugh. Hahaha. But enough with that nonsense. Feed #p1013102#, would you?");
	} else if (status == 1) {
		qm.sendNextPrev("#bWhat? That's #p1013101#'s job!", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("You little brat! I told you to call me Older Brother! You know how much #p1013102# hates me. He'll bite me if I go near him. You feed him. He likes you.");
	} else if (status == 3) {
		if (mode == 0) {
			qm.sendNext("Stop being lazy. Do you want to see your brother bitten by a dog? Hurry up! Talk to me again and accept the quest!");
			qm.dispose();		
		} else {//accept
			qm.gainItem(4032447, true);
			qm.forceStartQuest();
			qm.sendNext("Hurry up and head #bleft#k to feed #b#p1013102##k. He's been barking to be fed all morning.");
		}
	} else if (status == 4) {
		qm.sendPrev("Feed #p1013102# and come back to see me.");
		qm.dispose();
	}
}