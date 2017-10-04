function enter(pi) {
	var questProgress = pi.getQuestProgress(2330, 3300005) + pi.getQuestProgress(2330, 3300006) + pi.getQuestProgress(2330, 3300007); //3 Yetis

	if(questProgress == 3 && !pi.hasItem(4032388) && !pi.isQuestCompleted(2332)){
		if(pi.canHold(4032388)){
			pi.getPlayer().message("You have aquired a key to the Wedding Hall. King Pepe must have dropped it.")
			pi.gainItem(4032388, 1);
		}
		else{
			pi.getPlayer().message("Please make room in your ETC inventory.");
		}
	}

	if(pi.isQuestStarted(2330) && questProgress < 3){
		pi.openNpc(1300013);
	}
	else{
		pi.playPortalSound();
		pi.warp(106021401, 1);
	}
	
	return true;
}