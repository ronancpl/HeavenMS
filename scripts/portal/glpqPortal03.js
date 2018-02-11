function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 4) {
	pi.playPortalSound(); pi.warp(610030530,0);
        return true;
    } else {
	pi.playerMessage(5, "Only thieves may enter this portal.");
        return false;
    }
}