function enter(pi) {
	var questProgress = pi.getQuestProgressInt(2330, 3300005) + pi.getQuestProgressInt(2330, 3300006) + pi.getQuestProgressInt(2330, 3300007); //3 Yetis

	if(pi.isQuestStarted(2330) && questProgress < 3){
		pi.openNpc(1300013);
	}
	else{
		pi.playPortalSound();
		pi.warp(106021401, 1);
	}
	
	return true;
}