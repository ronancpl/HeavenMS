/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Badr
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Ariant Plastic Surgery
*/

var status = 0;
var beauty = 0;
var mface = Array(20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20012, 20014);
var fface = Array(21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21012, 21014);
var facenew = Array();

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
				facenew = Array();
				if (cm.getChar().getGender() == 0) {
					for(var i = 0; i < mface.length; i++) {
						facenew.push(mface[i] + cm.getChar().getFace()
 % 1000 - (cm.getChar().getFace()
 % 100));
					}
				}
				if (cm.getChar().getGender() == 1) {
					for(var i = 0; i < fface.length; i++) {
						facenew.push(fface[i] + cm.getChar().getFace()
 % 1000 - (cm.getChar().getFace()
 % 100));
					}
				}
				cm.sendStyle("Hmmm... Face of beauty glows even under cover and burning desert. With #bAriant face coupon(VIP)#k, I can make your face so much better. Choose the face you want, and I will pull out my outstanding skill for the great make over.", facenew);
			}
		else if (status == 1){
			cm.dispose();
			if (cm.haveItem(5152030) == true){
				cm.gainItem(5152030, -1);
				cm.setFace(facenew[selection]);
				cm.sendOk("Enjoy your new and improved face!");
			} else {
				cm.sendNext("Erm... You don't seem to have the exclusive coupon for this hospital. Without the coupon, I'm afraid I can't do it for you.");
			}
		}
	}
}