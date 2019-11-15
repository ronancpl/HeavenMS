//Author: kevintjuh93

function enter(pi) {  
	pi.playPortalSound(); pi.warp(922240100 + (pi.getPlayer().getMapId() - 922240000), 0);
	return true;
}  