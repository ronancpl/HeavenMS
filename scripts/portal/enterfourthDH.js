function enter(pi) {
    if (pi.hasItem(4032125) || pi.hasItem(4032126) || pi.hasItem(4032127) || pi.hasItem(4032128) || pi.hasItem(4032129)) {
        pi.playerMessage(5, "You already have the proof of ability.");
        return false;
    }
    
    if (pi.isQuestStarted(20611) || pi.isQuestStarted(20612) || pi.isQuestStarted(20613) || pi.isQuestStarted(20614) || pi.isQuestStarted(20615)) {
        if (pi.getPlayerCount(913020300) == 0) {
            var map = pi.getMap(913020300);
            map.killAllMonsters();
            
            pi.playPortalSound();
            pi.warp(913020300, 0);
            pi.spawnMonster(9300294, 87,88);
            return true;
        } else {
            pi.playerMessage(5, "Someone is already attempting to defeat the boss. Better come back later.");
            return false;
        }
    } else {
        pi.playerMessage(5, "You cannot access this hall.");
        return false;
    }
}