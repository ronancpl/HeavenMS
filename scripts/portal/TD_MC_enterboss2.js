function enter(pi) {
    if(pi.isQuestCompleted(2331)) {
        pi.openNpc(1300013);
        return false;
    }
    
    if(pi.isQuestCompleted(2333) && pi.isQuestStarted(2331) && !pi.hasItem(4001318)){
        pi.getPlayer().message("Lost the Royal Seal, eh? Worry not! Kevin's code here to save your hide.");
        if(pi.canHold(4001318)){
            pi.gainItem(4001318, 1);
        }
        else{
            pi.getPlayer().message("Hey, how do you plan to hold this Seal when your inventory is full?");
        }
    }

    if(pi.isQuestCompleted(2333)){
        pi.playPortalSound();
        pi.warp(106021600, 1);
        return true;
    }
    else if(pi.isQuestStarted(2332) && pi.hasItem(4032388)){
        pi.forceCompleteQuest(2332, 1300002);
        pi.getPlayer().message("You've found the princess!");
        pi.giveCharacterExp(4400, pi.getPlayer());
        
        var em = pi.getEventManager("MK_PrimeMinister");
        var party = pi.getPlayer().getParty();
        if (party != null) {
            var eli = em.getEligibleParty(pi.getParty());   // thanks Conrad for pointing out missing eligible party declaration here
            if(eli.size() > 0) {
                if (em.startInstance(party, pi.getMap(), 1)) {
                    pi.playPortalSound();
                    return true;
                } else {
                    pi.message("Another party is already challenging the boss in this channel.");
                    return false;
                }
            }
        } else {
            if (em.startInstance(pi.getPlayer())) { // thanks RedHat for noticing an issue here
                pi.playPortalSound();
                return true;
            } else {
                pi.message("Another party is already challenging the boss in this channel.");
                return false;
            }
        }
    }
    else if(pi.isQuestStarted(2333) || (pi.isQuestCompleted(2332) && !pi.isQuestStarted(2333))){
        var em = pi.getEventManager("MK_PrimeMinister");
        
        var party = pi.getPlayer().getParty();
        if (party != null) {
            var eli = em.getEligibleParty(pi.getParty());
            if(eli.size() > 0) {
                if (em.startInstance(party, pi.getMap(), 1)) {
                    pi.playPortalSound();
                    return true;
                } else {
                    pi.message("Another party is already challenging the boss in this channel.");
                    return false;
                }
            }
        } else {
            if (em.startInstance(pi.getPlayer())) {
                pi.playPortalSound();
                return true;
            } else {
                pi.message("Another party is already challenging the boss in this channel.");
                return false;
            }
        }
    }
    else{
        pi.getPlayer().message("The door seems to be locked. Perhaps I can find a key to open it...");
        return false;
    }
}