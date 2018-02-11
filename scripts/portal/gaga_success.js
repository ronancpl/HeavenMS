//Author: kevintjuh93

function enter(pi) {  
	pi.getPlayer().getEvents().getGagaRescue().complete();
	pi.playPortalSound(); pi.warp(922240100 + (pi.getPlayer().getMapId() - 922240000));
	pi.getPlayer().cancelEffect(2360002);
	return true;
}  