function enter(pi) {
    if (pi.getMap().getReactorByName("rnj3_out3").getState() == 1) {
	pi.playPortalSound(); pi.warp(926100203, 0); //next
        return true;
    } else {
	pi.playerMessage(5, "The door is not opened yet.");
        return false;
    }
}