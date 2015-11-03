function start(ms) {
	var py = ms.getPyramid();
	if (py != null) {
	    py.sendScore(ms.getPlayer());
	}
}