var windowTime = 10 * 1000;

function enter(pi) {
    var em = pi.getEventManager("PiratePQ");
    var level = em.getProperty("level");
    if(em.getProperty("stage3b") == "0") {
        pi.getMap(925100302).spawnAllMonstersFromMapSpawnList(level, true);
        em.setProperty("stage3b", "1");
    }
    
    pi.warp(925100302,0);
    return(true);
}