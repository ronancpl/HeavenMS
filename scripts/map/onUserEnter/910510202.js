importPackage(Packages.server.life);

function start(ms){
	var mobId = 9300344;
	var player = ms.getPlayer();
	var map = player.getMap();

        if(map.countMonster(mobId) == 0) {
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(680, 258));
        }
}