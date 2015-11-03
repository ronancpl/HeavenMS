function enter(pi) {
	if (pi.isQuestStarted(1041))
		pi.warp(1010100, 4);
	else if (pi.isQuestStarted(1042))
		pi.warp(1010200, 4);
	else if (pi.isQuestStarted(1043))
		pi.warp(1010300, 4);
	else if (pi.isQuestStarted(1044))
		pi.warp(1010400, 4);
	else {
		pi.message("Only the adventurers that have been trained by Mai may enter.");
		return false;
	}
	return true;
}