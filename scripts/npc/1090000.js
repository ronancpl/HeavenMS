/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Kyrin
	Pirate Job Advancement
	
	Custom Quest 100009, 100011
*/

status = -1;
actionx = {"1stJob" : false, "2ndjob" : false, "2ndjobT" : false, "3thJobI" : false, "3thJobC" : false};
job = 510;

spawnPnpc = false;
spawnPnpcFee = 7000000;
jobType = 5;

var advQuest = 0;
function start() {
    if (cm.isQuestStarted(6330)) {
        if (cm.getEventInstance() != null) {    // missing script for skill test found thanks to Lost(tm)
            advQuest = 5;                       // string visibility thanks to iPunchEm & Glvelturall
            cm.sendNext("Not bad at all. Let's discuss this outside!");
            cm.setQuestProgress(6330, 0, 1);
        } else if (cm.getQuestProgress(6330, 0) == 0) {
            advQuest = 1;
            cm.sendNext("You're ready, right? Now try to withstand my attacks for 2 minutes. I won't go easy on you. Good luck, because you will need it.");
        } else {
            advQuest = 3;
            cm.teachSkill(5121003, 0, 10, -1);
            cm.forceCompleteQuest(6330);
            
            cm.sendNext("Congratulations. You have managed to pass my test. I'll teach you a new skill called \"Super Transformation\".\r\n\r\n  #s5121003#    #b#q5121003##k");
        }
    } else if (cm.isQuestStarted(6370)) {
        if (cm.getEventInstance() != null) {
            advQuest = 6;
            cm.sendNext("Not bad at all. Let's discuss this outside!");
            cm.setQuestProgress(6370, 0, 1);
        } else if (cm.getQuestProgress(6370, 0) == 0) {
            advQuest = 2;
            cm.sendNext("You're ready, right? Now try to withstand my attacks for 2 minutes. I won't go easy on you. Good luck, because you will need it.");
        } else {
            advQuest = 4;
            cm.teachSkill(5221006, 0, 10, -1);
            cm.forceCompleteQuest(6370);
            
            cm.sendNext("Congratulations. You have managed to pass my test. I'll teach you a new skill called \"Battleship\".\r\n\r\n  #s5221006#    #b#q5221006##k");
        }
    } else if (parseInt(cm.getJobId() / 100) == jobType && cm.canSpawnPlayerNpc(Packages.constants.GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;
        
        var sendStr = "You have walked a long way to reach the power, wisdom and courage you hold today, haven't you? What do you say about having right now #ra NPC on the Hall of Fame holding the current image of your character#k? Do you like it?";
        if(spawnPnpcFee > 0) {
            sendStr += " I can do it for you, for the fee of #b " + cm.numberWithCommas(spawnPnpcFee) + " mesos.#k";
        }
        
        cm.sendYesNo(sendStr);
    } else {
        if (cm.getJobId() == 0) {
            actionx["1stJob"] = true;
            if (cm.getLevel() >= 10 && cm.canGetFirstJob(jobType))
                cm.sendNext("Want to be a pirate? There are some standards to meet. because we can't just accept EVERYONE in... #bYour level should be at least 10#k. Let's see.");
            else {
                cm.sendOk("Train a bit more until you reach #blevel 10, " + cm.getFirstJobStatRequirement(jobType) + "#k and I can show you the way of the #rPirate#k.");
                cm.dispose();
            }
        } else if (cm.getLevel() >= 30 && cm.getJobId() == 500) {
            actionx["2ndJob"] = true;
            if (cm.isQuestCompleted(2191) || cm.isQuestCompleted(2192))
                cm.sendNext("I see you have done well. I will allow you to take the next step on your long road.");
            else
                cm.sendNext("The progress you have made is astonishing.");
        } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 5 && !cm.getPlayer().gotPartyQuestItem("JBP"))){
            actionx["3thJobI"] = true;
            cm.sendNext("There you are. A few days ago, #b#p2020013##k of Ossyria talked to me about you. I see that you are interested in making the leap to the world of the third job advancement for pirates. To archieve that goal, I will have to test your strength in order to see whether you are worthy of the advancement. There is an opening in the middle of a cave on Victoria Island, where it'll lead you to a secret passage. Once inside, you'll face a clone of myself. Your task is to defeat him and bring #b#t4031059##k back with you.");
        } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)){
            cm.sendNext("Please, bring me the #b#t4031059##k.");
            cm.dispose();
        } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")){
            actionx["3thJobC"] = true;
            cm.sendNext("Nice work. You have defeated my clone and brought #b#t4031059##k back safely. You have now proven yourself worthy of the 3rd job advancement from the physical standpoint. Now you should give this necklace to #b#p2020013##k in Ossyria to take on the second part of the test. Good luck. You'll need it.");
        } else {
            cm.sendOk("You have chosen wisely.");
            cm.dispose();
        }
    }
}

