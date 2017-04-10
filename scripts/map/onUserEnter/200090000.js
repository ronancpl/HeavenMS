// Author: Ronan
importPackage(Packages.tools);

var mapId = 200090000;

function start(pi) {
	var map = pi.getClient().getChannelServer().getMapFactory().getMap(mapId);

	if(map.getDocked()) {
		pi.getClient().announce(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
		pi.getClient().announce(MaplePacketCreator.crogBoatPacket(true));
	}

	return(true);
}