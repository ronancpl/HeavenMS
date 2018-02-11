function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        pi.playPortalSound(); pi.warp(610030300, 0);
        
	if (eim.getIntProperty("glpq3") < 5 || eim.getIntProperty("glpq3_p") < 5) {
            if(eim.getIntProperty("glpq3_p") == 5) {
                pi.mapMessage(6, "Not all Sigils have been activated yet. Make sure they have all been activated to proceed to the next stage.");
            } else {
                eim.setIntProperty("glpq3_p", eim.getIntProperty("glpq3_p") + 1);
                
                if(eim.getIntProperty("glpq3") == 5 && eim.getIntProperty("glpq3_p") == 5) {
                    pi.mapMessage(6, "The Antellion grants you access to the next portal! Proceed!");
                    
                    eim.showClearEffect(610030300, "3pt", 2);
                    eim.giveEventPlayersStageReward(3);
                } else {
                    pi.mapMessage(6, "An adventurer has passed through! " + (5 - eim.getIntProperty("glpq3_p")) + " to go.");
                }
            }
	}
        else {
            pi.getPlayer().dropMessage(6, "The portal at the bottom has already been opened! Proceed there!");
        }
        
        return true;
    }
    
    return false;
}