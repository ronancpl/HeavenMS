function enter(pi) {
        var map = pi.getPlayer().getSavedLocation("BOSSPQ");
        if (map == -1)
                map = 100000000;
    
	pi.warp(map,0);
        return true;
}