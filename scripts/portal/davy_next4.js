importPackage(Packages.server.life);

function enter(pi) {
    if (pi.getMap().getReactorByName("sMob1").getState() >= 1 && pi.getMap().getReactorByName("sMob2").getState() >= 1 && pi.getMap().getReactorByName("sMob3").getState() >= 1 && pi.getMap().getReactorByName("sMob4").getState() >= 1) {
	if (pi.isLeader()) {
            var em = pi.getEventManager("PiratePQ");
            
            var level = parseInt(em.getProperty("level"));
            var chests = parseInt(em.getProperty("openedChests"));
            var boss;                                               
            
            if(chests == 0) boss = MapleLifeFactory.getMonster(9300119);        //lord pirate
            else if(chests == 1) boss = MapleLifeFactory.getMonster(9300105);   //angry lord pirate
            else boss = MapleLifeFactory.getMonster(9300106);                   //enraged lord pirate
            
            boss.changeDifficulty(level, true);
            
            pi.getMap(925100500).spawnMonster(boss);
	    pi.warpParty(925100500); //next
            return(true);
	} else {
	    pi.playerMessage(5, "The leader must be here");
            return(false);
	}
    } else {
	pi.playerMessage(5, "The portal is not opened yet.");
        return(false);
    }
}