function enter(pi) {
    var react = pi.getMap().getReactorByName("mob0");
    
    if (react.getState() < 1) {
        react.forceHitReactor(1);
        
        var eim = pi.getEventInstance();
        eim.setIntProperty("glpq1", 1);
        
        pi.getEventInstance().dropMessage(5, "A strange force starts being emitted from the portal apparatus, showing a hidden path once blocked now open.");
        pi.playPortalSound(); pi.warp(610030100, 0);
        
        pi.getEventInstance().showClearEffect();
        eim.giveEventPlayersStageReward(1);
        return true;
    }
    
    pi.getEventInstance().dropMessage(5, "The portal apparatus is malfunctional, due to the last transportation. The finding another way through.");
    return false;
}