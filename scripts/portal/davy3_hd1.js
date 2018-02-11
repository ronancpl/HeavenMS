var windowTime = 10 * 1000;

function enter(pi) {
    var eim = pi.getEventInstance();
    var level = eim.getProperty("level");
    if(eim.getProperty("stage3b") == "0") {
        pi.getMap(925100302).spawnAllMonstersFromMapSpawnList(level, true);
        eim.setProperty("stage3b", "1");
    }
    
    pi.playPortalSound(); pi.warp(925100302,0);
    return true;
}