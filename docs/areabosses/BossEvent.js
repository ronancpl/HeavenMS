// @Author: Resinate

var towns = new Array(800020120, 251010102, 260010201, 107000300, 200010300, 100040105, 100040106, 261030000, 110040000, 250010504, 240040401, 104000400, 222010310, 230040420, 230040420, 230020100, 105090310, 101030404, 250010304, 220050100, 220050000, 220050200, 221040301);
var spawns = new Array(6090002, 5220004, 3220001, 6220000, 8220000, 5220002, 5220002, 8220002, 5220001, 7220002, 8220003, 2220000, 7220001, 8510000, 8520000, 4220001, 8220008, 3220000, 7220000, 5220003, 5220003, 5220003, 6220001);
var x = new Array(560, 560, 645, 90, 208, 456, 474, -300, 200, 400, 0, 400, 0, 527, 138, 0, -626, 800, -300, -300, 0, 0, -4224);
var y = new Array(50, 50, 275, 119, 83, 278, 278, 180, 140, 540, 1125, 455, 33, -437, 138, 520, -604, 1280, 390, 1030, 1030, 1030, 776);
var mapObj;
var mobObj;

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);
}

function cancelSchedule() {
    if (setupTask != null)
        setupTask.cancel(true);
}

function start() {
	var time = (Math.floor(Math.random() * 10) + 10) * (60 * 1000);
	for(var i = 0; i < towns.length; i++) {
		mapObj = em.getChannelServer().getMapFactory().getMap(towns[i]);
		mobObj = Packages.server.life.MapleLifeFactory.getMonster(spawns[i]);
		if(mapObj.getMonsterById(spawns[i]) == null) {
			mapObj.spawnMonsterOnGroundBelow(mobObj, new Packages.java.awt.Point(x[i],y[i]));
		}
	}
	em.schedule("start", time);
}