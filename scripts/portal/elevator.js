function enter(pi) {
    try {
        var elevator = pi.getEventManager("Elevator");
        if (elevator == null) {
            pi.getPlayer().dropMessage(5, "The elevator is under maintenance.");
        } else if (elevator.getProperty(pi.getMapId() == 222020100 ? ("goingUp") : ("goingDown")).equals("false")) {
            pi.playPortalSound(); pi.warp(pi.getMapId() == 222020100 ? 222020110 : 222020210, 0);
            return true;
        } else if (elevator.getProperty(pi.getMapId() == 222020100 ? ("goingUp") : ("goingDown")).equals("true")) {
            pi.getPlayer().dropMessage(5, "The elevator is currently moving.");
        }
        else pi.getPlayer().dropMessage(5, "Dafuq is happening?!");
    } catch(e) {
        pi.getPlayer().dropMessage(5, "Error: " + e);
    }
	return false;
}