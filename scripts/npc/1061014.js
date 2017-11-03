importPackage(Packages.server.events);
var status = 0;

function start(){
    status = 0;
    action(1, 0, 0);
}

function action(mode, type, selection){
    if(mode <= 0){
        cm.dispose();
    } else if(status == 0){
        cm.sendNext("Hi there. I am #b#nMu Young#n#k, the temple Keeper.");
        status++;
    } else if(BalrogPQ.partyLeader == "undefined"){
        if(status == 1){
        var text = "This temple is currently under siege by the Balrog troops. We currently do not know who gave the orders. " +
            "For a few weeks now, the #e#b Order of the Altair#n#k has been sending mercenaries, but they were eliminated every time." +
            " So, traveler, would you like to try your luck at defeating this unspeakable horror? \r\n\r\n " +
            "#L0#Yes. Please register me as party leader\r\n#L1#What is the #eOrder of the Altair?";
        cm.sendSimple(text);
        status++;
        } else if(selection == 0){
            if(cm.getPlayer().getLevel() >= 70){
                BalrogPQ.partyLeader = cm.getPlayer().getName();
                cm.sendOk("Success. Your name has been registered and you may enter the battlefield. Come speak to me when you're ready!");
                cm.getPlayer().getMap().broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(0, cm.getPlayer().getName() + " is currently fighting the balrog on CH" + cm.getPlayer().getClient().getChannel() + ". To join, do @balrogpq."))
                BalrogPQ.open(cm.getPlayer());
                cm.dispose();
            } else if(cm.getPlayer().getLevel() < 70){
                cm.sendOk("You must be at least level 70 to even consider battling the monster.");
                cm.dispose();
            }
        } else if(selection == 1){
            cm.sendOk("The Order of the Altair is a group of elite mercenaries that oversee the world's economy and battle operations. It was founded 40 years ago right after Black Mage was defeated in hopes of forseeing the next possible attack.");
            cm.dispose();
        } else if(status == 3){
            cm.warp(105100300, 0);
            cm.dispose();
        }
        } else {
            if(status == 1){
            cm.sendYesNo(BalrogPQ.partyLeader + "'s party is currently battling the Balrog. Would you like to assist?");
            status++;
            } else if(status == 2){
                if(cm.getPlayer().getLevel() > 60 && cm.getPlayer().getClient().getChannel() == BalrogPQ.channel){
                cm.warp(105100300, 0);
                cm.dispose();
                } else {
                    cm.sendOk("You may not battle the balrog when you are below Lv60! \r\n\r\n Or maybe you are not on the right channel.. Try CH" + BalrogPQ.channel + ".");
                    cm.dispose();
                }
            }
        }
}