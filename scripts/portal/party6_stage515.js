function enter(pi) {
    if (Math.random() < 0.1) {
	pi.playPortalSound(); pi.warp(930000300,"16st");
    } else {
	pi.playPortalSound(); pi.warp(930000300, "01st");
    }
    
    return true;
}