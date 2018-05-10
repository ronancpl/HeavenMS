function enter(pi) {
	switch(pi.getMapId()) {
		case 930000000:
			pi.playPortalSound(); pi.warp(930000100,0);
                        return true;
			break;
		case 930000100:
			if (pi.getMap().getMonsters().size() == 0) {
				pi.playPortalSound(); pi.warp(930000200,0);
                                return true;
			} else {
				pi.playerMessage(5, "Eliminate all the monsters.");
                                return false;
			}
			break;
		case 930000200:
			if (pi.getMap().getReactorByName("spine") != null && pi.getMap().getReactorByName("spine").getState() < 4) {
				pi.playerMessage(5, "The spine blocks the way.");
                                return false;
			} else {
				pi.playPortalSound(); pi.warp(930000300,0); //assuming they cant get past reactor without it being gone
                                return true;
			}
			break;
                        
                default:
                        pi.playerMessage(5, "This portal leads to an unbound path.");
                        return false;
	}
}