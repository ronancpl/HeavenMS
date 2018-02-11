function enter(pi) {
	if (!pi.canHold(4001261,1)) {
		pi.playerMessage(5, "Please make 1 ETC room.");
		return false;
	}
	pi.gainItem(4001261,1);
	pi.playPortalSound(); pi.warp(105100100,0);
	return true;
}