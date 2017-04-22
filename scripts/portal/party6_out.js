function enter(pi) {
	if ((pi.getMap().getMonsters().size() == 0 || pi.getMap().getMonsterById(9300183) != null) && (pi.getMap().getReactorByName("") == null || pi.getMap().getReactorByName("").getState() == 1)) {
		pi.warp(930000800,0);
                return true;
	} else {
		pi.playerMessage(5, "Please eliminate the Poison Golem.");
                return false;
	}
}