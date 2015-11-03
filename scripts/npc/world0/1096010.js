function start() {
	if (cm.isQuestStarted(2566)) {
		if (!cm.haveItem(4032985)) {
			if (cm.canHold(4032985)) {
				cm.gainItem(4032985, true);
				cm.earnTitle("You found the Ignition Device. Bring it to Cutter.");				
			}
		} else {
			cm.earnTitle("You already have the Ignition Device.");
		}
	}
	cm.dispose();
}