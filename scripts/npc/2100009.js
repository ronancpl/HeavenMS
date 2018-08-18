/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Aldin
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
			cm.sendNext("I see...take your time, see if you really want it. Let me know when you make up your mind.");
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
				cm.sendYesNo("If you use the regular coupon, your face may transform into a random new look...do you still want to do it using #b#t5152029##k?");
			}
		else if (status == 1){	
			cm.dispose();
			if (cm.haveItem(5152029) == true){
				cm.gainItem(5152029, -1);
				cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
				cm.sendOk("Enjoy your new and improved face!");
			} else {
				cm.sendNext("Um ... it looks like you don't have the coupon specifically for this place...sorry to say this, but without the coupon, there's no plastic surgery for you.");
			}
		}
	}
}