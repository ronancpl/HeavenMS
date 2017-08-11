function enter(pi) {
    if (pi.getMap().getReactorByName("rnj6_out").getState() == 1) {
	pi.warp(926100300);
        return(true);
    } else {
	pi.playerMessage(5, "The portal is not opened yet.");
        return(false);
    }
}