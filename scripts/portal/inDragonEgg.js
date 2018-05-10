function enter(pi) {
	pi.playPortalSound();
	if(pi.isQuestStarted(22005)){
		pi.playPortalSound(); pi.warp(900020100, 0);
	} else{
		pi.playPortalSound(); pi.warp(100030301, 0);
    }
	return true;
}  