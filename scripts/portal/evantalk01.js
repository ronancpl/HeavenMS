//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "mo01=o")) {
	    return false;
	}
	pi.updateAreaInfo(22013, "dt00=o;mo00=o;mo01=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon01");
	return true;
}  