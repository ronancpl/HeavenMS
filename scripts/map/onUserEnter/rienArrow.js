function start(ms) {
	if (ms.containsAreaInfo(21019, "miss=o;helper=clear")) {
		ms.updateAreaInfo(21019, "miss=o;arr=o;helper=clear");
		ms.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
	}
}