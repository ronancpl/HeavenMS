function start() {
    if(cm.haveItem(4031576)){
        var em = cm.getEventManager("Genie");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("This will not be a short flight, so you need to take care of some things, I suggest you do that first before getting on board. Do you still wish to board the genie?");
        } else {
            cm.sendOk("This genie is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through the guide at the ticketing booth.");
            cm.dispose();
        }
    } else {
        cm.sendOk("Make sure you got an Ariant ticket to travel in this genie. Check your inventory.");
        cm.dispose();
    }
}
function action(mode, type, selection) {
    if (mode <= 0) {
	cm.sendOk("Okay, talk to me if you change your mind!");
        cm.dispose();
	return;
    }
    
    var em = cm.getEventManager("Genie");
    if (em.getProperty("entry") == "true") {
        cm.warp(200000152);
        cm.gainItem(4031576, -1);
    } else {
        cm.sendOk("This genie is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through the guide at the ticketing booth.");
    }
    
    cm.dispose();
}