function enter(pi) {
	if (pi.isQuestStarted(2570)) {
		pi.playPortalSound(); pi.warp(120000101, 0);
		return true;
	}
	pi.earnTitle("You still got some stuff to take care of. I can see it in your eyes. Wait...no, those are eye boogers.");
	return false;
}