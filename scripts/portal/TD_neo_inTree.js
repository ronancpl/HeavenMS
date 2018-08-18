function enter(pi) {
    var nex = pi.getEventManager("GuardianNex");
    if(nex == null) {
        pi.message("Guardian Nex challenge encountered an error and is unavailable.");
        return false;
    }
    
    var quests = [3719, 3724, 3730, 3736, 3742, 3748];
    var mobs = [7120100, 7120101, 7120102, 8120100, 8120101, 8140510];
    
    for(var i = 0; i < quests.length; i++) {
        if (pi.isQuestActive(quests[i])) {
            if(pi.getQuestProgress(quests[i], mobs[i]) != 0) {
                pi.message("You already faced Nex. Complete your mission.");
                return false;
            }
            
            if(!nex.startInstance(i, pi.getPlayer())) {
                pi.message("Someone is already challenging Nex. Wait for them to finish before you enter.");
                return false;
            } else {
                pi.playPortalSound();
                return true;
            }
        }
    }
    
    pi.message("A mysterious force won't let you in.");
    return false;
}