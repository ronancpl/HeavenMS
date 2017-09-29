importPackage(Packages.server.life);

function start(ms){

	if(ms.getQuestStatus(2175) == 1){
		var mobId = 9300156;
		var player = ms.getPlayer();
		var map = player.getMap();

		if(map.getMonsterById(mobId) != null){
			return;   	       
		}

		map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(-1027, 216));
	}
}