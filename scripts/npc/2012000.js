var ticket = new Array(4031047, 4031074, 4031331, 4031576);
var cost = new Array(5000, 6000, 30000, 6000);
var mapNames = new Array("Ellinia of Victoria Island", "Ludibrium", "Leafre", "Ariant");
var mapName2 = new Array("Ellinia of Victoria Island", "Ludibrium", "Leafre of Minar Forest", "Nihal Desert");
var select;
var status = 0;

function start() {
    var where = "Hello, I'm in charge of selling tickets for the ship ride for every destination. Which ticket would you like to purchase?";
    for (var i = 0; i < ticket.length; i++)
        where += "\r\n#L" + i + "##b" + mapNames[i] + "#k#l";
    cm.sendSimple(where);
}

function action(mode, type, selection) {
    if(mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            select = selection;
            cm.sendYesNo("The ride to " + mapName2[select] + " takes off every " + (select == 0 ? 15 : 10) + " minutes, beginning on the hour, and it'll cost you #b"+cost[select]+" mesos#k. Are you sure you want to purchase #b#t"+ticket[select]+"##k?");
        } else if(status == 2) {
            if (cm.getMeso() < cost[select] || !cm.canHold(ticket[select]))
                cm.sendOk("Are you sure you have #b"+cost[select]+" mesos#k? If so, then I urge you to check you etc. inventory, and see if it's full or not.");
            else {
                cm.gainMeso(-cost[select]);
                cm.gainItem(ticket[select],1);
            }
            cm.dispose();
        }
    }
}
