function enter(pi) {
    if(pi.getMap().countMonsters() == 0) {
        if(pi.canHold(4001193, 1)) {
            pi.gainItem(4001193, 1);
            pi.playPortalSound(); pi.warp(140010210, 0);
            return true;
        } else {
            pi.playerMessage(5, "Free a slot on your inventory before receiving the couse clear's token.");
            return false;
        }
    } else {
        pi.playerMessage(5, "Defeat all wolves before exiting the stage.");
        return false;
    }
}