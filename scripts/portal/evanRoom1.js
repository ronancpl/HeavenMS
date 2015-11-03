//Author: kevintjuh93

function enter(pi) {  
	pi.blockPortal();
	if (pi.containsAreaInfo(22013, "hand=o")) {
	    return false;
	}
	pi.updateAreaInfo(22013, "dt00=o;dt01=o;mo00=o;mo01=o;mo10=o;mo02=o;mo20=o;hand=o;mo21=o");
	pi.sendImage("UI/tutorial/evan/0/0");
	pi.showIntro("Effect/OnUserEff.img/guideEffect/evanTutorial/evanBalloon70");
	return true;
}  