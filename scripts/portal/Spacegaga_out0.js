//Author: kevintjuh93

function enter(pi) {  
        var eim = pi.getPlayer().getEventInstance();
	var fc = eim.getIntProperty("falls");
        
        if (fc >= 3) {
            pi.playPortalSound(); pi.warp(922240200, 0);
	} else {
            eim.setIntProperty("falls", fc + 1);
	    pi.playPortalSound(); pi.warp(pi.getPlayer().getMapId(), 0);
        }

	return true;
}