importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools);
importPackage(Packages.client.inventory);

function init() {
}


function monsterValue(eim, mobId) {
    return 1;
}


function setClassVars(player) {
    var returnMapId;
    var monsterId;
    var mapId;
    if (player.getJob().equals(MapleJob.FP_WIZARD) ||
        player.getJob().equals(MapleJob.IL_WIZARD) ||
        player.getJob().equals(MapleJob.CLERIC)) {
        mapId = 108010201;
        returnMapId = 100040106;
        monsterId = 9001001;
    } else if (player.getJob().equals(MapleJob.FIGHTER) ||
        player.getJob().equals(MapleJob.PAGE) ||
        player.getJob().equals(MapleJob.SPEARMAN)) {
        mapId = 108010301;
        returnMapId = 105070001;
        monsterId = 9001000;
    } else if (player.getJob().equals(MapleJob.ASSASSIN) ||
        player.getJob().equals(MapleJob.BANDIT)) {
        mapId = 108010401;
        returnMapId = 107000402;
        monsterId = 9001003;
    } else if (player.getJob().equals(MapleJob.HUNTER) ||
        player.getJob().equals(MapleJob.CROSSBOWMAN)) {
        mapId = 108010101;
        returnMapId = 105040305;
        monsterId = 9001002;
    } else if (player.getJob().equals(MapleJob.BRAWLER) ||
        player.getJob().equals(MapleJob.GUNSLINGER)) {
        mapId = 108010501;
        returnMapId = 105040305;
        monsterId = 9001008;
    }
    return new Array(mapId, returnMapId, monsterId);
}


function playerEntry(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    var map = eim.getMapInstance(mapId);
    map.toggleDrops();
    
    player.changeMap(map, map.getPortal(0));
    var mob = map.getMonsterById(monsterId);
    eim.registerMonster(mob);
	eim.schedule("warpOut", 20 * 60 * 1000);
	map.addMapTimer(20 * 60);
}


function playerDead(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    player.setHp(1);
    var returnMap = em.getChannelServer().getMapFactory().getMap(returnMapId);
    player.changeMap(returnMap, returnMap.getPortal(0));
    eim.unregisterPlayer(player);
    eim.dispose();
}


function playerDisconnected(eim, player) {
    var info = setClassVars(player);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    var returnMap = em.getChannelServer().getMapFactory().getMap(returnMapId);
    player.setMap(returnMap);
    eim.dispose();
}


function allMonstersDead(eim) {
    var winner = eim.getPlayers().get(0);
    var info = setClassVars(winner);
    var mapId = info[0];
    var returnMapId = info[1];
    var monsterId = info[2];
    var map = eim.getMapFactory().getMap(mapId);
    map.spawnItemDrop(winner, winner, new Item(4031059, 0, 1), winner.getPosition(), true, false);
    eim.schedule("warpOut", 12 * 60 * 1000);
	map.addMapTimer(12 * 60);
}

function cancelSchedule(eim) {
	
}


function warpOut(eim) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        var info = setClassVars(player);
        var mapId = info[0];
        var returnMapId = info[1];
        var monsterId = info[2];
        var returnMap = em.getChannelServer().getMapFactory().getMap(returnMapId);
        player.changeMap(returnMap, returnMap.getPortal(0));
        eim.unregisterPlayer(player);
    }
    eim.dispose();
}


function leftParty(eim, player) {
    
}


function disbandParty(eim, player) {


}


function dispose() {


}

function cancelSchedule(eim) {
}