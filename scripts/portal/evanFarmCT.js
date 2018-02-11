function enter(pi) {
	if(pi.isQuestStarted(22010) || pi.getPlayer().getJob().getId() != 2001) {
		pi.playPortalSound(); pi.warp(100030310, 0);
	} else {
		pi.playerMessage(5, "Cannot enter the Lush Forest without a reason.");
	}
	return true;
}