function enter(pi) {
    if(pi.isQuestActive(21747) || pi.isQuestActive(21744) && pi.isQuestCompleted(21745)) {
        pi.playPortalSound(); pi.warp(925040000,0);
        return true;
    }
    else {
        pi.message("You don't have permission to access this area.");
        return false;
    }
}