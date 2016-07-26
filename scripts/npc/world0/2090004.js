/* Author: aaroncsn(MapleSea Like)(Incomplete)
	NPC Name: 		Mr. Do
	Map(s): 		Mu Lung: Mu Lung(2500000000)
	Description: 		Potion Creator
*/
importPackage(Packages.client);

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else
		cm.dispose();
	if (status == 0 && mode == 1) {
		if (cm.isQuestActive(3821)) {
			if(!cm.haveItem(4031554)) {
				if(cm.canHold(4031554)) {
					cm.sendNext("Oh, the boy wanted you to bring him a #t4031554#? No problem, I was on his debt anyway. Now, tell him I am repaying the debt, OK?");
					cm.gainItem(4031554, 1);
					cm.dispose();
					return;
				}
				else {
					cm.sendNext("Make room at your ETC inventory first.");
					cm.dispose();
					return;
				}
			}
		}
		var selStr = "I am a man of many talents. Let me know what you'd like to do. #b"
		var options = new Array("Make a medicine","Make a scroll","Donate medicine ingredients","I want to forfeit the restoration of Portrait Scroll...");
		for (var i = 0; i < options.length; i++){
			selStr += "\r\n#L" + i + "# " + options[i] + "#l";
		}
			
		cm.sendSimple(selStr);
	} else if (status == 1 && mode == 1) {
		selectedType = selection;
		var selStr;
		var items;
		if (selectedType == 0){ //Make a medicine
			cm.sendNext("If you want to make a medicine, you must study the Book on Herbal Medicine first. Nothing is more dangerous than practicing a medicine without proper knowledge.");
			cm.dispose();
			return;
		} 
		else if(selectedType == 1){//Make a scroll
			selStr = "What kind of scrolls are you interested in making?#b";
			items = new Array("Scroll for One-Handed Sword for ATT", "Scroll for One-Handed Axe for ATT", "Scroll for One-Handed BW for ATT",
								  "Scroll for Dagger for ATT","Scroll for Wand for Magic Att.","Scroll for Staff for Magic Att.",
								  "Scroll for Two-handed Sword for ATT.","Scroll for Two-handed Axe for ATT","Scroll for Two-handed BW for ATT",
								  "Scroll for Spear for ATT","Scroll for Pole Arm for ATT","Scroll for Bow for ATT","Scroll for Crossbow for ATT ",
								  "Scroll for Claw for ATT","Knuckle Attack Power Scroll","Gun Attack Power Scroll#k");
		} 
		else if(selectedType == 2){//Donate medicine ingredients
			selStr = "So you wish to donate some medicine ingredients? This is great news! Donations will be accepted in the unit of #b100#k. The donator will receive a marble that enables one to make a scroll. Which of these would you like to donate? #b";
			items = new Array("Acorn","Thimble","Needle Pouch","Necki Flower","Necki Swimming Cap","Broken Piece of Pot","Ginseng-Boiled Water","Straw Doll","Wooden Doll","Bellflower Root","100-Year-Old Bellflower",
							  "Old Paper","Yellow Belt","Broken Deer Horn","Red Belt","Peach Seed","Mr. Alli's Leather","Cat Doll","Mark of the Pirate","Captain Hat#k");
		}
		else {//I want to forfeit the restoration of Portrait Scroll...
			cm.dispose();
			return;
		}
		for (var i = 0; i < items.length; i++){
			selStr += "\r\n#L" + i + "# " + items[i] + "#l";
		}
		cm.sendSimple(selStr);
	}
	else if (status == 2 && mode == 1){
		selectedItem = selection;
		if (selectedType == 1){ //Scrolls
			var itemSet = new Array(2043000,2043100,2043200,2043300,2043700,2043800,2044000,2044100,2044200,2044300,2044400,2044500,2044600,2044700,2044800,2044900);
			var matSet = new Array(new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),
						new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),
						new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001));
			var matQtySet = new Array(new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),
							new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),new Array(100, 10),
							new Array(100, 10));
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			var prompt = "You want to make #t" + item + "#? In order to make #t" + item +"#,You'll need #b100 Dr. Do's Marbles#k and #b10 Steel Ores.#k";
			if (mats instanceof Array){
			for(var i = 0; i < mats.length; i++){
				prompt += "\r\n#i"+mats[i]+"# " + matQty[i] + " #t" + mats[i] + "#";
			}
		}
		else {
			prompt += "\r\n#i"+mats+"# " + matQty + " #t" + mats + "#What do you think? Would you like to make on right now?";
		}
			cm.sendYesNo(prompt);
		} 
		else if(selectedType == 2){
			status = 3;
			var itemSet = new Array(4000276,4000277,4000278,4000279,4000280,4000291,4000292,4000286,4000287,4000293, 4000294,4000298,4000284,4000288,4000285,4000282,4000295,4000289,4000296,4031435);
			item = itemSet[selectedItem];
			var prompt = "Are you sure you want to donate #b100 #t " + item + "##k?";
			cm.sendYesNo(prompt);
		}
	} else if (status == 3 && mode == 1) {
		var complete = false;
		if (mats instanceof Array) {
			for(var i = 0; i < mats.length; i++) {
				if (matQty[i] == 1) {
					if (!cm.haveItem(mats[i])) {
						complete = false;
					}
				}
				else {
					var count = 0;
					var iter = cm.getInventory(4).listById(mats[i]).iterator();
					while (iter.hasNext()) {
						count += iter.next().getQuantity();
                                        }
					if (count < matQty[i])
						complete = false;
				}					
			}
		}
		else {
			var count = 0;
			var iter = cm.getInventory(4).listById(mats).iterator();
			while (iter.hasNext()) {
				count += iter.next().getQuantity();
			}
			if (count < matQty)
				complete = false;
		}
		
        	if (!complete || !cm.canHold(2044900))
			cm.sendOk("Please make sure you are neither lacking ingredients or lacking space in your use inventory.");
		else {
			if (mats instanceof Array) {
				for (var i = 0; i < mats.length; i++){
					cm.gainItem(mats[i], -matQty[i]);
				}
			}
			else
				cm.gainItem(mats, -matQty);
		}
                
                cm.dispose();
	}
}
