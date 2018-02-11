function passedGrindMode(map, eim) {
    if(eim.getIntProperty("grindMode") == 0) return true;
    return eim.activatedAllReactorsOnMap(map, 2511000, 2517999);
}

function enter(pi) {
    if (pi.getMap().getMonsters().size() == 0 && passedGrindMode(pi.getMap(), pi.getEventInstance())) {
	pi.playPortalSound(); pi.warp(925100400,0); //next
        return true;
    } else {
	pi.playerMessage(5, "The portal is not opened yet.");
        return false;
    }
}