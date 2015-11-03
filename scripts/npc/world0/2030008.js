/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Adobis
 * 
 * El Nath: The Door to Zakum (211042300)
 * 
 * Zakum Quest NPC 
 
 * Custom Quest 100200 = whether you can do Zakum
 * Custom Quest 100201 = Collecting Gold Teeth <- indicates it's been started
 * Custom Quest 100203 = Collecting Gold Teeth <- indicates it's finished
 * 4031061 = Piece of Fire Ore - stage 1 reward
 * 4031062 = Breath of Fire    - stage 2 reward
 * 4001017 = Eye of Fire       - stage 3 reward
 * 4000082 = Zombie's Gold Tooth (stage 3 req)
*/

var status;
var mapId = 211042300;
var stage;
var teethmode;
var minLevel = 50;

function start() {
    if (cm.getPlayer().getLevel() >= minLevel) {
        if (cm.isQuestCompleted(100200) && !cm.isQuestStarted(100200)) {
            cm.startQuest(100200);
            cm.sendOk("You want to be permitted to do the Zakum Dungeon Quest?  Well, I, #bAdobis#k... judge you to be suitable.  You should be safe roaming around the dungeon.  Just be careful...");
            cm.dispose();
            return;
        }
        else if (cm.isQuestStarted(100201)) {
            teethmode = 1;
            cm.sendNext("Have you got the items I asked for?  This ain't no charity.");
        }
        else
            cm.sendSimple("Beware, for the power of olde has not been forgotten... #b\r\n#L0#Enter the Unknown Dead Mine (Stage 1)#l\r\n#L1#Face the Breath of Lava (Stage 2)#l\r\n#L2#Forging the Eyes of Fire (Stage 3)#l");
        if (cm.isQuestCompleted(100201))
            teethmode = 2;
    }
    else {
        cm.sendOk("Please come back to me when you've become stronger.  I've seen a few adventurers in my day, and you're far too weak to complete my tasks.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (teethmode == 1) {
                if (cm.haveItem(4031061,1) && cm.haveItem(4031062,1) && cm.haveItem(4000082,30)) {
                    cm.gainItem(4031061,-1);
                    cm.gainItem(4031062,-1);
                    cm.gainItem(4000082,-30);
                    cm.gainItem(4001017,5);
                    cm.sendNext("Thank you for the teeth!  Next time you see me, I'll be blinging harder than #rJaws#k! Goodbye and good luck!");
                    cm.completeQuest(100201);
                    cm.completeQuest(100200);
                    cm.dispose();
                }
                else {
                    cm.sendNext("You shtill didn't get me my teef! Howsh a man shupposhed to conshentrate wifout teef?");
                    cm.dispose();
                }
                return;
            }
            if (selection == 0) { //ZPQ
                if (cm.getParty() == null) { //no party
                    cm.sendNext("Please talk to me again when you have formed a party.");
                    cm.dispose();
                }
                else if (!cm.isLeader()) { //not party leader
                    cm.sendNext("Please have the leader of your party speak with me.");
                    cm.dispose();
                }
                else {
                    //TODO: add zakum variable to characters, check that instead; less hassle
                    var party = cm.getParty().getMembers();
                    mapId = cm.getPlayer().getMapId();
                    var next = true;
                    for (var i = 0; i < party.size(); i++)
                        if ((party.get(i).getLevel() < 50) || (party.get(i).getMapid() != mapId))
                            next = false;
                    if (next) {
                        //all requirements met, make an instance and start it up
                        //cm.startPQ("ZakumPQ");
                        var em = cm.getEventManager("ZakumPQ");
                        if (em == null) {
                            cm.sendOk("This trial is currently under construction.");
                        } else {
                            em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                            party = cm.getPlayer().getEventInstance().getPlayers();
                            cm.removeFromParty(4001015, party);
                            cm.removeFromParty(4001018, party);
                            cm.removeFromParty(4001016, party);
                        }
                        cm.dispose();
                    }
                    else {
                        cm.sendNext("Please make sure all of your members are qualified to begin my trials...");
                        cm.dispose();
                    }
                }
            }
            else if (selection == 1) { //Zakum Jump Quest
                stage = 1;
                if (cm.haveItem(4031061) && !cm.haveItem(4031062))
                    cm.sendYesNo("Would you like to attempt the #bBreath of Lava#k?  If you fail, there is a very real chance you will die.");
                else {
                    if (cm.haveItem(4031062))
                        cm.sendNext("You've already got the #bBreath of Lava#k, you don't need to do this stage.");
                    else
                        cm.sendNext("Please complete the earlier trials first.");
                    cm.dispose();
                }
            }
            else if (selection == 2) { //Golden Tooth Collection
                stage = 2;
                if (teethmode == 2 && cm.haveItem(4031061) && cm.haveItem(4031062))
                    cm.sendYesNo("If you want more #bEyes of Fire#k, you need to bring me the same #b30 Zombie's Lost Gold Tooth#k.  Turns out gold dentures don't last long, and I need a new one.\r\nDo you have those teeth for me?");
                else if (cm.haveItem(4031061) && cm.haveItem(4031062))
                    cm.sendYesNo("Okay, you've completed the earlier trials.  Now, with a little hard work I can get you the #bseeds of Zakum#k necessary to enter combat.  But first, my teeths are not as good as they used to be.  You ever seen a dentist in Maple Story?  Well, I heard the Miner Zombies have gold teeth.  I'd like you to collect #b30 Zombie's Lost Gold Tooth#k so I can build myself some dentures.  Then I'll be able to get you the items you desire.\r\nRequired:\r\n#i4000082##b x 30");
                else {
                    cm.sendNext("Please complete the earlier trials before attempting this one.");
                    cm.dispose();
                }
            }
        }
        else if (status == 2) {
            if (stage == 1) {
                cm.warp(280020000); // Breath of Lava I
                cm.dispose();
            }
            if (stage == 2) {
                if (teethmode == 2) {
                    if (cm.haveItem(4031061,1) && cm.haveItem(4031062,1) && cm.haveItem(4000082,30)) { // take away items, give eyes of fire, complete quest
                        cm.gainItem(4031061,-1);
                        cm.gainItem(4031062,-1);
                        cm.gainItem(4000082,-30);
                        cm.gainItem(4001017,5);
                        cm.sendNext("Thank you for the teeth!  Next time you see me, I'll be blinging harder than #rJaws#k!  Goodbye and good luck!");
                        cm.completeQuest(100201);
                        cm.completeQuest(100200);
                        cm.dispose();
                    }
                    else {
                        cm.sendNext("You don't have any teeth yet!  Don't try to pull a fast one on me.");
                        cm.dispose();
                    }
                }
                else {
                    cm.startQuest(100201);
                    cm.dispose();
                }
            }
        }
    }
}
