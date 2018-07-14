function enter(pi) {
        if(!(pi.isQuestStarted(3647) && pi.haveItem(4031793, 1))) {
            pi.playPortalSound(); pi.warp(222010200, "east00");
        } else {
            if(!pi.isQuestStarted(23647)) pi.forceStartQuest(23647);
            pi.playPortalSound(); pi.warp(922220000, "east00");
        }
        
	return true;
}
