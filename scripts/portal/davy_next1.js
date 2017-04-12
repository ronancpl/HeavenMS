function enter(pi) {
    try {
        var em = pi.getEventManager("PiratePQ");
        if (em != null && em.getProperty("stage2").equals("3")) {
            pi.warp(925100200,0); //next
            return(true);
        } else {
            pi.playerMessage(5, "The portal is not opened yet.");
            return(false);
        }
    } catch(e) {
        pi.playerMessage(5, "Error: " + e);
    }
 
    return(false);
}