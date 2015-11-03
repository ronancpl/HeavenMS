var mapz = Array(100, 200, 300, 400, 500, 510, 520, 521, 522, 530, 540, 550, 600, 700, 800);
var a = Array("a", "b", "c", "d", "e", "f", "g", "h", "i");
/*
a1,3,6
b1-7
c1,3,6
d1-7
e1-7
f1,3,6
g1-7
h1,3,6
i1-7
*/

var pos_x = Array(944,401,28,-332,-855);
var pos_y = Array(-204,-384,-504,-384,-204);
var pos_y2 = Array(-144, -444, -744, -1044, -1344, -1644);

function init() {
em.setProperty("state", "0");
	em.setProperty("leader", "true");
}

function setup(eim, leaderid) {
em.setProperty("state", "1");
	em.setProperty("leader", "true");
	em.setProperty("current_instance", "0");
	em.setProperty("glpq1", "0");
	em.setProperty("glpq2", "0");
	em.setProperty("glpq3", "0");
	em.setProperty("glpq4", "0");
	em.setProperty("glpq5", "0");
	em.setProperty("glpq6", "0");
    var eim = em.newInstance("CWKPQ" + leaderid);
	for (var i = 0; i < mapz.length; i++) {
		var map = eim.setInstanceMap(610030000 + mapz[i]);
		if (map != null) {
			map.resetFully();
			if (map.getId() == 610030400) {
				map.setReactorState(); //because everything is at 0 =[
				map.limitReactor(6109016, 1);
				map.limitReactor(6109017, 1);
				map.limitReactor(6109018, 1);
				map.limitReactor(6109019, 1);
				map.limitReactor(6109020, 1);
				map.shuffleReactors(6109016, 6109020);
				map.destroyReactors(6108000, 6108005); //destroy the fake ones, non-GMS like or is this necessary

				//add environments
				for (var x = 0; x < a.length; x++) {
					for (var y = 1; y <= 7; y++) {
						if (x == 1 || x == 3 || x == 4 || x == 6 || x == 8) {
							if (y != 2 && y != 4 && y != 5 && y != 7) {
								map.moveEnvironment(a[x] + "" + y, 1);
							}
						} else {
							map.moveEnvironment(a[x] + "" + y, 1);
						}
					}
				}
			} else if (map.getId() == 610030510) { //warrior room, crimson guardians
				for (var z = 0; z < pos_y2.length; z++) {
					var mob = em.getMonster(9400582);
					eim.registerMonster(mob);
					map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, pos_y2[z]));
				}
			//skipping mage room, ehh
			} else if (map.getId() == 610030540) { //bowman room, spawn master guardians
				for (var z = 0; z < pos_x.length; z++) {
					var mob = em.getMonster(9400594);
					eim.registerMonster(mob);
					map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(pos_x[z], pos_y[z]));
				}
			} else if (map.getId() == 610030550) {
				map.shuffleReactors(); //pirate room
			}
		}
	}
    eim.startEventTimer(120000); //2 MIN for first stg
    eim.schedule("spawnGuardians", 60000);
    return eim;
}

function playerEntry(eim, player) {
    eim.broadcastPlayerMsg(5, "[Expedition] " + player.getName() + " has entered the map.");
    var map = eim.getMapInstance(610030100 + (parseInt(em.getProperty("current_instance")) * 100));
    player.changeMap(map, map.getPortal(0));
}

function spawnGuardians(eim) {
    var map = eim.getMapInstance(0);
    if (map.getCharactersSize() <= 0) {
	return;
    }
    eim.broadcastPlayerMsg(5, "The Master Guardians have detected you.");
    for (var i = 0; i < 20; i++) { //spawn 20 guardians
	var mob = em.getMonster(9400594);
	eim.registerMonster(mob);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(1000, 336));
    }
}

function playerRevive(eim, player) {
}

function scheduledTimeout(eim) {
    end(eim);
}

function changedMap(eim, player, mapid) {
    if (mapid < 610030100 || mapid > 610030800) {
	playerExit(eim,player);
    } else {
	switch(mapid) {
	    case 610030200:
		if (em.getProperty("current_instance").equals("0")) {
		    eim.restartEventTimer(600000); //10 mins
		    em.setProperty("current_instance", "1");
		}
		break;
	    case 610030300:
		if (em.getProperty("current_instance").equals("1")) {
		    eim.restartEventTimer(600000); //10 mins
		    em.setProperty("current_instance", "2");
		}
		break;
	    case 610030400:
		if (em.getProperty("current_instance").equals("2")) {
		    eim.restartEventTimer(600000); //10 mins
		    em.setProperty("current_instance", "3");
		}
		break;
	    case 610030500:
		if (em.getProperty("current_instance").equals("3")) {
		    eim.restartEventTimer(1200000); //20 mins
		    em.setProperty("current_instance", "4");
		}
		break;
	    case 610030600:
		if (em.getProperty("current_instance").equals("4")) {
		    eim.restartEventTimer(3600000); //1 hr
		    em.setProperty("current_instance", "5");
		}
		break;
	    case 610030800:
		if (em.getProperty("current_instance").equals("5")) {
		    eim.restartEventTimer(60000); //1 min
		    em.setProperty("current_instance", "6");
		}
		break;
        }
    }
}

function playerDisconnected(eim, player) {
    return 0;
}

function monsterValue(eim, mobId) {
    return 1;
}

function playerExit(eim, player) {
    eim.broadcastPlayerMsg(5, "[Expedition] " + player.getName() + " has left the map.");
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
	em.setProperty("state", "0");
		em.setProperty("leader", "true");
	}
}

function end(eim) {
    eim.disposeIfPlayerBelow(100, 610030010);
	em.setProperty("state", "0");
		em.setProperty("leader", "true");
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
}

function leftParty (eim, player) {
}
function disbandParty (eim) {
}
function playerDead(eim, player) {}
function cancelSchedule() {}