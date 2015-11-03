
function enter(pi) {
    if (pi.isQuestStarted(21701)) {
		pi.playPortalSound();
		pi.warp(914010000, 1);
    } else if (pi.isQuestStarted(21702)) {
		pi.playPortalSound();
		pi.warp(914010100, 1);
    } else if (pi.isQuestStarted(21703)) {
    	pi.playPortalSound();
    	pi.warp(914010200, 1);
    } else {
    	pi.playerMessage(5, "Only if you are recieving a lesson from Puo, you will be allowed to enter the Pengiun Training Ground.");
    }
}