/* @Author SharpAceX
*/

function start() {
        if (cm.getPlayer().getMapId() == 610030500) {
                cm.sendOk("As a powerful Elite Mage, Ridly knew the value of intelligence, the hallmark quality of a wizard. Thus, the Mage Chamber is a twisting maze of devious conception--the Teleport skill is the only skill you can use inside to get around, and Magic Claw is the only skill that will break the statues. You must also kill numerous monsters within. After you solve the maze and defeat all the foes within it, deduce which Mage Statue hides the Staff of First Magic and break it open to claim it! Good luck!");
                cm.dispose();
        } else if (cm.getPlayer().getMap().getId() == 610030000) {
                cm.sendOk("A name forever remembered, Rafael was an exceptionally skilled sorcerer, and the foremost master of mental magic powers, telekinesis and telepathy. In addition to that, he was one of the 'Elite Mages' who mastered all the elements. He was last seen looking for the 'Temple of the Elementals' to turn the tide against the invading Krakian Army...");
                cm.dispose();
        } else if (cm.getPlayer().getMapId() == 610030521) {
                if (cm.getPlayer().getMap().countMonsters() == 0) {
                        var eim = cm.getEventInstance();
                        var stgStatus = eim.getIntProperty("glpq5_room");
                        var jobNiche = cm.getPlayer().getJob().getJobNiche();
                    
                        if ((stgStatus >> jobNiche) % 2 == 0) {
                                if(cm.canHold(4001257, 1)) {
                                        cm.gainItem(4001257, 1);
                                        cm.sendOk("Good job.");
                                        
                                        stgStatus += (1 << jobNiche);
                                        eim.setIntProperty("glpq5_room", stgStatus);
                                } else {
                                        cm.sendOk("Make room on your ETC inventory first.");
                                }
                        } else {
                                cm.sendOk("The weapon inside this room has already been retrieved.");
                        }
                } else {
                        cm.sendOk("Eliminate all monsters.");
                }
                cm.dispose();
        } /* else if (cm.getPlayer().getMapId() == 610030522) {
                if (cm.getPlayer().getMap().countMonsters() == 0) {
                        cm.warp(610030522,0);
                } else {
                        cm.sendOk("Eliminate all monsters.");
                }
                cm.dispose();
        }
        */
}