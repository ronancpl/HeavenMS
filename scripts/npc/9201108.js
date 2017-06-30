/* @Author SharpAceX
*/

function start() {
        if (cm.getPlayer().getMapId() == 610030500) {
                cm.sendOk("A legendary creature known as the Master Guardian awaits you. It was a Crimson Guardian that Ridley once experimented on, which resulted in its becoming highly resistant to magic attacks, spears, maces, everything--except arrows fired with exceptional power. Bowmen and women! As undisputed masters of the Bow and Arrow, you must use your most powerful attacks--everything from Strafe to Hurricane to Piercing Arrow to destroy this powerful creature and reach the Bowman Statue to claim The Ancestral Bow! Good luck!");
                cm.dispose();
        } else if (cm.getPlayer().getMap().getId() == 610030000) {
                cm.sendOk("One of the only known Holy Archers, Lockewood is one of the Keep's most famous heroes. Of particular note is his custom white and gold battle barb, said to be blessed by a powerfull goddess. His aim was tremendously accurate over long distanes. Feared and respected for his 'Genesis Arrow' and 'Doom Phoenix', he once struck down six Typhons from the Valley of Heroes.");
                cm.dispose();
        } else if (cm.getPlayer().getMapId() == 610030540) {
                if (cm.getPlayer().getMap().countMonsters() == 0) {
                        var eim = cm.getEventInstance();
                        var stgStatus = eim.getIntProperty("glpq5_room");
                        var jobNiche = cm.getPlayer().getJob().getJobNiche();
                    
                        if ((stgStatus >> jobNiche) % 2 == 0) {
                                if(cm.canHold(4001258, 1)) {
                                        cm.gainItem(4001258, 1);
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
                        cm.sendOk("Eliminate all Master Guardians.");
                }
                cm.dispose();
        }
}