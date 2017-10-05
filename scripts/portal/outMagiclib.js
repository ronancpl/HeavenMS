function enter(pi) {
	if(pi.getMap().countMonster(2220100) > 0){
		pi.getPlayer().message("Cannot leave until all Blue Mushrooms have been defeated.");
		return false;
	}
	else{
		var eim = pi.getEventInstance();
		eim.stopEventTimer();
		eim.dispose();
	
		pi.playPortalSound();
		pi.warp(101000000, 26);

		if(pi.isQuestCompleted(20718))
			pi.openNpc(1103003, "MaybeItsGrendel_end");
		
		return true;
	}
}