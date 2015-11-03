function enter(pi) {
	if (pi.isQuestStarted(1035))
		pi.showInfo("UI/tutorial.img/20");

	pi.blockPortal();
	return true;
}