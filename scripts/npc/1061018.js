importPackage(Packages.server.events);

var status = 0;
var dispose = false;
function start(){
    status == 0;
    action(1, 0, 0);
}

function action(mode, type, selection){
    if(mode <= 0){
        cm.dispose();
        return;
    } else if(status == 0){
        if(cm.getPlayer().getMap().getMonsters().size() == 0){
            cm.sendOk("Wow! You defeated the balrog.");
            dispose = true;
            cm.getPlayer().getClient().getChannelServer().broadcastPacket(Packages.tools.MaplePacketCreator.serverNotice(0, BalrogPQ.partyLeader + "'s party has successfully defeated the Balrog! Praise to them, they finished with " + cm.getPlayer().getMap().getCharacters().size() + " players."));
            status++;
        } else if(cm.getPlayer().getMap().getCharacters().size() > 1){
            cm.sendYesNo("Are you really going to leave this battle and leave your fellow travelers to die?");
            dispose = false;
            status++;
        } else if(cm.getPlayer().getMap().getCharacters().size() <= 1){
            cm.sendYesNo("If you're a coward, you will leave.");
            dispose = true;
            status++;
        } else {
            cm.sendYesNo("So you are really going to leave?");
            status++;
        }
    } else if(status == 1){
        if(dispose){
            cm.getPlayer().getMap().killAllMonsters();
            BalrogPQ.partyLeader = "undefined";
            BalrogPQ.balrogSpawned = false;
            BalrogPQ.close();
        }
        cm.warp(105100100);
        cm.dispose();
    }
}