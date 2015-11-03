function enter(pi) {
	pi.playPortalSound();
	pi.warp(pi.getMapId() + 10, "east00");
	return true;
}