function enter(pi) {
    if(pi.isQuestCompleted(2238)) {
        pi.playPortalSound(); pi.warp(105100101, "in00");
        return true;
    } else {
        pi.message("A mysterious force won't let you in.");
        return false;
    }
}