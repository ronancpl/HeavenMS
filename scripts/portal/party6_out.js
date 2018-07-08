function enter(pi) {
        var eim = pi.getEventInstance();
    
        if (eim.isEventCleared()) {
                if(pi.isEventLeader()) {
                        pi.playPortalSound();
                        eim.warpEventTeam(930000800);
                        return true;
                } else {
                        pi.playerMessage(5, "Wait for the leader to pass through the portal.");
                        return false;
                }
        } else {
                pi.playerMessage(5, "Please eliminate the Poison Golem.");
                return false;
        }
}