function enter(pi) {
    if (pi.isQuestCompleted(20404)) {
        var warpMap;
        
        if (pi.isQuestCompleted(20407)) {
            warpMap = 924010200;
        } else if (pi.isQuestCompleted(20406)) {
            warpMap = 924010100;
        } else {
            warpMap = 924010000;
        }
        
        pi.playPortalSound();
	pi.warp(warpMap, 1);
        return true;
        
    
    } else {
	pi.playerMessage(5, "I shouldn't go here.. it's creepy!");
        return false;
    }
}