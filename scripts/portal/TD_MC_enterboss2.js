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
        if(pi.getPlayer().getParty() != null){
            pi.getPlayer().showHint("The next part of the quest is solo only! Must leave party.");
            return false;
        }
        else{
            pi.forceCompleteQuest(2332, 1300002);
            pi.getPlayer().message("You've found the princess!");
            pi.giveCharacterExp(4400 * 1.5, pi.getPlayer());
            var pm = pi.getEventManager("MK_PrimeMinister");
            pm.setProperty("player", pi.getPlayer().getName());
            
            pm.startInstance(pi.getPlayer());
            pi.playPortalSound();
            return true;
        }
    }
    else if(pi.isQuestStarted(2333) || (pi.isQuestCompleted(2332) && !pi.isQuestStarted(2333))){
        if(pi.getPlayer().getParty() != null){
            pi.getPlayer().showHint("The next part of the quest is solo only! Must leave party.");
            return false;
        }
        else{
            var pm = pi.getEventManager("MK_PrimeMinister");
            pm.setProperty("player", pi.getPlayer().getName());
            
            pm.startInstance(pi.getPlayer());
            pi.playPortalSound();
            return true;
        }
    }
    else{
        pi.getPlayer().message("The door seems to be locked. Perhaps I can find a key to open it...");
        return false;
    }
}