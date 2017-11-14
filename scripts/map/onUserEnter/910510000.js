importPackage(Packages.server.life);

function start(ms){
        var player = ms.getPlayer();
        var map = player.getMap();
        
	if(player.isCygnus()) {
                if(ms.isQuestStarted(20730) && ms.getQuestProgress(20730, 9300285) == 0) {
                        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300285), new java.awt.Point(680, 258));
                }
        } else {
                if(ms.isQuestStarted(21731) && ms.getQuestProgress(21731, 9300344) == 0) {
                        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300344), new java.awt.Point(680, 258));
                }
        }
}