function action() {
	var flames = Array("a6", "a7", "b6", "b7", "c6", "c7");
	for (var i = 0; i < flames.length; i++) {
		rm.getMap().toggleEnvironment(flames[i]);
	}
}

var fid = "glpq_f2";

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