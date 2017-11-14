/* Monstrous Looking Statue
	Puppeteer's Secret Passage (910510100)
	Puppeteer JQ.
 */

var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        cm.sendYesNo("Ahead awaits the Master himself. Are you ready to face him?");
                } else {
                        if(cm.getClient().getChannelServer().getMapFactory().getMap(925020010).getCharacters().size() > 0) {
                            cm.sendOk("Someone is already challenging the Master. Try again later.");
                        } else {
                            cm.getWarpMap(910510202).spawnMonsterOnGroundBelow(Packages.server.life.MapleLifeFactory.getMonster(9300346), new java.awt.Point(95, 200));
                            cm.warp(910510202, 0);
                        }
                    
                        cm.dispose();
                }
        }
}