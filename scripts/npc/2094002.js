var status = -1;
var level = 1;

function start() {
    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (cm.getPlayer().getMapId() == 925100700) {
	cm.warp(251010404,0);
	cm.dispose();
	return;
    }
    
    if(status == 1) {   // leaders cant withdraw
        cm.warp(251010404,0);
        return;
    }
    
    if (!cm.isEventLeader()) {
	cm.sendYesNo("I wish for your leader to talk to me. Alternatively, you may be wanting to quit. Are you going to abandon this campaign?");
    }
    else {
        var eim = cm.getEventInstance();
        if (eim == null) {
            cm.warp(251010404,0);
            cm.sendNext("How are you even here without being registered on an instance?");
            cm.dispose();
            return;
        }

        level = eim.getProperty("level");

        switch(cm.getPlayer().getMapId()) {
            case 925100000:
                cm.sendNext("We are heading into the Pirate Ship now! To get in, we must destroy all the monsters guarding it.");
                cm.dispose();
                break;
            case 925100100:
                var emp = eim.getProperty("stage2");
                if (emp == null) {
                    eim.setProperty("stage2", "0");
                    emp = "0";
                }
                if (emp.equals("0")) {
                    if (cm.haveItem(4001120,20)) {
                        cm.sendNext("Excellent! Now hunt me 20 Rising Medals.");
                        cm.gainItem(4001120,-20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "1");
                    } else {
                        cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Hunt me 20 Rookie Medals.");
                        if(cm.countMonster() < 1) cm.getPlayer().getMap().spawnAllMonsterIdFromMapSpawnList(9300114, level, true);
                    }
                } else if (emp.equals("1")) {
                    if (cm.haveItem(4001121,20)) {
                        cm.sendNext("Excellent! Now hunt me 20 Veteran Medals.");
                        cm.gainItem(4001121,-20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "2");
                    } else {
                        cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Hunt me 20 Rising Medals.");
                        if(cm.countMonster() < 1) cm.getPlayer().getMap().spawnAllMonsterIdFromMapSpawnList(9300115, level, true);
                    }
                } else if (emp.equals("2")) {
                    if (cm.haveItem(4001122,20)) {
                        cm.sendNext("Excellent! Now let us go.");
                        cm.gainItem(4001122,-20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "3");
                        eim.showClearEffect(cm.getMapId());
                    } else {
                        cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Hunt me 20 Veteran Medals.");
                        if(cm.countMonster() < 1) cm.getPlayer().getMap().spawnAllMonsterIdFromMapSpawnList(9300116, level, true);
                    }
                } else {
                    cm.sendNext("The next stage has opened. GO!");
                }
                cm.dispose();
                break;
            case 925100200:
            case 925100300:
                cm.sendNext("To assault the pirate ship, we must destroy the guards first.");
                cm.dispose();
                break;
            case 925100201:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("The Lord Pirate's chest has appeared! If you happen to have a key, drop it by the chest to reveal it's treasures. That will certainly make him upset.");
                    if (eim.getProperty("stage2a") == "0") {
                        cm.getMap().setReactorState();
                        eim.setProperty("stage2a", "1");
                    }
                } else {
                    cm.sendNext("These bellflowers are in hiding. We must liberate them.");
                }
                cm.dispose();
                break;
            case 925100301:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("The Lord Pirate's chest has appeared! If you happen to have a key, drop it by the chest to reveal it's treasures. That will certainly make him upset.");
                    if (eim.getProperty("stage3a").equals("0")) {
                        cm.getMap().setReactorState();
                        eim.setProperty("stage3a", "1");
                    }
                } else {
                    cm.sendNext("These bellflowers are in hiding. We must liberate them.");
                }
                cm.dispose();
                break;
            case 925100202:
            case 925100302:
                cm.sendNext("These are the Captains and Krus that devote their lives to the Lord Pirate. Kill them as you see fit.");
                cm.dispose();
                break;
            case 925100400:
                cm.sendNext("These are the sources of the ship's power. We must seal it by using the Old Metal Keys on the doors!");
                cm.dispose();
                break;
            case 925100500:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("Thanks for saving our leader! We are in your debt.");
                } else {
                    cm.sendNext("Defeat all monsters! Even Lord Pirate's minions!");
                }
                cm.dispose();
                break;
        }
    }
    
    
}