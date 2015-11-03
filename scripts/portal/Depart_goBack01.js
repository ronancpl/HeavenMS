function enter(pi) {
    pi.playPortalSound();
    pi.warp(pi.getPlayer().getMap().getId() -10,"left01");
    return true;
}