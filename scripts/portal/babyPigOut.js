function enter(pi) {
	if (pi.isQuestCompleted(22015)) {
		pi.playPortalSound();
		pi.warp(100030300, 2);
	} else {
		pi.playerMessage(5, "Please rescue the baby pig!");//not gms like
	}
	return true;
}