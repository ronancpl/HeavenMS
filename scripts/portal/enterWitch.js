function enter(pi) {
    if (pi.getQuestStatus(20407) == 2) {
	pi.warp(924010200,0);
        return true;
    } else if (pi.getQuestStatus(20406) == 2) {
	pi.warp(924010100,0);
        return true;
    } else if (pi.getQuestStatus(20404) == 2) {
	pi.playPortalSound(); pi.warp(924010000,0);
        return true;
    } else {
	pi.playerMessage(5, "I shouldn't go here.. it's creepy!");
        return false;
    }
}