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
		cm.sendGetText("A suspicious voice pierces through the silence. #bPassword#k!");
	}
	else if(status == 1){
		if(cm.getText() == "Open Sesame"){
			if(cm.isQuestCompleted(3925))
				cm.warp(260010402);
			else
                                cm.playerMessage(5, "Although you said the right answer, the door will not budge.");

			cm.dispose();
		}
		else{
			cm.sendOk("#rWrong!");
		}
	}
	else if(status == 2){
		cm.dispose();
	}
}