function action() {
	var flames = Array("d1", "d2", "e1", "e2", "f1", "f2");
	for (var i = 0; i < flames.length; i++) {
		rm.getMap().toggleEnvironment(flames[i]);
	}
}

var fid = "glpq_f3";

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