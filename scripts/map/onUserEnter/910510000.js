importPackage(Packages.server.life);

function start(ms){
	var mobId = 9300285;
	var player = ms.getPlayer();
	var map = player.getMap();

	if(ms.isQuestStarted(20730) && ms.getQuestProgress(20730, 9300285) == 0)
		map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(680, 258));
}