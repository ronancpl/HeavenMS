importPackage(Packages.server.life);

function start(ms) {
        var pos = new java.awt.Point(171, 50);
	var mobId = 9400611;
        var mobName = "Crocell";
        
	var player = ms.getPlayer();
	var map = player.getMap();

	if(map.getMonsterById(mobId) != null){
		return;   	       
	}

	map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), pos);
	player.message(mobName + " has appeared!");
}