//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22014, "mo41=o")) {
	    return false;
	}
	pi.updateAreaInfo(22014, "mo30=o;mo40=o;mo41=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon41");
	return true;
}  