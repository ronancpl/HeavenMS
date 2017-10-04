function enter(pi) {
	
	if(!pi.isQuestStarted(2335) || (pi.isQuestStarted(2335) && !pi.hasItem(4032405))){
		pi.getPlayer().message("The door is locked securely. I will need a key if I want to go in there.");
		return false;
	}

	if(pi.isQuestStarted(2335) && pi.hasItem(4032405)){
		pi.forceCompleteQuest(2335, 1300002);
		pi.giveCharacterExp(5000 * 1.5, pi.getPlayer());
		pi.gainItem(4032405, -1);
	}
	pi.playPortalSound();
	pi.warp(106021001, 1);
	return true;
}