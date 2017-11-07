var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == -1 || (mode == 0 && status == 0)){
		cm.dispose();
		return;
	}
	else if(mode == 0)
		status--;
	else
		status++;

	if(status == 0){
		cm.sendNext("What the... you don't belong here!");
	}
	else if(status == 1){
		var puppet = cm.getEventManager("Puppeteer");
		puppet.setProperty("player", cm.getPlayer().getName());
		puppet.startInstance(cm.getPlayer());
		cm.dispose();
		return;
	}
}