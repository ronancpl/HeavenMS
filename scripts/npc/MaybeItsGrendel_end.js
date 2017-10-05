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
		cm.sendNext("...a black shadowy figure came out and attacked you? How can this take place at #b#p1032001##k's house? This sounds like one big conspiracy here...");
	}
	else if(status == 1){
		cm.sendNextPrev("I'll have to sort this all out in my mind. Talk to me in a bit.");
	}
	else if(status == 2){
		cm.dispose();
	}
}