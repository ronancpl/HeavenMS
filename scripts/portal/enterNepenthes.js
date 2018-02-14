function enter(pi) {
    if (pi.isQuestActive(21739)) {
        var mapobj1 = pi.getWarpMap(920030000);
        var mapobj2 = pi.getWarpMap(920030001);
        
        if(mapobj1.countPlayers() == 0 && mapobj2.countPlayers() == 0) {
            mapobj1.resetPQ(1);
            mapobj2.resetPQ(1);
            
            mapobj2.spawnMonsterOnGroundBelow(Packages.server.life.MapleLifeFactory.getMonster(9300348), new Packages.java.awt.Point(591, -34));
            
            pi.playPortalSound(); pi.warp(920030000,2);
            return true;
        } else {
            pi.message("Someone is already challenging the area.");
            return false;
        }
    } else {
	pi.playPortalSound(); pi.warp(200060001,2);
        return true;
    }
}