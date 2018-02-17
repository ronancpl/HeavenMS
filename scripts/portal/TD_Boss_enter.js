/* @author RonanLana */

function enter(pi) {
        var stage = ((Math.floor(pi.getMapId() / 100)) % 10) - 1;
        var em = pi.getEventManager("TD_Battle" + stage);
        if(em == null) {
                pi.playerMessage(5, "TD Battle " + stage + " encountered an unexpected error and is currently unavailable.");
                return false;
        }

        if (pi.getParty() == null) {
                pi.playerMessage(5, "You are currently not in a party, create one to attempt the boss.");
                return false;
        } else if(!pi.isLeader()) {
                pi.playerMessage(5, "Your party leader must enter the portal to start the battle.");
                return false;
        } else {
                var eli = em.getEligibleParty(pi.getParty());
                if(eli.size() > 0) {
                        if(!em.startInstance(pi.getParty(), pi.getPlayer().getMap(), 1)) {
                                pi.playerMessage(5, "The battle against the boss has already begun, so you may not enter this place yet.");
                                return false;
                        }
                }
                else {
                        pi.playerMessage(5, "Your party must consist of at least 2 players to attempt the boss.");
                        return false;
                }

                pi.playPortalSound();
                return true;
        }
}
