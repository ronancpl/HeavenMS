function start() {
	if (!cm.isQuestStarted(22015)) {
		cm.sendOk("#b(You are too far from the Piglet. Go closer to grab it.)");
	} else {
		cm.gainItem(4032449, true);
		cm.forceCompleteQuest(22015);
		cm.playerMessage(5, "You have rescued the Piglet.");
	}
	cm.dispose();
}