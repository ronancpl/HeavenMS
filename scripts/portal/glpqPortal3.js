function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
	if (eim.getIntProperty("glpq3") < 5 || eim.getIntProperty("glpq3_p") < 5) {
	    pi.playerMessage(5, "The portal is not opened yet.");
            return false;
	} else {
	    pi.playPortalSound(); pi.warp(610030400, 0);
            return true;
	}
    }
    
    return false;
}