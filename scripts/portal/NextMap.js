function enter(pi) {
	pi.playPortalSound();
	pi.warp(pi.getMapId() + 100, 0);
	return true;
}