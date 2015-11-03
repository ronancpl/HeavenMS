function enter(pi) {
	pi.playPortalSound();
	pi.warp(pi.getMapId() - 10, "west00");
	return true;
}