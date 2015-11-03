function enter(pi) {
	if(pi.getPlayer().getMapId() == 130030001){
		if(pi.isQuestStarted(20010)){
			pi.warp(130030002);
		} else {
			pi.message("Please click on the NPC first to receive a quest");
		}
	} else if(pi.getPlayer().getMapId() == 130030002){
		pi.warp(130030003);
	} else if(pi.getPlayer().getMapId() == 130030003){
		pi.warp(130030004);
	} else if(pi.getPlayer().getMapId() == 130030004){
		pi.warp(130030005);
	}
	return true;
}