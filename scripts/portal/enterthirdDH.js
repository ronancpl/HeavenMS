function enter(pi) {
	if (pi.hasItem(4032120) || pi.hasItem(4032121) || pi.hasItem(4032122) || pi.hasItem(4032123) || pi.hasItem(4032124)) {
		 pi.playerMessage(5, "You already have the proof of qualification.");
		 return false;
	}
    if (pi.isQuestStarted(20601) || pi.isQuestStarted(20602) || pi.isQuestStarted(20603) || pi.isQuestStarted(20604) || pi.isQuestStarted(20605)) {    	
		if (pi.getPlayerCount(913010200) == 0) {
		    var map = pi.getMap(913010200);
		    map.killAllMonsters();  
		    pi.warp(913010200, 0);
		    pi.spawnMonster(9300289, 0, 0);
		} else {
		    pi.playerMessage(5, "Someone is already attempting to defeat the boss. Better come back later.");
		}
    } else {
    	pi.playerMessage(5, "The only way to enter the hall #3 is if you're training for the Level 100 skills.");
    }
}