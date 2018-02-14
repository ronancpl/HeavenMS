//Author: kevintjuh93

function enter(pi) {  
	if (pi.getPlayer().getEvents().getGagaRescue().fallAndGet() > 3) {
	    pi.playPortalSound(); pi.warp(922240200, 0);
	    pi.getPlayer().cancelEffect(2360002);
	} else
	    pi.playPortalSound(); pi.warp(pi.getPlayer().getMapId(), 0);

	return true;
}