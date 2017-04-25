importPackage(Packages.server.life);

function start(ms) {   	       
	spawnMob(251, -841, 9400613, ms.getPlayer().getMap());
}

function spawnMob(x, y, id, map) {
	if(map.getMonsterById(id) != null)
		return;
		
	var mob = MapleLifeFactory.getMonster(id);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(x, y));
}