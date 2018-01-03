var status = -1;
var sel;

var destinations = new Array("Ellinia", "Ludibrium", "Leafre", "Mu Lung", "Ariant", "Ereve");
var boatType = new Array("the ship", "the train", "the bird", "Hak", "Genie", "the ship");

function start() {
	var message = "Orbis Station has lots of platforms available to choose from. You need to choose the one that'll take you to the destination of your choice. Which platform will you take?\r\n";
	for(var i = 0; i < destinations.length; i++){
		message += "\r\n#L" + i + "##bThe platform to " + boatType[i] + " that heads to " + destinations[i] + ".#l";
	}
	cm.sendSimple(message);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }
    status++;
    if (status == 0){
        sel = selection;
        cm.sendNext("Ok #h #, I will send you to the platform for #b#m" + (200000110 + (sel * 10)) + "##k.");
    }else if (status == 1) {
        cm.warp(200000110 + (sel * 10), "west00");
        cm.dispose();
    }
}