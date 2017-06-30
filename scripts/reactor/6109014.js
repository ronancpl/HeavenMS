function action() { //flame0, im assuming this is topleft
	var flames = Array("a1", "a2", "b1", "b2", "c1", "c2");
	for (var i = 0; i < flames.length; i++) {
		rm.getMap().toggleEnvironment(flames[i]);
	}
}

var fid = "glpq_f0";

function touch() {
        var eim = rm.getEventInstance();
        
        if(eim.getIntProperty(fid) == 0) action();
        eim.setIntProperty(fid, eim.getIntProperty(fid) + 1);
}

function untouch() {
        var eim = rm.getEventInstance();
        
        if(eim.getIntProperty(fid) == 1) action();
        eim.setIntProperty(fid, eim.getIntProperty(fid) - 1);
}