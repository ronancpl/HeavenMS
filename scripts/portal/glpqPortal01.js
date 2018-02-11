function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 3) {
	pi.playPortalSound(); pi.warp(610030540,0);
        return true;
    } else {
	pi.playerMessage(5, "Only bowmen may enter this portal.");
        return false;
    }
}