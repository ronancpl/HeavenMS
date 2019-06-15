function enter(pi) {
    if (pi.isQuestCompleted(2324)) {
        pi.playPortalSound(); pi.warp(106020501,0);
        return true;
    } else {
        pi.playerMessage(5, "The path ahead is covered with sprawling vine thorns, only a Thorn Remover to clear this out...");
        return false;
    }
}