function act() {
	var eim = rm.getEventInstance();
	if (eim != null) {
		eim.dropMessage(6, "A weapon has been restored to the Relic of Mastery!");
		eim.setIntProperty("glpq5", eim.getIntProperty("glpq5") + 1);
		if (eim.getIntProperty("glpq5") == 5) { //all 5 done
			eim.dropMessage(6, "The Antellion grants you access to the next portal! Proceed!");
                        
			eim.showClearEffect(610030500, "5pt", 2);
                        eim.giveEventPlayersStageReward(5);
		}
	}
}