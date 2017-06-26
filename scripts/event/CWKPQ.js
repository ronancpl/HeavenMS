var minPlayers = 1;

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

function afterSetup(eim) {}

function getNameFromList(index, array) {
    return array[index];
}

function generateMapReactors(map) {
    
    var jobReactors = [ [0, 0, -1, -1, 0],
                        [-1, 4, 3, 3, 3],
                        [1, 3, 4, 2, 2],
                        [2, -1, 0, 1, -1], 
                        [3, 2, 1, 0, -1],
                        [4, 1, -1, 4, 1],
                        [-1, 2, 4],
                        [-1, -1]
                      ];
                      
    var rndIndex;
    var jobFound;
    while(true) {
        jobFound = {};
        rndIndex = [];
        
        for(var i = 0; i < jobReactors.length; i++) {
            var jobReactorSlot = jobReactors[i];
            
            var idx = Math.floor(Math.random() * jobReactorSlot.length);
            jobFound["" + jobReactorSlot[idx]] = 1;
            rndIndex.push(idx);
        }
        
        if(Object.keys(jobFound).length == 6) break;
    }
    
    var toDeploy = [];
    
    toDeploy.push(getNameFromList(rndIndex[0], ["4skill0a", "4skill0b", "4fake1c", "4fake1d", "4skill0e"]));
    toDeploy.push(getNameFromList(rndIndex[1], ["4fake0a", "4skill4b", "4skill3c", "4skill3d", "4skill3e"]));
    toDeploy.push(getNameFromList(rndIndex[2], ["4skill1a", "4skill3b", "4skill4c", "4skill2d", "4skill2e"]));
    toDeploy.push(getNameFromList(rndIndex[3], ["4skill2a", "4fake1b", "4skill0c", "4skill1d", "4fake1e"]));
    toDeploy.push(getNameFromList(rndIndex[4], ["4skill3a", "4skill2b", "4skill1c", "4skill0d", "4fake0e"]));
    toDeploy.push(getNameFromList(rndIndex[5], ["4skill4a", "4skill1b", "4fake0c", "4skill4d", "4skill1e"]));
    toDeploy.push(getNameFromList(rndIndex[6], ["4fake1a", "4skill2c", "4skill4e"]));
    toDeploy.push(getNameFromList(rndIndex[7], ["4fake0b", "4fake0d"]));
    
    var toRandomize = new Array();
    
    for(var i = 0; i < toDeploy.length; i++) {
        var react = map.getReactorByName(toDeploy[i]);
        
        react.setState(1);
        toRandomize.push(react);
    }
    
    map.shuffleReactors(toRandomize);
}

function setup(channel) {
    var eim = em.newInstance("CWKPQ" + channel);
    
    eim.setProperty("state", "1");
    eim.setProperty("leader", "true");
    eim.setProperty("current_instance", "0");
    eim.setProperty("glpq1", "0");
    eim.setProperty("glpq2", "0");
    eim.setProperty("glpq3", "0");
    eim.setProperty("glpq3_p", "0");
    eim.setProperty("glpq4", "0");
    eim.setProperty("glpq5", "0");
    eim.setProperty("glpq5_room", "0");
    eim.setProperty("glpq6", "0");

    for (var i = 0; i < mapz.length; i++) {
        var map = eim.getInstanceMap(610030000 + mapz[i]);
        if (map != null) {
            map.resetFully();
            if (map.getId() == 610030400) {
                generateMapReactors(map);

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
    eim.dropMessage(5, "[Expedition] " + player.getName() + " has entered the map.");
    var map = eim.getMapInstance(610030100 + (eim.getIntProperty("current_instance") * 100));
    player.changeMap(map, map.getPortal(0));
}

function spawnGuardians(eim) {
    var map = eim.getMapInstance(610030100);
    if (map.countPlayers() <= 0) {
	return;
    }
    map.broadcastStringMessage(5, "The Master Guardians have detected you.");
    for (var i = 0; i < 20; i++) { //spawn 20 guardians
	var mob = eim.getMonster(9400594);
	eim.registerMonster(mob);
	map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(1000, 336));
    }
}

function playerRevive(eim, player) {}

function scheduledTimeout(eim) {
    end(eim);
}

function changedMap(eim, player, mapid) {
    if (mapid < 610030100 || mapid > 610030800) {
	playerExit(eim,player);
    } else {
	switch(mapid) {
	    case 610030200:
		if (eim.getIntProperty("current_instance") == 0) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 1);
		}
		break;
	    case 610030300:
		if (eim.getIntProperty("current_instance") == 1) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 2);
		}
		break;
	    case 610030400:
		if (eim.getIntProperty("current_instance") == 2) {
		    eim.restartEventTimer(600000); //10 mins
		    eim.setIntProperty("current_instance", 3);
		}
		break;
	    case 610030500:
		if (eim.getIntProperty("current_instance") == 3) {
		    eim.restartEventTimer(1200000); //20 mins
		    eim.setIntProperty("current_instance", 4);
		}
		break;
	    case 610030600:
		if (eim.getIntProperty("current_instance") == 4) {
		    eim.restartEventTimer(3600000); //1 hr
		    eim.setIntProperty("current_instance", 5);
		}
		break;
	    case 610030800:
		if (eim.getIntProperty("current_instance") == 5) {
		    eim.restartEventTimer(60000); //1 min
		    eim.setIntProperty("current_instance", 6);
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

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.dropMessage(5, "[Expedition] " + player.getName() + " has left the event.");
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(minPlayers, 610030010)) {
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
    eim.setEventCleared();
}

function monsterKilled(mob, eim) {}
function allMonstersDead(eim) {}

function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}
function dispose(eim) {}