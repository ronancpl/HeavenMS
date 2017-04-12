var windowTime = 10 * 1000;

function enter(pi) {
    var em = pi.getEventManager("PiratePQ");
    var level = em.getProperty("level");
    if(em.getProperty("stage2b") == "0") {
        pi.getMap(925100202).spawnAllMonstersFromMapSpawnList(level, true);
        em.setProperty("stage2b", "1");
    }
    
    pi.warp(925100202,0);
    return(true);
}