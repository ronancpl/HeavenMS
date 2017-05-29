function start() {
	if (cm.isQuestStarted(22007)) {
		if (!cm.haveItem(4032451)) {
			cm.gainItem(4032451, true);
			cm.sendNext("#b(You have obtained an Egg. Deliver it to Utah.)");
		} else {
			cm.sendNext("#b(You have already obtained an Egg. Take the Egg you have and give it to Utah.)");
		}
	} else {
		cm.sendNext("#b(You don't need to take an egg now.)#k");
	}
	cm.dispose();
}