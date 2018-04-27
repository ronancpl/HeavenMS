function enter(pi) {
    if(pi.isQuestStarted(2224) || pi.isQuestStarted(2226) || pi.isQuestCompleted(2227)) {
        var hourDay = pi.getHourOfDay();
        if(!((hourDay >= 0 && hourDay < 7) || hourDay >= 17)) {
            pi.getPlayer().dropMessage(5, "You cannot access this area right now.");
            return false;
        } else {
            pi.playPortalSound(); pi.warp(pi.isQuestCompleted(2227) ? 910100001 : 910100000,"out00");
            return true;
        }
    }
    
    pi.getPlayer().dropMessage(5, "You cannot access this area.");
    return false;
}