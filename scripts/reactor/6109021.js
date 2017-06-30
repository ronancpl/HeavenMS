function action() { //flame1, top center?
	var flames = Array("a3", "a4", "a5", "b3", "b4", "b5", "c3", "c4", "c5");
	for (var i = 0; i < flames.length; i++) {
		rm.getMap().toggleEnvironment(flames[i]);
	}
}

var fid = "glpq_f1";

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