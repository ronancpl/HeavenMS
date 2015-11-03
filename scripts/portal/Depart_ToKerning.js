function enter(pi) {
	pi.playPortalSound();
	var train = pi.getEventManager("KerningTrain");
	train.newInstance("KerningTrain");
	train.setProperty("player", pi.getPlayer().getName());
	train.startInstance(pi.getPlayer());
	return true;
}