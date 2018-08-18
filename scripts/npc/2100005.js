/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Shati
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Assistant Hairdresser
*/

var status = 0;
var beauty = 0;
var mhair = Array(30250, 30350, 30270, 30150, 30300, 30600, 30160, 30700, 30720, 30420);
var fhair = Array(31040, 31250, 31310, 31220, 31300, 31680, 31160, 31030, 31230, 31690, 31210, 31170, 31450);
var hairnew = Array();

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status >= 0) {
			cm.sendNext("I guess you aren't ready to make the change yet. Let me know when you are!");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Hey there! I'm Shatti, and I'm Mazra's apprentice. If you have #bAriant hair style coupon(REG)#k or #bAriant hair color coupon(REG)#k with you, how about allowing me to work on your hair? \r\n#L0##bChange Hairstyle (Reg Coupon) \r\n#L1##bDye Hair(Reg. coupon)");
		} else if (status == 1) {
			if (selection == 0) {
				beauty = 1;
				hairnew = Array();
				if (cm.getChar().getGender() == 0) {
					for(var i = 0; i < mhair.length; i++) {
						hairnew.push(mhair[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				} 
				if (cm.getChar().getGender() == 1) {
					for(var i = 0; i < fhair.length; i++) {
						hairnew.push(fhair[i] + parseInt(cm.getChar().getHair()
 % 10));
					}
				}
				cm.sendYesNo("If you use the Reg. coupon, your hairstyle will be changed to a random new look. You'll also have access to new hairstyles I worked on that's not available for VIP coupons. Would you like to use #bAriant hair style coupon(REG)#k for a fabulous new look?");
			} else if (selection == 1) {
				beauty = 2;
				haircolor = Array();
				var current = parseInt(cm.getChar().getHair()
/10)*10;
				for(var i = 0; i < 8; i++) {
					haircolor.push(current + i);
				}
				cm.sendYesNo("If you use the regular coupon, your hair color will change to a random new color. Are you sure you want to use #b#t5151021##k and randomly change your hair color?");
			}
		}
		else if (status == 2){
			cm.dispose();
			if (beauty == 1){
				if (cm.haveItem(5150026) == true){
					cm.gainItem(5150026, -1);
					cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
					cm.sendOk("Enjoy your new and improved hairstyle!");
				} else {
					cm.sendNext("I can only change your hairstyle if you bring me the coupon. You didn't forget that, did you?");
				}
			}
			if (beauty == 2){
				if (cm.haveItem(5151021) == true){
					cm.gainItem(5151021, -1);
					cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
					cm.sendOk("Enjoy your new and improved haircolor!");
				} else {
					cm.sendNext("I can only change your hairstyle if you bring me the coupon. You didn't forget that, did you?");
				}
			}
		}
	}
}