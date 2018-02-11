function enter(pi) {
    if (pi.getPlayer().getMapId() != 910000000) {
        pi.getPlayer().saveLocation("FREE_MARKET");
        pi.playPortalSound(); pi.warp(910000000, "out00");
        return true;
    }
    return false;
}