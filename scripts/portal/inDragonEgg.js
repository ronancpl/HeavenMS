function enter(pi) {
	pi.playPortalSound();
	if(pi.isQuestStarted(22005)){
		pi.warp(900020100);
	} else{
		pi.warp(100030301);
    }
	return true;
}  