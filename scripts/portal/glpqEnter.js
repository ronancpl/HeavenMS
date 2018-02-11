function enter(pi) {
    if (pi.haveItem(3992041, 1)) {
	pi.playPortalSound(); pi.warp(610030020, "out00");
        return true;
    } else {
	pi.playerMessage(5, "The giant gate of iron will not budge no matter what, however there is a visible key-shaped socket.");
        return false;
    }
}