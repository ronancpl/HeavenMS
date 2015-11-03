function enter(pi) {
	pi.playPortalSound();
	pi.warp(pi.getMapId() + 100);
	return true;
}