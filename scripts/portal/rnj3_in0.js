function enter(pi) {
    if (pi.getMap().getReactorByName("rnj3_out1").getState() == 1) {
	pi.playPortalSound(); pi.warp(926100201, 0);
        return true;
    } else {
	pi.playerMessage(5, "The door is not opened yet.");
        return false;
    }
}