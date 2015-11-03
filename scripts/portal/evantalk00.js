//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "mo00=o")) {
	    return false;
	}
	pi.updateAreaInfo(22013, "mo00=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon00");
	return true;
}  