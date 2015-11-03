function start(ms) {
	if (ms.isQuestCompleted(21101) && ms.containsAreaInfo(21019, "miss=o;arr=o;helper=clear")) {
		ms.updateAreaInfo(21019, "miss=o;arr=o;ck=1;helper=clear");
	}
	ms.unlockUI();
}