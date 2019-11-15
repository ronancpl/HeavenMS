var status = -1;
var canStart;

function start(mode, type, selection) {
	status++;
	if (status == 0) {
                if(qm.haveItem(4032423, 1)) {
                        qm.forceStartQuest();
                        qm.dispose();
                        return;
                }
                
                canStart = qm.canHold(4032423, 1);
                if(!canStart) {
                        qm.sendNext("Please open a slot in your ETC inventory first.");
                        return;
                }
            
		qm.sendNext("#bHm, there's a medicinal substance in the box. What could this be? You better take this to John and ask him what it is.#k");
	} else if (status == 1) {
                if(canStart) {
                        qm.gainItem(4032423,1);
                        qm.forceStartQuest();
                }
                
		qm.dispose();
	}
}