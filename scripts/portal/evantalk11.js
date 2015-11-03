//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "mo11=o")) {
	    return false;
	}
	pi.updateAreaInfo(22013, "dt00=o;dt01=o;mo00=o;mo01=o;mo10=o;mo02=o;mo11=o");
	pi.showInfo("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon11");
	return true;
}  