//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "dt01=o")) {
	    return false;
	}
	pi.mapEffect("evan/dragonTalk01");
	pi.updateAreaInfo(22013, "dt00=o;dt01=o;mo00=o;mo01=o;mo10=o;mo02=o");
	return true;
}  