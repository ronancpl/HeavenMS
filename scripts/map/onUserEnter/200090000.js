// Author: Ronan
importPackage(Packages.tools);

var mapId = 200090000;

function start(ms) {
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

	if(map.getDocked()) {
		ms.getClient().announce(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
		ms.getClient().announce(MaplePacketCreator.crogBoatPacket(true));
	}

	return(true);
}