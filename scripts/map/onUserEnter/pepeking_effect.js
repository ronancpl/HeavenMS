importPackage(Packages.server.life);

function start(ms){
	var mobId = 3300000 + (Math.floor(Math.random() * 3) + 5);
	var player = ms.getPlayer();
	var map = player.getMap();

	map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(-28, -67));
}