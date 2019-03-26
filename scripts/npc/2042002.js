/**
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Drago (MapleStorySA)
        2.0 - Second Version by Ronan (HeavenMS)
        3.0 - Third Version by Jayd - translated CPQ contents to English & added Pirate items
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var rnk = -1;
var n1 = 50; //???
var n2 = 40; //??? ???
var n3 = 7; //35
var n4 = 10; //40
var n5 = 20; //50

var cpqMap = 980000000;
var cpqMinLvl = 30;
var cpqMaxLvl = 50;
var cpqMinAmt = 2;
var cpqMaxAmt = 6;

// Ronan's custom ore refiner NPC
var refineRocks = true;     // enables moon rock, star rock
var refineCrystals = true;  // enables common crystals
var refineSpecials = true;  // enables lithium, special crystals
var feeMultiplier = 7.0;

function start() {
    status = -1;
    
    if (!Packages.constants.ServerConstants.USE_CPQ) {
        if (Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
            status = 0;
            action(1, 0, 4);
        } else {
            cm.sendOk("The Monster Carnival is currently unavailable.");
            cm.dispose();
        }
        
        return;
    }
    
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        
        if (cm.getPlayer().getMapId() == 980000010) {
            if (status == 0) {
                cm.sendNext("I hope you had fun at the Monster Carnival!");
            } else if (status > 0) {
                cm.warp(980000000, 0);
                cm.dispose();
            }
        } else if (cm.getChar().getMap().isCPQLoserMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shiu = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shiu += "#rA#k";
                        cm.sendOk("Unfortunately, you either drew or lost the battle despite your excellent performance. Victory can be yours next time! \r\n\r\n#bYour result: " + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("Unfortunately, you either drew or lost the battle, even with your ultimate performance. Just a little bit, and the victory could have been yours! \r\n\r\n#bYour result: " + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("Unfortunately, you either drew or lost the battle. Victory is for those who strive. I see your efforts, so victory is not far from your reach. Keep it up!\r\n\r\n#bYour result: " + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("Unfortunately, you either equalized or lost the battle, and your performance clearly reflects on it. I expect more from you next time. \r\n\r\n#bYour result: " + shiu);
                    }
                } else {
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 10:
                        cm.warp(980000000, 0);
                        cm.gainExp(17500);
                        cm.dispose();
                        break;
                    case 20:
                        cm.warp(980000000, 0);
                        cm.gainExp(1200);
                        cm.dispose();
                        break;
                    case 30:
                        cm.warp(980000000, 0);
                        cm.gainExp(5000);
                        cm.dispose();
                        break;
                    case 40:
                        cm.warp(980000000, 0);
                        cm.gainExp(2500);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getChar().getMap().isCPQWinnerMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shi = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shi += "#rA#k";
                        rnk = 1;
                        cm.sendOk("Congratulations on your victory!!! What a performance! The opposite group could not do anything! I hope the same good work next time! \r\n\r\n#bYour result: " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("Congratulations on your victory! That was awesome! You did a good job against the opposing group! Just a little longer, and you'll definitely get an A next time! \r\n\r\n#bYour result: " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("Congratulations on your victory. You did some things here and there, but that can not be considered a good victory. I expect more from you next time. \r\n\r\n#bYour result: " + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("Congratulations on your victory, though your performance did not quite reflect that. Be more active in your next participation in the Monster Carnival! \r\n\r\n#bYour result: " + shi);
                    }
                } else {
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 1:
                        cm.warp(980000000, 0);
                        cm.gainExp(50000);
                        cm.dispose();
                        break;
                    case 2:
                        cm.warp(980000000, 0);
                        cm.gainExp(25500);
                        cm.dispose();
                        break;
                    case 3:
                        cm.warp(980000000, 0);
                        cm.gainExp(21000);
                        cm.dispose();
                        break;
                    case 4:
                        cm.warp(980000000, 0);
                        cm.gainExp(19505);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getMapId() == cpqMap) {   // only CPQ1
            if (status == 0) {
                if (cm.getParty() == null) {
                    status = 10;
                    cm.sendOk("You need to create a party first before you can join the battle!");
                } else if (!cm.isLeader()) {
                    status = 10;
                    cm.sendOk("If you want to start the battle, let the #bParty Leader#k talk to me.");
                } else {
                    var party = cm.getParty().getMembers();
                    var inMap = cm.partyMembersInMap();
                    var lvlOk = 0;
                    var isOutMap = 0;
                    for (var i = 0; i < party.size(); i++) {
                        if (party.get(i).getLevel() >= cpqMinLvl && party.get(i).getLevel() <= cpqMaxLvl) {
                            lvlOk++;

                            if (party.get(i).getPlayer().getMapId() != cpqMap) {
                                isOutMap++;
                            }
                        }
                    }

                    if (party >= 1) {
                        status = 10;
                        cm.sendOk("You do not have enough people in your party. You need a party with #b" + cpqMinAmt + "#k - #r" + cpqMaxAmt + "#k members and they should be on the map with you.");
                    } else if (lvlOk != inMap) {
                        status = 10;
                        cm.sendOk("Make sure everyone in your party is among the correct levels (" + cpqMinLvl + "~" + cpqMaxLvl + ")!");
                    } else if (isOutMap > 0) {
                        status = 10;
                        cm.sendOk("There are some of the party members that is not on the map!");
                    } else {
                        if (!cm.sendCPQMapLists()) {
                            cm.sendOk("All Monster Carnival fields are currently in use! Try again later.");
                            cm.dispose();
                        }
                    }
                }
            } else if (status == 1) {
                if (cm.fieldTaken(selection)) {
                    if (cm.fieldLobbied(selection)) {
                        cm.challengeParty(selection);
                        cm.dispose();
                    } else {
                        cm.sendOk("The room is currently full.");
                        cm.dispose();
                    }
                } else {
                    var party = cm.getParty().getMembers();
                    if ((selection >= 0 && selection <= 3) && party.size() < 1) {
                        cm.sendOk("You need at least 2 players to participate in the battle!");
                    } else if ((selection >= 4 && selection <= 5) && party.size() < 1) {
                        cm.sendOk("You need at least 3 players to participate in the battle!");
                    } else {
                        cm.cpqLobby(selection);
                    }
                    cm.dispose();
                }
            } else if (status == 11) {
                cm.dispose();
            }
        } else {
            if (status == 0) {
                var talk = "What would you like to do? If you have never participate in the Monster Carnival, you will need to know a few things before participating! \r\n#b#L0# Go to the Monster Carnival 1.#l \r\n#L3# Go to the Monster Carnival 2.#l \r\n#L1# Learn about the Monster Carnival.#l\r\n#L2# Trade #t4001129#.#l";
                if (Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
                    talk += "\r\n#L4# ... Can I just refine my ores?#l";
                }
                cm.sendSimple(talk);
            } else if (status == 1) {
                if (selection == 0) {
                    if ((cm.getLevel() > 29 && cm.getLevel() < 51) || cm.getPlayer().isGM()) {
                        cm.getChar().saveLocation("MONSTER_CARNIVAL");
                        cm.warp(980000000, 0);
                        cm.dispose();
                        return;
                    } else if (cm.getLevel() < 30) {
                        cm.sendOk("You must be at least level 30 to participate in the Monster Carnival. Talk to me when you're strong enough.");
                        cm.dispose();
                        return;
                    } else {
                        cm.sendOk("I'm sorry, but only players of level 30 ~ 50 can participate in the Monster Carnival.");
                        cm.dispose();
                        return;
                    }
                } else if (selection == 1) {
                    status = 60;
                    cm.sendSimple("What would you like to do?\r\n#b#L0# What is Monster Carnival?#l\r\n#L1# Overview of the Monster Carnival.#l\r\n#L2# Detailed information about the Monster Carnival.#l\r\n#L3# Nothing really, I've changed my mind.#l");
                } else if (selection == 2) {
                    cm.sendSimple("Remember, if you have #t4001129#, you can exchange for items. Select the item you would like to change them! \r\n#b#L0# #t1122007# (" + n1 + " coins)#l\r\n#L1# #t2041211# (" + n2 + " coins)#l\r\n#L2# Weapons for Warriors#l\r\n#L3# Weapons for Magician#l\r\n#L4# Weapons for Archers#l\r\n#L5# Weapons for Thief#l\r\n#L6# Weapons for Pirate#l");
                } else if (selection == 3) {
                    cm.getChar().saveLocation("MONSTER_CARNIVAL");
                    cm.warp(980030000, 0);
                    cm.dispose();
                    return;
                } else if (selection == 4) {
                    var selStr = "Very well, instead I offer a steadfast #bore refining#k service for you, taxing #r" + ((feeMultiplier * 100) | 0) + "%#k over the usual fee to synthetize them. What will you do?#b";

                    var options = new Array("Refine mineral ores","Refine jewel ores");
                    if(refineCrystals) {
                        options.push("Refine crystal ores");
                    }
                    if(refineRocks) {
                        options.push("Refine plates/jewels");
                    }

                    for (var i = 0; i < options.length; i++){
                        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
                    }

                    cm.sendSimple(selStr);
                    
                    status = 76;
                }
            } else if (status == 2) {
                select = selection;
                if (select == 0) {
                    if (cm.haveItem(4001129, n1) && cm.canHold(4001129)) {
                        cm.gainItem(1122007, 1);
                        cm.gainItem(4001129, -n1);
                        cm.dispose();
                    } else {
                        cm.sendOk("Check and see if you are missing #b#t4001129##k or if your EQUIP inventory is full.");
                        cm.dispose();
                    }
                } else if (select == 1) {
                    if (cm.haveItem(4001129, n2) && cm.canHold(2041211)) {
                        cm.gainItem(2041211, 1);
                        cm.gainItem(4001129, -n2);
                        cm.dispose();
                    } else {
                        cm.sendOk("Check and see if you are missing #b#t4001129##k or if your USE inventory is full.");
                        cm.dispose();
                    }
                } else if (select == 2) {//S2 Warrior 26 S3 Magician 6 S4 Bowman 6 S5 Thief 8
                    status = 10;
                    cm.sendSimple("Please make sure you have #t4001129# for the weapon you want. Select the weapon you would like to trade #t4001129#. The choices I have are really good, and I'm not the one who speaks to the people who say it! \r\n#b#L0# #z1302004# (" + n3 + " coins)#l\r\n#L1# #z1402006# (" + n3 + " coins)#l\r\n#L2# #z1302009# (" + n4 + " coins)#l\r\n#L3# #z1402007# (" + n4 + " coins)#l\r\n#L4# #z1302010# (" + n5 + " coins)#l\r\n#L5# #z1402003# (" + n5 + " coins)#l\r\n#L6# #z1312006# (" + n3 + " coins)#l\r\n#L7# #z1412004# (" + n3 + " coins)#l\r\n#L8# #z1312007# (" + n4 + " coins)#l\r\n#L9# #z1412005# (" + n4 + " coins)#l\r\n#L10# #z1312008# (" + n5 + " coins)#l\r\n#L11# #z1412003# (" + n5 + " coins)#l\r\n#L12# Continue to the next page(1/2)#l");
                } else if (select == 3) {
                    status = 20;
                    cm.sendSimple("Select the weapon you would like to trade. The weapons I have here are extremely attractive. See for yourself! \r\n#b#L0# #z1372001# (" + n3 + " coins)#l\r\n#L1# #z1382018# (" + n3 + " coins)#l\r\n#L2# #z1372012# (" + n4 + " coins)#l\r\n#L3# #z1382019# (" + n4 + " coins)#l\r\n#L4# #z1382001# (" + n5 + " coins)#l\r\n#L5# #z1372007# (" + n5 + " coins)#l");
                } else if (select == 4) {
                    status = 30;
                    cm.sendSimple("Select the weapon you would like to trade. The weapons I have here are extremely attractive. See for yourself! \r\n#b#L0# #z1452006# (" + n3 + " coins)#l\r\n#L1# #z1452007# (" + n4 + " coins)#l\r\n#L2# #z1452008# (" + n5 + " coins)#l\r\n#L3# #z1462005# (" + n3 + " coins)#l\r\n#L4# #z1462006# (" + n4 + " coins)#l\r\n#L5# #z1462007# (" + n5 + " coins)#l");
                } else if (select == 5) {
                    status = 40;
                    cm.sendSimple("Select the weapon you would like to trade for. The weapons I have are of the highest quality. Select the one most appealing to you! \r\n#b#L0# #z1472013# (" + n3 + " coins)#l\r\n#L1# #z1472017# (" + n4 + " coins)#l\r\n#L2# #z1472021# (" + n5 + " coins)#l\r\n#L3# #z1332014# (" + n3 + " coins)#l\r\n#L4# #z1332031# (" + n4 + " coins)#l\r\n#L5# #z1332011# (" + n4 + " coins)#l\r\n#L6# #z1332016# (" + n5 + " coins)#l\r\n#L7# #z1332003# (" + n5 + " coins)#l");
                } else if (select == 6) {
                    status = 50; //pirate rewards
                    cm.sendSimple("Select the weapon you would like to trade for. The weapons I have are of the highest quality. Select the one most appealing to you! \r\n#b#L0# #z1482005# (" + n3 + " coins)#l \r\n#b#L1# #z1482006# (" + n4 + " coins)#l \r\n#b#L2# #z1482007# (" + n5 + " coins)#l \r\n#b#L3# #z1492005# (" + n3 + " coins)#l \r\n#b#L4# #z1492006# (" + n4 + " coins)#l \r\n#b#L5# #z1492007# (" + n5 + " coins)#l");
                }
            } else if (status == 11) {
                if (selection == 12) {
                    cm.sendSimple("Select the weapon you would like to trade. The weapons I have here are extremely useful. Take a look! \r\n#b#L0# #z1322015# (" + n3 + " coins)#l\r\n#L1# #z1422008# (" + n3 + " coins)#l\r\n#L2# #z1322016# (" + n4 + " coins)#l\r\n#L3# #z1422007# (" + n4 + " coins)#l\r\n#L4# #z1322017# (" + n5 + " coins)#l\r\n#L5# #z1422005# (" + n5 + " coins)#l\r\n#L6# #z1432003# (" + n3 + " coins)#l\r\n#L7# #z1442003# (" + n3 + " coins)#l\r\n#L8# #z1432005# (" + n4 + " coins)#l\r\n#L9# #z1442009# (" + n4 + " coins)#l\r\n#L10# #z1442005# (" + n5 + " coins)#l\r\n#L11# #z1432004# (" + n5 + " coins)#l\r\n#L12# Back to the first page (2/2)#l");
                } else {
                    var item = new Array(1302004, 1402006, 1302009, 1402007, 1302010, 1402003, 1312006, 1412004, 1312007, 1412005, 1312008, 1412003);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                        cm.dispose();
                    }
                }
            } else if (status == 12) {
                if (selection == 12) {
                    status = 10;
                    cm.sendSimple("Please make sure you have #b#t4001129##k for the weapon you want. Select the weapon you would like to trade #t4001129#. The choices I have are really good, and I'm not the one who speaks to the people who say it! \r\n#b#L0# #z1302004# (" + n3 + " coins)#l\r\n#L1# #z1402006# (" + n3 + " coins)#l\r\n#L2# #z1302009# (" + n4 + " coins)#l\r\n#L3# #z1402007# (" + n4 + " coins)#l\r\n#L4# #z1302010# (" + n5 + " coins)#l\r\n#L5# #z1402003# (" + n5 + " coins)#l\r\n#L6# #z1312006# (" + n3 + " coins)#l\r\n#L7# #z1412004# (" + n3 + " coins)#l\r\n#L8# #z1312007# (" + n4 + " coins)#l\r\n#L9# #z1412005# (" + n4 + " coins)#l\r\n#L10# #z1312008# (" + n5 + " coins)#l\r\n#L11# #z1412003# (" + n5 + " coins)#l\r\n#L12# Continue to the next page(1/2)#l");
                } else {
                    var item = new Array(1322015, 1422008, 1322016, 1422007, 1322017, 1422005, 1432003, 1442003, 1432005, 1442009, 1442005, 1432004);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                        cm.dispose();
                    }
                }
            } else if (status == 21) {
                var item = new Array(1372001, 1382018, 1372012, 1382019, 1382001, 1372007);
                var cost = new Array(n3, n3, n4, n4, n5, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                    cm.dispose();
                }
            } else if (status == 31) {
                var item = new Array(1452006, 1452007, 1452008, 1462005, 1462006, 1462007);
                var cost = new Array(n3, n4, n5, n3, n4, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                    cm.dispose();
                }
            } else if (status == 41) {
                var item = new Array(1472013, 1472017, 1472021, 1332014, 1332031, 1332011, 1332016, 1332003);
                var cost = new Array(n3, n4, n5, n3, n4, n4, n5, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                    cm.dispose();
                }
            } else if (status == 51) {
                var item = new Array(1482005, 1482006, 1482007, 1492005, 1492006, 1492007);
                var cost = new Array(n3, n4, n5, n3, n4, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("You do not have enough #b#t4001129##k, or your inventory is full. Please check again.");
                    cm.dispose();
                }
            } else if (status == 61) {
                select = selection;
                if (selection == 0) {
                    cm.sendNext("Haha! I am Spiegelmann, the leader of this Monster Carnival. I got the first #bMonster Carnival#k here, waiting for travelers like you to take part in this extravaganza!");
                } else if (selection == 1) {
                    cm.sendNext("#bMonster Carnival#k consists of 2 groups entering the battlefield, and dropping the monsters invoked by the other party. #bA combat brigade that determines the victor by the amount of Carnival Points (CP) received#k.");
                } else if (selection == 2) {
                    cm.sendNext("When you enter the Carnival Field, you will see the Monster List window appear. All you need to do is #bselect what you want to use, and press OK#k. Very easy, right?");
                } else {
                    cm.dispose();
                }
            } else if (status == 62) {
                if (select == 0) {
                    cm.sendNext("What is #bMonster Carnival#k? Hahaha! Let's say it's an experience you'll never forget! It's a battle against other travelers just like you!#k");
                } else if (select == 1) {
                    cm.sendNext("When entering the Carnival Field, your task is to #breceive CP by killing the monsters from the opposite group, and using these CP's to distract the opposing group from hitting monsters#k.");
                } else if (select == 2) {
                    cm.sendNext("Once you get used to the commands, try using #bTAB and F1 ~ F12#k. #bTAB toggles between Monster Invocation / Skills / Protector#k, and, #bF1 ~ F12 enables you to access one of the windows directly#k.");
                }
            } else if (status == 63) {
                if (select == 0) {
                    cm.sendNext("I know it's too dangerous for you to fight with each other using real weapons; and I would not suggest such a barbaric act. Not my friend, what I offer to the competition. The excitement of the battle and the excitement of competing against such strong and motivated people. I offer the premise that your group and the opposite group both #binvoquem the monsters, and defeat the monsters invoked by the opposing group. This is the essence of the Monster Carnival. In addition, you can use Maple Coins earned during the Monster Carnival to get new items and weapons! #k");
                } else if (select == 1) {
                    cm.sendNext("There are 3 ways to distract the opposing group: #bSummoning a monster, Ability, and Protector#k. I will give you a more in-depth look if you want to know more about 'detailed instructions'!");
                } else if (select == 2) {
                    cm.sendNext("#bSummoning#k a Monster calls a monster that attacks the opposing party, under its control. Use CP to bring an Summoned Monster, and it will appear in the same area, attacking the opposing group.");
                }
            } else if (status == 64) {
                if (select == 0) {
                    cm.sendNext("Of course, it's not that simple. There are other ways to prevent the other group from dropping monsters, and it's up to you to figure out how to do it. What do you think? Interested in a friendly competition?");
                    cm.dispose();
                } else if (select == 1) {
                    cm.sendNext("Please remember. It's never a good idea to keep your CP's. #bThe CPs you used will help determine the winner and loser of Monster Carnival.");
                } else if (select == 2) {
                    cm.sendNext("#bAbility#k is an option to use abilities such as Darkness, Weakness, and others to prevent the opposing group from killing other monsters. Not many CPs are needed, but it's worth it. The only problem is they do not last very long. Use this tactic wisely!");
                }
            } else if (status == 65) {
                if (select == 1) {
                    cm.sendNext("Oh, and do not worry about turning into a ghost. In the Monster Carnival, #byou will not lose EXP after death#k. So it's really an experience like no other!");
                    cm.dispose();
                } else if (select == 2) {
                    cm.sendNext("#bProtetor#k basically an invoked item that drastically increases the abilities of the monsters invoked by your group. Protector works until it is demolished by the opposing group, so I'm hoping you'll summon several monsters first, and then bring the Protector.");
                }
            } else if (status == 66) {
                cm.sendNext("Lastly, while in the Monster Carnival, #byou can not use items / recovery potions that you carry around with you. #kMeanwhile, the monsters let these items fall for good. when, and when you #bget them, the item will immediately activate#k. That's why it's important to know when to get these items.");
                cm.dispose();
            } else if (status == 77) {
                var allDone;

                if (selection == 0) {
                    allDone = refineItems(0); // minerals
                } else if (selection == 1) {
                    allDone = refineItems(1); // jewels
                } else if (selection == 2 && refineCrystals) {
                    allDone = refineItems(2); // crystals
                } else if (selection == 2 && !refineCrystals || selection == 3) {
                    allDone = refineRockItems(); // moon/star rock
                }

                if(allDone) {
                    cm.sendOk("Done. Thanks for showing up~.");
                } else {
                    cm.sendOk("Done. Be aware some of the items #rcould not be synthetized#k because either you have a lack of space on your ETC inventory or there's not enough mesos to cover the fee.");
                }
                cm.dispose();
            }
        }
    }
}

function getRefineFee(fee) {
    return ((feeMultiplier * fee) | 0);
}

function isRefineTarget(refineType, refineItemid) {
    if(refineType == 0) { //mineral refine
        return refineItemid >= 4010000 && refineItemid <= 4010007 && !(refineItemid == 4010007 && !refineSpecials);
    } else if(refineType == 1) { //jewel refine
        return refineItemid >= 4020000 && refineItemid <= 4020008 && !(refineItemid == 4020008 && !refineSpecials);
    } else if(refineType == 2) { //crystal refine
        return refineItemid >= 4004000 && refineItemid <= 4004004 && !(refineItemid == 4004004 && !refineSpecials);
    }
    
    return false;
}

function getRockRefineTarget(refineItemid) {
    if(refineItemid >= 4011000 && refineItemid <= 4011006) {
        return 0;
    } else if(refineItemid >= 4021000 && refineItemid <= 4021008) {
        return 1;
    }
    
    return -1;
}

function refineItems(refineType) {
    var allDone = true;
    
    var refineFees = [[300,300,300,500,500,500,800,270],[500,500,500,500,500,500,500,1000,3000],[5000,5000,5000,5000,1000000]];
    var itemCount = {};
    
    var iter = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();

        if(isRefineTarget(refineType, itemid)) {
            var ic = itemCount[itemid];
            
            if(ic != undefined) {
                itemCount[itemid] += it.getQuantity();
            } else {
                itemCount[itemid] = it.getQuantity();
            }
        }
    }
    
    for(var key in itemCount) {
        var itemqty = itemCount[key];
        var itemid = parseInt(key);
        
        var refineQty = ((itemqty / 10) | 0);
        if(refineQty <= 0) continue;
        
        while(true) {
            itemqty = refineQty * 10;
        
            var fee = getRefineFee(refineFees[refineType][(itemid % 100) | 0] * refineQty);
            if(cm.canHold(itemid + 1000, refineQty, itemid, itemqty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);
                cm.gainItem(itemid, -itemqty);
                cm.gainItem(itemid + (itemid != 4010007 ? 1000 : 1001), refineQty);
                
                break;
            } else if(refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }
    
    return allDone;
}

function refineRockItems() {
    var allDone = true;
    var minItems = [[0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0, 0, 0]];
    var minRocks = [2147483647, 2147483647];
    
    var rockItems = [4011007, 4021009];
    var rockFees = [10000, 15000];

    var iter = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();
        var rockRefine = getRockRefineTarget(itemid);
        if(rockRefine >= 0) {
            var rockItem = ((itemid % 100) | 0);
            var itemqty = it.getQuantity();
            
            minItems[rockRefine][rockItem] += itemqty;
        }
    }
    
    for(var i = 0; i < minRocks.length; i++) {
        for(var j = 0; j < minItems[i].length; j++) {
            if(minRocks[i] > minItems[i][j]) {
                minRocks[i] = minItems[i][j];
            }
        }
        if(minRocks[i] <= 0 || minRocks[i] == 2147483647) continue;
        
        var refineQty = minRocks[i];
        while(true) {
            var fee = getRefineFee(rockFees[i] * refineQty);
            if(cm.canHold(rockItems[i], refineQty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);

                var j;
                if(i == 0) {
                    for(j = 4011000; j < 4011007; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                } else {
                    for(j = 4021000; j < 4021009; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                }
                
                break;
            } else if(refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }
    
    return allDone;
}
