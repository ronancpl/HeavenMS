function enter(pi) {
    if (pi.getMap().getReactorByName("jnr32_out").getState() == 1) {
	pi.playPortalSound(); pi.warp(926110200, 2);
        return true;
    } else {
	pi.playerMessage(5, "The door is not opened yet.");
        return false;
    }
}