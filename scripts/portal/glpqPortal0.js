function enter(pi) {
    if (pi.getEventInstance().getIntProperty("glpq1") == 0) {
        pi.getEventInstance().dropMessage(5, "This path is currently blocked.");
        return false;
        
    } else {
        pi.playPortalSound(); pi.warp(610030200, 0);
        return true;
    }
}

