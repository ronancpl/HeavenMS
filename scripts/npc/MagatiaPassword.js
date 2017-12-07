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
                cm.sendGetText("The door reacts to the entry pass inserted. #bPassword#k!");
	}
	else if(status == 1){
                if(cm.getText() == cm.getStringQuestProgress(3360, 0)){
                        cm.setQuestProgress(3360, 1, 1);
                        cm.warp((cm.getMapId() == 261010000) ? 261020200 : 261010000, "secret00");
                }
                else {
			cm.sendOk("#rWrong!");
		}
                
                cm.dispose();
	}
}