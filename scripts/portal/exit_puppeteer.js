function enter(pi) {
	if(pi.getMap().countMonster(9300285) > 0){
		pi.getPlayer().message("Defeat the Puppeteer before leaving.");
		return false;
	}
	else{
		var eim = pi.getEventInstance();
		if(eim != null){
			eim.stopEventTimer();
			eim.dispose();
		}

		pi.playPortalSound();
		pi.warp(105070300, 3);
		return true;
	}
}