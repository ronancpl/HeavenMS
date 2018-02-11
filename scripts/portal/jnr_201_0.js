function enter(pi) {
    if (pi.getMap().getReactorByName("jnr31_out").getState() == 1) {
	pi.playPortalSound(); pi.warp(926110200, 1);
        return true;
    } else {
	pi.playerMessage(5, "The door is not opened yet.");
        return false;
    }
}