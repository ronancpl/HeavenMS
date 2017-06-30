var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
        var eim = cm.getEventInstance();
        if (eim != null && eim.getIntProperty("glpq6") == 3) {
                cm.sendOk("... Well played. You overtook the Twisted Masters. Pass through that gate to receive your prizes.");
                cm.dispose();
                return;
        }
        
        if (!cm.isEventLeader()) {
                cm.sendNext("I wish for your leader to talk to me.");
                cm.dispose();
                return;
        }
    
        if (mode == 1) {
                status++;
        } else {
                status--;
        }

        if (eim != null) {
                if (eim.getIntProperty("glpq6") == 0) {
                        if (status == 0) {
                                cm.sendNext("Welcome to the Twisted Masters' Keep. I will be your host for this evening...");
                        } else if (status == 1) {
                                cm.sendNext("Tonight, we have a feast of a squad of Maplers.. ahaha...");
                        } else if (status == 2) {
                                cm.sendNext("Let our specially trained Master Guardians escort you!");
                                cm.mapMessage(6, "Engarde! Master Guardians approach!");
                                for (var i = 0; i < 10; i++) {
                                        var mob = eim.getMonster(9400594);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-1337 + (java.lang.Math.random() * 1337), 276));
                                }
                                for (var i = 0; i < 20; i++) {
                                        var mob = eim.getMonster(9400582);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-1337 + (java.lang.Math.random() * 1337), 276));
                                }
                                eim.setIntProperty("glpq6", 1);
                                cm.dispose();
                        }
                } else if (eim.getIntProperty("glpq6") == 1) {
                        if (cm.getMap().countMonsters() == 0) {
                                if (status == 0) {
                                        cm.sendOk("Eh. What is this? You've defeated them?");
                                } else if (status == 1) {
                                        cm.sendNext("Well, no matter! The Twisted Masters will be glad to welcome you.");
                                        cm.mapMessage(6, "Twisted Masters approach!");

                                        //Margana
                                        var mob = eim.getMonster(9400590);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-22, 1));

                                        //Red Nirg
                                        var mob2 = eim.getMonster(9400591);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob2, new java.awt.Point(-22, 276));

                                        //Hsalf
                                        var mob4 = eim.getMonster(9400593);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob4, new java.awt.Point(496, 276));

                                        //Rellik
                                        var mob3 = eim.getMonster(9400592);
                                        cm.getMap().spawnMonsterOnGroundBelow(mob3, new java.awt.Point(-496, 276));

                                        eim.setIntProperty("glpq6", 2);
                                        cm.dispose();
                                }
                        } else {
                                cm.sendOk("Pay no attention to me. The Master Guardians will escort you!");
                                cm.dispose();
                        }
                } else if (eim.getIntProperty("glpq6") == 2) {
                        if (cm.getMap().countMonsters() == 0) {
                                cm.sendOk("WHAT? Ugh... this can't be happening.");
                                cm.mapMessage(5, "The portal to the next stage has opened!");
                                eim.setIntProperty("glpq6", 3);
                                
                                eim.showClearEffect(true);
                                eim.giveEventPlayersStageReward(6);
                                
                                eim.clearPQ();
                                cm.dispose();
                        } else {
                                cm.sendOk("Pay no attention to me. The Twisted Masters will escort you!");
                                cm.dispose();
                        }
                } else {
                        cm.sendOk("... Well played. You overtook the Twisted Masters. Pass through that gate to receive your prizes.");
                        cm.dispose();
                }
        } else {
                cm.dispose();
        }
}