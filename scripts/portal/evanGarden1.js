function enter(pi) {
	if(pi.isQuestStarted(22008)){
		pi.playPortalSound(); pi.warp(100030103, "west00");
	} else {
		pi.playerMessage(5, "You cannot go to the Back Yard without a reason");
    } 
	return true;
}  