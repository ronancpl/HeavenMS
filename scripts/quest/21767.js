var status = -1;

function start(mode, type, selection) {
	status++;
	if (status == 0) {
                if(qm.haveItem(4032423, 1)) {
                        qm.forceStartQuest();
                        qm.dispose();
                        return;
                }
            
		qm.sendNext("#bHm, there's a medicinal substance in the box. What could this be? You better take this to John and ask him what it is.#k");
	} else {
                qm.gainItem(4032423,1);
		
                qm.forceStartQuest();
		qm.dispose();
	}
}