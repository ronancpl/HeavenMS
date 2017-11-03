function start() {
	cm.forceCompleteQuest(22011);
	cm.playerMessage(5, "You have acquired a Dragon Egg.");//actually getInfoMessage
	cm.warp(900090103, 0);
	cm.dispose();
}