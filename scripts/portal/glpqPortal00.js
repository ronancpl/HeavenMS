function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 1) {
	pi.playPortalSound(); pi.warp(610030510,0);
        return true;
    } else {
	pi.playerMessage(5, "Only warriors may enter this portal.");
        return false;
    }
}