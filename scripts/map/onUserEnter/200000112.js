var eventName = "Boats";
var toMap = 200090000;

function start(ms) {   	       
	var em = ms.getClient().getEventManager(eventName);
	
	//is the player late to start the travel?
	if(em.getProperty("docked") == "false") {
		ms.getClient().getPlayer().warpAhead(toMap);
        }
}
