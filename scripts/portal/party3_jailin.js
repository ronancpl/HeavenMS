importPackage(Packages.tools);

function enter(pi) {
        var map = pi.getMap();
    
        var jailn = (pi.getMap().getId() / 10) % 10;
        var maxToggles = (jailn == 1) ? 7 : 6;
        
        var mapProp = pi.getEventInstance().getProperty("jail" + jailn);
        
        if(mapProp == null) {
            var seq = 0;
            
            for(var i = 1; i <= maxToggles; i++) {
                if(Math.random() < 0.5) seq += (1 << i);
            }
            
            pi.getEventInstance().setProperty("jail" + jailn, seq);
            mapProp = seq;
        }
        
        mapProp = Number(mapProp);
        if(mapProp != 0) {
            var countMiss = 0;
            for(var i = 1; i <= maxToggles; i++) {
                if(!(pi.getMap().getReactorByName("lever" + i).getState() == (mapProp >> i) % 2)) {
                    countMiss++;
                }
            }
            
            if(countMiss > 0) {
                map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/wrong_kor"));
                map.broadcastMessage(MaplePacketCreator.playSound("Party1/Failed"));
                
                pi.playerMessage(5, "The right combination of levers is needed to pass. " + countMiss + " lever(s) are misplaced.");
                return false;
            }
            
            map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear"));
            map.broadcastMessage(MaplePacketCreator.playSound("Party1/Clear"));
            pi.getEventInstance().setProperty("jail" + jailn, "0");
        }
        
        pi.playPortalSound(); pi.warp(pi.getMapId() + 2,0);
        return true;
}