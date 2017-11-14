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
		cm.sendGetText("可疑的声音刺透寂静. #b密码#k!");
	}
	else if(status == 1){
		var text = cm.getText();
		if(text == "Francis is a genius Puppeteer!" || text == "弗朗西斯是天才人偶师" || text == "弗朗西斯是天才人偶师!" || text == "弗朗西斯是天才人偶师！"){
			if(cm.isQuestStarted(20730) && cm.getQuestProgress(20730, 9300285) == 0)
				cm.warp(910510001, 1);
			else
                                cm.playerMessage(5, "尽管你答对了密码, 某种神秘的力量阻挡了你的去路.");

			cm.dispose();
		}
		else{
			cm.sendOk("#r密码错误!");
		}
	}
	else if(status == 2){
		cm.dispose();
	}
}