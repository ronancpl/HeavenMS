function enter(pi) {
    if ( pi.getPlayer().getTeam() == 0 ) {
	pi.warp( pi.getMapId() - 100);
    } else {
	pi.warp( pi.getMapId() - 100);
    }
    return true;
}