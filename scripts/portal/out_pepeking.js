function enter(pi) {
	var eim = pi.getEventInstance();
        if(eim != null) {
                eim.stopEventTimer();
                eim.dispose();
        }
	
	var questProgress = pi.getQuestProgress(2330, 3300005) + pi.getQuestProgress(2330, 3300006) + pi.getQuestProgress(2330, 3300007); //3 Yetis
 	if(questProgress == 3 && !pi.hasItem(4032388)) {
 		if(pi.canHold(4032388)){
 			pi.getPlayer().message("You have aquired a key to the Wedding Hall. King Pepe must have dropped it.");
 			pi.gainItem(4032388, 1);
 
 			pi.playPortalSound();
                        pi.warp(106021400, 2);
                        return true;
 		} else {
 			pi.getPlayer().message("Please make room in your ETC inventory.");
 			return false;
 		}
 	} else {
 		pi.playPortalSound();
                pi.warp(106021400, 2);
                return true;
 	}
}