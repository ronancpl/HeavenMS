function enter(pi) {
	var questProgress = pi.getQuestProgress(2330, 3300005) + pi.getQuestProgress(2330, 3300006) + pi.getQuestProgress(2330, 3300007); //3 Yetis

	if(pi.isQuestStarted(2330) && questProgress < 3){
		pi.openNpc(1300013);
	}
	else{
		pi.playPortalSound();
		pi.warp(106021401, 1);
	}
	
	return true;
}