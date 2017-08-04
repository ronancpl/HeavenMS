/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Joko <Crimsonwood Exchange Quest> - Phantom Forest: Dead Man's Gorge(610010004)
-- By ---------------------------------------------------------------------------------------------
	Ronan Lana
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Ronan Lana
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var eQuestChoices = new Array (4032007, 4032006, 4032009, 4032008, 4032007, 4032006, 4032009, 4032008);

var eQuestPrizes = new Array();

eQuestPrizes[0] = new Array ([1002801,1],  // Raven Ninja Bandana
    [1462052,1],	// Raven's Eye
    [1462006,1], 	// Silver Crow
    [1462009,1],	// Gross Jaeger
    [1452012,1],	// Marine Arund
    [1472031,1],        // Black Mamba
    [2044701,1],        // Claw for ATT 60%
    [2044501,1],        // Bow for ATT 60%
    [3010041,1],        // Skull Throne
    [0, 750000]);       // Mesos
    
eQuestPrizes[1] = new Array ([1332077,1],  // Raven's Beak
    [1322062,1],	// Crushed Skull
    [1302068,1], 	// Onyx Blade
    [4032016,1],        // Tao of Sight
    [2043001,1],        // One Handed Sword for Att 60%
    [2043201,1],        // One Handed BW for Att 60%
    [2044401,1],        // Polearm for Att 60%
    [2044301,1],        // Spear for Att 60%
    [3010041,1],        // Skull Throne
    [0,1250000]);       // Mesos
    
eQuestPrizes[2] = new Array ([1472072,1],   //Raven's Claw
    [1332077,1],	// Raven's Beak
    [1402048,1], 	// Raven's Wing
    [1302068,1],        // Onyx Blade
    [4032017,1],        // Tao of Harmony
    [4032015,1],        // Tao of Shadows
    [2043023,1],        // One-Handed Sword for Att 100%[2]
    [2043101,1],        // One-Handed Axe for Att 60%
    [2043301,1],        // Dagger for Att 60%
    [3010040,1],        // The Stirge Seat
    [0,2500000]);       // Mesos
    
eQuestPrizes[3] = new Array ([1002801,1],   //Raven Ninja Bandana
    [1382008,1],	// Kage
    [1382006,1], 	// Thorns
    [4032016,1],        // Tao of Sight
    [4032015,1],        // Tao of Shadows
    [2043701,1],        // Wand for Magic Att 60%
    [2043801,1],        // Staff for Magic Att 60%
    [3010040,1],        // The Stirge Seat
    [0,1750000]);       // Mesos

eQuestPrizes[4] = new Array ([0,3500000]);	// Mesos
eQuestPrizes[5] = new Array ([0,3500000]);	// Mesos
eQuestPrizes[6] = new Array ([0,3500000]);	// Mesos
eQuestPrizes[7] = new Array ([0,3500000]);	// Mesos

var requiredItem  = 0;
var lastSelection = 0;
var prizeItem     = 0;
var prizeQuantity = 0;
var itemSet;
var qnt;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
	cm.sendOk("Hmmm...it shouldn't be a bad deal for you. Come see me at the right time and you may get a much better item to be offered. Anyway, let me know if you have a change of mind.");
	cm.dispose();
	return;
    }
    
    status++;
    if (status == 0) { // first interaction with NPC
        if(cm.getQuestStatus(8225) != 2) {
            cm.sendNext("Hey, I'm not a bandit, ok?");
            cm.dispose();
            return;
        }
        
	cm.sendNext("Hey, got a little bit of time? Well, my job is to collect items here and sell them elsewhere, but these days the monsters have become much more hostile so it have been difficult to get good items... What do you think? Do you want to do some business with me?");
    } else if (status == 1) {
	cm.sendYesNo("The deal is simple. You get me something I need, I get you something you need. The problem is, I deal with a whole bunch of people, so the items I have to offer may change every time you see me. What do you think? Still want to do it?");
    } else if (status == 2) {
	var eQuestChoice = makeChoices(eQuestChoices);
	cm.sendSimple(eQuestChoice);
    } else if (status == 3){
	lastSelection = selection;
	requiredItem = eQuestChoices[selection];
        
        if(selection < 4) qnt = 50;
        else qnt = 25;
        
	cm.sendYesNo("Let's see, you want to trade your #b" + qnt +  " #t" + requiredItem + "##k with my stuff, right? Before trading make sure you have an empty slot available on your use or etc. inventory. Now, do you want to trade with me?");
    }else if (status == 4){
	itemSet = (Math.floor(Math.random() * eQuestPrizes[lastSelection].length));
	reward = eQuestPrizes[lastSelection];
	prizeItem = reward[itemSet][0];
	prizeQuantity = reward[itemSet][1];
	if(!cm.haveItem(requiredItem,qnt)){
	    cm.sendOk("Hmmm... are you sure you have #b" + qnt + " #t" + requiredItem + "##k? If so, then please check and see if your item inventory is full or not.");
	} else if(prizeItem == 0) {
            cm.gainItem(requiredItem,-qnt);
            cm.gainMeso(prizeQuantity);
            cm.sendOk("For your #b" + qnt + " #t"+requiredItem+"##k, here's #b" + prizeQuantity + " mesos#k. What do you think? Did you like the items I gave you in return? I plan on being here for awhile, so if you gather up more items, I'm always open for a trade...");
        } else if(!cm.canHold(prizeItem)){
	    cm.sendOk("Your use and etc. inventory seems to be full. You need the free spaces to trade with me! Make room, and then find me.");
	} else {
	    cm.gainItem(requiredItem,-qnt);
	    cm.gainItem(prizeItem, prizeQuantity);
	    cm.sendOk("For your #b" + qnt + " #t"+requiredItem+"##k, here's my #b"+prizeQuantity+" #t"+prizeItem+"##k. What do you think? Did you like the items I gave you in return? I plan on being here for awhile, so if you gather up more items, I'm always open for a trade...");
	}
	cm.dispose();
    }
}

function makeChoices(a){
    var result  = "Ok! First you need to choose the item that you'll trade with. The better the item, the more likely the chance that I'll give you something much nicer in return.\r\n";
    var qnty = [50, 25];
    
    for (var x = 0; x< a.length; x++){
	result += " #L" + x + "##v" + a[x] + "#  #b#t" + a[x] + "# #kx " + qnty[Math.floor(x/4)] + "#l\r\n";
    }
    return result;
}