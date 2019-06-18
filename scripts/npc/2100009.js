/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Aldin
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Ariant Plastic Surgery

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mface_r = Array(20001, 20003, 20009, 20010, 20025, 20031);
var fface_r = Array(21002, 21009, 21011, 21013, 21016, 21029, 21030);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];
        
        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
    }
}

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
                if (type == 7) {
			cm.sendNext("I see...take your time, see if you really want it. Let me know when you make up your mind.");
		}
            
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
                    
                if (status == 0) {
                        cm.sendSimple("Hi, I'm the face surgery assistant doctor from around here. With a #b#t5152029##k or a #b#t5152048##k, I can make it kick in just nice, trust me. Ah, don't forget, what comes next after the operation will be random! Then, what are you going for?\r\n#L1#Plastic Surgery: #i5152029##t5152029##l\r\n#L2#Cosmetic Lens: #i5152048##t5152048##l");
                } else if (status == 1) {
                        if (selection == 1) {
                                beauty = 0;
                            
                                facenew = Array();
                                if (cm.getChar().getGender() == 0) {
                                        for(var i = 0; i < mface_r.length; i++) {
                                                pushIfItemExists(facenew, mface_r[i] + cm.getChar().getFace()
                                                 % 1000 - (cm.getChar().getFace()
                                                 % 100));
                                        }
                                }
                                if (cm.getChar().getGender() == 1) {
                                        for(var i = 0; i < fface_r.length; i++) {
                                                pushIfItemExists(facenew, fface_r[i] + cm.getChar().getFace()
                                                 % 1000 - (cm.getChar().getFace()
                                                 % 100));
                                        }
                                }
                                cm.sendYesNo("If you use the regular coupon, your face may transform into a random new look...do you still want to do it using #b#t5152029##k?");
                        } else if (selection == 2) {
                                beauty = 1;
                                if (cm.getPlayer().getGender() == 0) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 20000;
                                }
                                if (cm.getPlayer().getGender() == 1) {
                                        var current = cm.getPlayer().getFace()
                                        % 100 + 21000;
                                }
                                colors = Array();
                                pushIfItemsExists(colors, [current , current + 100, current + 300, current + 600, current + 700]);
                                cm.sendYesNo("If you use the regular coupon, you'll be awarded a random pair of cosmetic lenses. Are you going to use a #b#t5152048##k and really make the change to your eyes?");
                        }
                } else if (status == 2){	
			cm.dispose();
                        
                        if (beauty == 0) {
                                if (cm.haveItem(5152029) == true){
                                        cm.gainItem(5152029, -1);
                                        cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
                                        cm.sendOk("Enjoy your new and improved face!");
                                } else {
                                        cm.sendNext("Um ... it looks like you don't have the coupon specifically for this place...sorry to say this, but without the coupon, there's no plastic surgery for you.");
                                }
                        } else if (beauty == 1) {
                                if (cm.haveItem(5152048)){
                                        cm.gainItem(5152048, -1);
                                        cm.setFace(colors[Math.floor(Math.random() * colors.length)]);
                                        cm.sendOk("Enjoy your new and improved cosmetic lenses!");
                                } else {
                                       cm.sendOk("Hmm ... it looks like you don't have the coupon specifically for this place. Sorry to say this, but without the coupon, there's no plastic surgery for you...");
                                }
                        }
		}
	}
}