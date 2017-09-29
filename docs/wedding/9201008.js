var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    }else if (mode == 0){
        cm.dispose();
		return;		
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			cm.sendOk("Wow Vickii! You look so beautiful today. Are you ready to move on to the next part of this surprise?\r\n I will be giving you a tour for our chapel.");
			cm.getPlayer().startWedding();
		
        }
    }
}