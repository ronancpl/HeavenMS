function start(pi) {
	var map = pi.getClient().getChannelServer().getMapFactory().getMap(922000000);
	map.resetReactors();
	map.shuffleReactors();

	return(true);
}