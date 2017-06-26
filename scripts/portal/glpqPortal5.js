function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
	if (eim.getIntProperty("glpq5") < 5){
	    pi.playerMessage(5, "The portal is not opened yet.");
            return false;
	} else {
            pi.removeAll(4001256);
            pi.removeAll(4001257);
            pi.removeAll(4001258);
            pi.removeAll(4001259);
            pi.removeAll(4001260);
	    pi.warp(610030600, 0);
            return true;
	}
    }
    
    return false;
}