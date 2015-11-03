//Author: kevintjuh93

function enter(pi) {  
	if (pi.getPlayer().getEvents().getGagaRescue().fallAndGet() > 3) {
	    pi.warp(922240200);
	    pi.getPlayer().cancelEffect(2360002);
	} else
	    pi.warp(pi.getPlayer().getMapId());

	return true;
}