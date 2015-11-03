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
		qm.sendNext("No, no, no. This isn't what I need. I need something more nutritious, master!");
	} else if (status == 1) {
		qm.sendNextPrev("#bHm... So you're not a herbivore. You might be a carnivore. You're a Dragon, after all. How does some #t4032453# sound?", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("What's a...#t4032453#? Never heard of it, but if it's yummy, I accept! Just feed me something tasty. Anything but plants!");
	} else if (status == 3) {
		if (mode == 0) {
			qm.sendNext("How can you starve me like this. I'm just a baby. This is wrong!");
		} else {
			qm.forceStartQuest();
			qm.sendNext("#b#b(Try giving #p1013000# some #t4032453#. You have to hunt a few #o1210100#s at the farm. Ten should be plenty...)");
		}
		qm.dispose();
	}
}