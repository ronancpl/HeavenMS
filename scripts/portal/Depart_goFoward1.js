function enter(pi) { 
	if (pi.getPlayer().getMap().getId() == 103040410 && pi.isQuestCompleted(2287)) {
    	pi.playPortalSound();
    	pi.warp(103040420, "right01");
    	return true;
	} else if (pi.getPlayer().getMap().getId() == 103040420 && pi.isQuestCompleted(2288)) {
    	pi.playPortalSound();
    	pi.warp(103040430, "right01");
    	return true;
	} else if (pi.getPlayer().getMap().getId() == 103040410 && pi.isQuestStarted(2287)) {
    	pi.playPortalSound();
    	pi.warp(103040420, "right01");
    	return true;
    } else if (pi.getPlayer().getMap().getId() == 103040420 && pi.isQuestStarted(2288)) {
    	pi.playPortalSound();
    	pi.warp(103040430, "right01");
    	return true; 	
    } else {
    	if (pi.getPlayer().getMap().getId() == 103040440 || pi.getPlayer().getMap().getId() == 103040450) {
        	pi.playPortalSound();
	    	pi.warp(pi.getPlayer().getMap().getId() + 10, "right01");
	    	return true;
    	}
    	pi.getPlayer().dropMessage(5, "You cannot access this area.");
    	return false;
    }
}