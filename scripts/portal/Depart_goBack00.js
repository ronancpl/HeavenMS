function enter(pi) {
    pi.playPortalSound();
    pi.warp(pi.getPlayer().getMap().getId() - 10,"left00");
}