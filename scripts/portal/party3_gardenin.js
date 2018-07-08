function enter(pi) {
	if (pi.getPlayer().getParty() != null && pi.isEventLeader() && pi.haveItem(4001055,1)) {
                pi.playPortalSound();
                pi.getEventInstance().warpEventTeam(920010100);
                return true;
	} else {
		pi.playerMessage(5,"Please get the leader in this portal, make sure you have the Root of Life.");
                return false;
	}
}