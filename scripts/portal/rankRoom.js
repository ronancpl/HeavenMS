function enter(pi) {
    pi.playPortalSound();
    
    switch (pi.getPlayer().getMapId()) {
	case 130000000:
	    pi.warp(130000100, 5); //or 130000101
	    break;
	case 130000200:
	    pi.warp(130000100, 4); //or 130000101
	    break;
	case 140010100:
            pi.warp(140010110, 1); //or 140010111
            break;
	case 120000101:
            pi.warp(120000105, 1);
            break;
	case 103000003:
            pi.warp(103000008, 1); //or 103000009
            break;
	case 100000201:
            pi.warp(100000204, 2); //or 100000205
            break;
	default:
            pi.warp(pi.getMapId() + 1, 1); //or + 2
            break;
    }
	
    return true;
}