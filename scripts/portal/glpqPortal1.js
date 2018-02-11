function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
	if (eim.getIntProperty("glpq2") == 5) {
	    pi.playPortalSound(); pi.warp(610030300, 0);
            return true;
	} else {
	    pi.playerMessage(5, "The portal has not been activated yet!");
            return false;
	}
    }
    
    return false;
}