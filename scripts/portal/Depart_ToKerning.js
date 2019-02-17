function enter(pi) {
        var em = pi.getEventManager("KerningTrain");
        if (!em.startInstance(pi.getPlayer())) {
            pi.message("The passenger wagon is already full. Try again a bit later.");
            return false;
        }
        
	pi.playPortalSound();
	return true;
}