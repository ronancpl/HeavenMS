function enter(pi) {
    if (!pi.haveItem(4031890)) {
        pi.getPlayer().dropMessage(6, "You need a warp card to activate this portal.");
        return false;
    }
    
    pi.playPortalSound(); pi.warp(120000101,"earth01");
    return true;
}