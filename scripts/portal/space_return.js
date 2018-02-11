//Author: kevintjuh93

function enter(pi) {  
	pi.playPortalSound(); pi.warp(pi.getPlayer().getSavedLocation("EVENT"));
	return true;
}