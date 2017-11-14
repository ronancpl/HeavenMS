importPackage(Packages.server.life);

function start(ms){
        var player = ms.getPlayer();
        var map = player.getMap();
        
        if(ms.isQuestStarted(21747) && ms.getQuestProgress(21747, 9300351) == 0) {
                map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300351), new java.awt.Point(897, 51));
        }
}