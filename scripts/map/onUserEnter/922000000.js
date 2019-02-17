function start(ms) {
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(922000000);
        map.clearDrops();
	map.resetReactors();
	map.shuffleReactors();

	return(true);
}