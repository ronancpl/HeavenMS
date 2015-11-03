//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22014, "mo30=o")) {
	    return false;
	}
	pi.updateAreaInfo(22014, "mo30=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon30");
	return true;
}  