function action(mode, type, selection) {
    status++;
    if (mode == 0 && type != 1)
        status -= 2;
    if (status == -1){
        start();
        return;
    } else {
        if (advQuest > 0) {
            if (advQuest < 3) {
                var em = cm.getEventManager(advQuest == 1 ? "4jship" : "4jsuper");
                if(!em.startInstance(cm.getPlayer())) {
                    cm.sendOk("Someone is already challenging the test. Please try again later.");
                }
            } else if (advQuest < 5) {
                if (advQuest == 3) {
                    cm.sendOk("It is similar to that of 'Transformation', but it's much more powerful than that. Keep training, and hope to see you around.");
                } else {
                    cm.sendOk("Unlike most of the other skills you used as a Pirate, this one definitely is different. You can actually ride the 'Battleship' and attack enemies with it. Your DEF level will increase for the time you're on board, so that'll help you tremendously in combat situations. May you become the best Gunslinger out there...");
                }
            } else {
                cm.warp(120000101);
            }
            
            cm.dispose();
        } else if(spawnPnpc) {
            if(mode > 0) {
                if(cm.getMeso() < spawnPnpcFee) {
                    cm.sendOk("Sorry, you don't have enough mesos to purchase your place on the Hall of Fame.");
                    cm.dispose();
                    return;
                }
                
                if(Packages.server.life.MaplePlayerNPC.spawnPlayerNPC(Packages.constants.GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
                    cm.sendOk("There you go! Hope you will like it.");
                    cm.gainMeso(-spawnPnpcFee);
                } else {
                    cm.sendOk("Sorry, the Hall of Fame is currently full...");
                }
            }
            
            cm.dispose();
            return;
        } else {
            if (mode != 1 || status == 7 && type != 1 || (actionx["1stJob"] && status == 4) || (cm.haveItem(4031008) && status == 2) || (actionx["3thJobI"] && status == 1)){
                if (mode == 0 && status == 2 && type == 1)
                    cm.sendOk("You know there is no other choice...");
                if (!(mode == 0 && type != 1)){
                    cm.dispose();
                    return;
                }
            }
        }
    }
    
    if (actionx["1stJob"]){
        if (status == 0)
            cm.sendYesNo("Oh...! You look like someone that can definitely be a part of us... all you need is a little slang, and... yeah... so, what do you think? Wanna be the Pirate?");
        else if (status == 1){
            if (cm.canHold(2070000) && cm.canHold(1472061)){
                if (cm.getJobId() == 0){
                    cm.changeJobById(500);
                    cm.gainItem(1492000, 1);
                    cm.gainItem(1482000, 1);
                    cm.gainItem(2330000, 1000);
                    cm.resetStats();
                }
                cm.sendNext("Alright, from here out, you are a part of us! You'll be living the life of a wanderer at ..., but just be patient as soon, you'll be living the high life. Alright, it ain't much, but I'll give you some of my abilities... HAAAHHH!!!");
            } else {
                cm.sendNext("Make some room in your inventory and talk back to me.");
                cm.dispose();
            }
        } else if (status == 2) 
            cm.sendNextPrev("You've gotten much stronger now. Plus every single one of your inventories have added slots. A whole row, to be exact. Go see for it yourself. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.");
        else if (status == 3)
            cm.sendNextPrev("Now a reminder. Once you have chosen, you cannot change up your mind and try to pick another path. Go now, and live as a proud Pirate.");
        else
            cm.dispose();
    } else if(actionx["2ndJob"]){
        if (status == 0){
            if (cm.isQuestCompleted(2191) || cm.isQuestCompleted(2192))
                cm.sendSimple("Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Brawler is all about.\r\n#L1#Please explain to me what being the Gunslinger is all about.\r\n#L3#I'll choose my occupation!");
            else
                cm.sendNext("Good decision. You look strong, but I need to see if you really are strong enough to pass the test, it's not a difficult test, so you'll do just fine.");
        } else if (status == 1){
            if (!cm.isQuestCompleted(2191) && !cm.isQuestCompleted(2192)){
                // Pirate works differently from the other jobs. It warps you directly in.
                actionx["2ndJobT"] = true;
                cm.sendYesNo("Would you like to take the test now?");
            } else {
                if (selection < 3) {
                    if(selection == 0) {    //brawler
                        cm.sendNext("Pirates that master #rKnuckles#k.\r\n\r\n#bBrawlers#k are melee, close-ranged fist fighters who deal lots of damage and have high HP. Armed with #rCorkscrew Blow#k, one can deal massive damage to multiple targets at once. #rOak Barrel#k permits one to scout or disguise themselves in middle of difficult fights, enabling a possible escaping route in front of danger.");
                    } else if(selection == 1) {    //gunslinger
                        cm.sendNext("Pirates that master #rGuns#k.\r\n\r\n#bGunslingers#k are faster and ranged attackers. With the #rWings#k skill, Gunslingers can hover in the air, allowing for a longer, more sustained jump than a regular jump. #rBlank Shot#k allows to deal Stun status to multiple targets nearby.");
                    }
                    
                    status -= 2;
                } else
                    cm.sendNextPrev("You have a long road ahead of you still, but being a pirate will help you get there. Just keep that in mind and you will do fine.");
            }
        } else if (status == 2){
            if (actionx["2ndJobT"]) {
                var map = 0;
				if(cm.isQuestStarted(2191))
					map = 108000502;
				else
					map = 108000501;
                if(cm.getPlayerCount(map) > 0) {
					cm.sendOk("All the training maps are currently in use. Please try again later.");
					cm.dispose();
				} else {
					cm.warp(map);
					cm.dispose();
					return;
                }
            } else {
                if(cm.isQuestCompleted(2191) && cm.isQuestCompleted(2192))
                        job = (Math.random() < 0.5) ? 510 : 520;
                else if(cm.isQuestCompleted(2191))
                        job = 510;
                else if(cm.isQuestCompleted(2192))
                        job = 520;
					
                cm.sendYesNo("So you want to make the second job advancement as the " + (job == 510 ? "#bBrawler#k" : "#bGunslinger#k") + "? You know you won't be able to choose a different job for the 2nd job advancement once you make your decision here, right?");
            }
        } else if (status == 3){
            if (cm.haveItem(4031012))
                cm.gainItem(4031012, -1);
            
            if(job == 510) cm.sendNext("From here on out, you are a #bBrawler#k. Brawlers rule the world with the power of their bare fists...which means they need to train their body more than others. If you have any trouble training, I'll be more than happy to help.");
            else cm.sendNext("From here on out, you are a #bGunslinger#k. Gunslingers are notable for their long-range attacks with sniper-like accuracy and of course, using Guns as their primary weapon. You should continue training to truly master your skills. If you are having trouble training, I'll be here to help.");
            
            if (cm.getJobId() != job)
                cm.changeJobById(job);
        } else if (status == 4)
            cm.sendNextPrev("I have just given you a book that gives you the list of skills you can acquire as a " + (job == 510 ? "brawler" : "gunslinger") + ". Also your etc inventory has expanded by adding another row to it. Your max HP and MP have increased, too. Go check and see for it yourself.");
        else if (status == 5)
            cm.sendNextPrev("I have also given you a little bit of #bSP#k. Open the #bSkill Menu#k located at the bottom left corner. you'll be able to boost up the newer acquired 2nd level skills. A word of warning, though. You can't boost them up all at once. Some of the skills are only available after you have learned other skills. Make sure yo remember that.");
        else if (status == 6)
            cm.sendNextPrev((job == 510 ? "Brawlers" : "Gunslingers") + " need to be strong. But remember that you can't abuse that power and use it on a weakling. Please use your enormous power the right way, because... for you to use that the right way, that is much harden than just getting stronger. Please find me after you have advanced much further. I'll be waiting for you.");
    } else if (actionx["3thJobI"]){
        if (status == 0){
            if (cm.getPlayer().gotPartyQuestItem("JB3")){
                cm.getPlayer().removePartyQuestItem("JB3");
                cm.getPlayer().setPartyQuestItemObtained("JBP");
            }
            cm.sendNextPrev("Since he is a clone of myself, you can expect a tough battle ahead. He uses a number of special attacking skills unlike any you have ever seen, and it is your task to successfully take him one on one. There is a time limit in the secret passage, so it is crucial that you defeat him within the time limit. I wish you the best of luck, and I hope you bring the #b#t4031059##k with you.");
        }
    } else if (actionx["3thJobC"]){
        cm.getPlayer().removePartyQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}