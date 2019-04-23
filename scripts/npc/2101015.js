var arena;
var status = 0;

importPackage(Packages.client);

function start() {
    arena = cm.getPlayer().getAriantColiseum();
    if (arena == null) {
        cm.sendOk("Hey, I did not see you on the field during the battle in the arena! What are you doing here?");
        cm.dispose();
        return;
    }
    
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
            menuStr = generateSelectionMenu(["I would like to check my battle points! / I would like to exchange (1) Palm Tree Beach Chair", "I would like to know more about the points of the Battle Arena."]);
            cm.sendSimple("Hello, what I can do for you?\r\n\r\n" + menuStr);
        } else if (status == 1) {
            if (selection == 0) {
                apqpoints = cm.getPlayer().getAriantPoints();
                if (apqpoints < 100) {
                    cm.sendOk("Your Battle Arena score: #b" + apqpoints + "#k points. You need to surpass #b100 points#k so that I can give you the #bPalm Tree Beach Chair#k. Talk to me again when you have enough points.");
                    cm.dispose();
                } else if (apqpoints + arena.getAriantRewardTier(cm.getPlayer()) >= 100) {
                    cm.sendOk("Your Battle Arena score: #b" + apqpoints + "#k points and you pratically already have that score! Talk to my wife, #p2101016#to get them and then re-chat with me!");
                    cm.dispose();
                } else {
                    cm.sendNext("Wow, it looks like you got the #b100#k points ready to trade, let's trade?!");
                }
            } else if (selection == 1) {
                cm.sendOk("The main objective of the Battle Arena is to allow the player to accumulate points so that they can be traded honorably for the highest prize: the #bPalm Tree Beach Chair#k. Collect points during the battles and talk to me when it's time to get the prize. In each battle, the player is given the opportunity to score points based on the amount of jewelry that the player has at the end. But be careful! If your points distance from other players #ris too high#k, this will have been all for nothing and you will earn mere #r1 point#k only.");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.getPlayer().gainAriantPoints(-100);
            cm.gainItem(3010018, 1);
            cm.dispose();
        }
    }
}

function generateSelectionMenu(array) {     // nice tool for generating a string for the sendSimple functionality
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "##b" + array[i] + "#l#k\r\n";
    }
    return menu;
}