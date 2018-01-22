/* Jack
        Refining NPC: 
	* ITEMMAKE
        * 
        * By RonanLana
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;
var last_use; //last item is a use item

function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.sendOk("Very well, see you around.");
        cm.dispose();
        return;
    }

    if (status == 0) {
        var selStr = "Hey, are you aware about the expeditions running right now at the Crimsonwood Keep? So, there is a great opportunity for one to improve themselves, one can rack up experience and loot pretty fast there.";
        cm.sendNext(selStr);
    }
    else if (status == 1) {
	var selStr = "Said so, methinks making use of some strong utility potions can potentially create some differential on the front, and by this I mean to start crafting #b#t2022284##k's to help on the efforts. So, getting right down to business, I'm currently pursuing #rplenty#k of those items: #r#t4032010##k, #r#t4032011##k, #r#t4032012##k, and some funds to support the cause. Would you want to get some of these boosters?";
        cm.sendYesNo(selStr);
    }

    else if (status == 2) {
        //selectedItem = selection;
        selectedItem = 0;

        var itemSet = new Array(2022284, 7777777);
        var matSet = new Array(new Array(4032010, 4032011, 4032012));
        var matQtySet = new Array(new Array(60, 60, 45));
        var costSet = new Array(75000, 7777777);
        item = itemSet[selectedItem];
        mats = matSet[selectedItem];
        matQty = matQtySet[selectedItem];
        cost = costSet[selectedItem];
                
        var prompt = "Ok, I'll be crafting some #t" + item + "#. In that case, how many of those do you want me to make?";
        cm.sendGetNumber(prompt,1,1,100)
    }
        
    else if (status == 3) {
        qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        last_use = false;
                
        var prompt = "So, you want me to make ";
        if (qty == 1)
            prompt += "a #t" + item + "#?";
        else
            prompt += qty + " #t" + item + "#?";
                        
        prompt += " In that case, I'm going to need specific items from you in order to make it. And make sure you have room in your inventory!#b";
                
        if (mats instanceof Array){
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i"+mats[i]+"# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i"+mats+"# " + matQty * qty + " #t" + mats + "#";
        }
                
        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost * qty + " meso";
        }
        cm.sendYesNo(prompt);
    }
    
    else if (status == 4) {
        var complete = true;
                
        if (cm.getMeso() < cost * qty) {
            cm.sendOk("Well, I DID say I would be needing some funds to craft it, wasn't it?");
        }
        else if(!cm.canHold(item, qty)) {
            cm.sendOk("You didn't check if you got a slot to spare on your inventory before crafting, right?");
        }
        else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (matQty[i] * qty == 1) {
                        complete = cm.haveItem(mats[i]);
                    } else {
                        complete = cm.haveItem(mats[i], matQty[i] * qty);
                    }
                }
            } else {
                complete = cm.haveItem(mats, matQty * qty);
            }
            
            if (!complete)
                cm.sendOk("There are not enough resources on your inventory. Please check it again.");
            else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++){
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * qty);
                }
                cm.gainMeso(-cost * qty);
                cm.gainItem(item, qty);
                cm.sendOk("There it is! Thanks for your cooperation.");
            }
        }
        cm.dispose();
    }
}