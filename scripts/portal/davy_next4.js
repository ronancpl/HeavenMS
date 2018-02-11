importPackage(Packages.server.life);

function enter(pi) {
    if (pi.getMap().getReactorByName("sMob1").getState() >= 1 && pi.getMap().getReactorByName("sMob2").getState() >= 1 && pi.getMap().getReactorByName("sMob3").getState() >= 1 && pi.getMap().getReactorByName("sMob4").getState() >= 1 && pi.getMap().getMonsters().size() == 0) {
	var eim = pi.getEventInstance();
        
        if(eim.getProperty("spawnedBoss") == null) {
            var level = parseInt(eim.getProperty("level"));
            var chests = parseInt(eim.getProperty("openedChests"));
            var boss;                                               

            if(chests == 0) boss = MapleLifeFactory.getMonster(9300119);        //lord pirate
            else if(chests == 1) boss = MapleLifeFactory.getMonster(9300105);   //angry lord pirate
            else boss = MapleLifeFactory.getMonster(9300106);                   //enraged lord pirate

            boss.changeDifficulty(level, true);

            pi.getMap(925100500).spawnMonsterOnGroundBelow(boss, new java.awt.Point(777, 140));
            eim.setProperty("spawnedBoss", "true");
        }
        
        pi.playPortalSound(); pi.warp(925100500, 0);
        return true;
    } else {
	pi.playerMessage(5, "The portal is not opened yet.");
        return false;
    }
}