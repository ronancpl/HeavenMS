/* @author RonanLana */

function enter(pi) {
        if (!pi.haveItem(4000381)) {
                pi.playerMessage(5, "You do not have White Essence.");
                return false;
        } else {
                var em = pi.getEventManager("LatanicaBattle");

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
                        else {  //this should never appear
                                pi.playerMessage(5, "You cannot start this battle yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. If you're having trouble finding party members, try Party Search.");
                                return false;
                        }

                        pi.playPortalSound();
                        return true;
                }
        }
}