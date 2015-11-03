//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "dt00=o")) {
	    return false;
	}
	pi.mapEffect("evan/dragonTalk00");
	pi.updateAreaInfo(22013, "dt00=o;mo00=o");
	return true;
}  