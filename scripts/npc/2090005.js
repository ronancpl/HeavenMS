/**
-- Odin JavaScript --------------------------------------------------------------------------------
    Hak - Cabin <To Mu Lung>(200000141) / Mu Lung Temple(250000100) / Herb Town(251000000)
-- By ---------------------------------------------------------------------------------------------
    Information
-- Version Info -----------------------------------------------------------------------------------
    1.1 - Text and statement fix [Information]
    1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

var menu = new Array("Mu Lung","Orbis","Herb Town","Mu Lung");
var cost = new Array(1500,1500,500,1500);
var hak;
var slct;
var display = "";
var btwmsg;
var method;


function start() {
    status = -1;
    hak = cm.getEventManager("Hak");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if(mode == -1) {
        cm.dispose();
        return;
    } else {
        if(mode == 0 && status == 0) {
            cm.dispose();
            return;
        } else if(mode == 0) {
            cm.sendNext("OK. If you ever change your mind, please let me know.");
            cm.dispose();
            return;
        }
        status++;
        if (status == 0) {
            for(var i=0; i < menu.length; i++) {
                if(cm.getPlayer().getMapId() == 200000141 && i < 1) {
                    display += "\r\n#L"+i+"##b"+menu[i]+"("+cost[i]+" mesos)#k";
                } else if(cm.getPlayer().getMapId() == 250000100 && i > 0 && i < 3) {
                    display += "\r\n#L"+i+"##b"+menu[i]+"("+cost[i]+" mesos)#k";
                }
            }
            if(cm.getPlayer().getMapId() == 200000141 || cm.getPlayer().getMapId() == 251000000) {
                btwmsg = "#bOrbis#k to #bMu Lung#k";
            } else if(cm.getPlayer().getMapId() == 250000100) {
                btwmsg = "#bMu Lung#k to #bOrbis#k";
            }
            if(cm.getPlayer().getMapId() == 251000000) {
                cm.sendYesNo("Hello there. How's the traveling so far? I've been transporting other travelers like you to #b"+menu[3]+"#k in no time, and... are you interested? It's not as stable as the ship, so you'll have to hold on tight, but i can get there much faster than the ship. I'll take you there as long as you pay #b"+cost[2]+" mesos#k.");
                status++;
            } else if(cm.getPlayer().getMapId() == 250000100) {
                cm.sendSimple("Hello there. How's the traveling so far? I understand that walking on two legs is much harder to cover ground compared to someone like me that can navigate the skies. I've been transporting other travelers like you to other regions in no time, and... are you interested? If so, then select the town you'd like yo head to.\r\n"+display);
            } else {
                cm.sendSimple("Hello there. How's the traveling so far? I've been transporting other travelers like you to other regions in no time, and... are you interested? If so, then select the town you'd like to head to.\r\n"+display);
            }
        } else if(status == 1) {
            slct = selection;
            cm.sendYesNo("Will you move to #b"+menu[selection]+"#k now? If you have #b"+cost[selection]+" mesos#k, I'll take you there right now.");

        } else if(status == 2) {
            if(slct == 2) {
                if(cm.getMeso() < cost[2]) {
                    cm.sendNext("Are you sure you have enough mesos?");
                    cm.dispose();
                } else {
                    cm.gainMeso(-cost[2]);
                    cm.warp(251000000, 0);
                    cm.dispose();
                }
            }
            
            else {
                if(cm.getMeso() < cost[slct]) {
                        cm.sendNext("Are you sure you have enough mesos?");
                        cm.dispose();
                } else {
                        if(cm.getPlayer().getMapId() == 251000000) {
                            cm.gainMeso(-cost[2]);
                            cm.warp(250000100, 0);
                            cm.dispose();
                        } else {
                            cm.gainMeso(-cost[slct]);
                            hak.newInstance("Hak");
                            hak.setProperty("player", cm.getPlayer().getName());
                            hak.startInstance(cm.getPlayer());
                            cm.dispose();
                        }
                }
            }
        }
    }
}  