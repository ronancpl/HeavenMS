
function act() {
	var em = rm.getEventManager("HenesysPQ").getInstance("HenesysPQ_" + rm.getParty().getLeader().getName());
	if (em != null) {
		var react = rm.getReactor().getMap().getReactorByName("fullmoon");
		var stage = parseInt(em.getProperty("stage")) + 1;
		var newStage = stage.toString();
		em.setProperty("stage", newStage);
		react.forceHitReactor(react.getState() + 1);
		if (em.getProperty("stage").equals("6")) {
			rm.mapMessage(6, "Protect the Moon Bunny!!!");
			var map = em.getMapInstance(rm.getReactor().getMap().getId());
			map.allowSummonState(true);
			map.spawnMonsterOnGroudBelow(9300061, -183, -433);
		}
	}
}