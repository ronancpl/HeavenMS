function enter(pi) {
	if (pi.isQuestCompleted(2260) ||
                        pi.isQuestStarted(2300) || pi.isQuestCompleted(2300) ||
                        pi.isQuestStarted(2301) || pi.isQuestCompleted(2301) || 
			pi.isQuestStarted(2302) || pi.isQuestCompleted(2302) || 
			pi.isQuestStarted(2303) || pi.isQuestCompleted(2303) ||
			pi.isQuestStarted(2304) || pi.isQuestCompleted(2304) || 
			pi.isQuestStarted(2305) || pi.isQuestCompleted(2305) || 
			pi.isQuestStarted(2306) || pi.isQuestCompleted(2306) ||
			pi.isQuestStarted(2307) || pi.isQuestCompleted(2307) ||
			pi.isQuestStarted(2308) || pi.isQuestCompleted(2308) ||
			pi.isQuestStarted(2309) || pi.isQuestCompleted(2309) ||
			pi.isQuestStarted(2310) || pi.isQuestCompleted(2310)) {
		pi.playPortalSound();
		pi.warp(106020000, 0);
		return true;
	}
	pi.playerMessage(5, "A strange force is blocking you from entering.");
	return false;
}