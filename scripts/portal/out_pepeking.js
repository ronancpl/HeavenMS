function enter(pi) {
	var eim = pi.getEventInstance();
	eim.stopEventTimer();
	eim.dispose();
	
	pi.playPortalSound();
	pi.warp(106021400, 2);
	return true;
}