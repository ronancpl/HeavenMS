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
/* Author: Xterminator
	NPC Name: 		Cloy
	Map(s): 		Victoria Road : Henesys Park (100000200)
	Description: 		Pet Master
 */
var status = -2;
var sel;

function start() {
    status = -2
    action(1, 0, 0);
}

function action(mode, type, selection) {
    
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
            
        if(status == -1) {
            cm.sendNext("Hmm... are you raising one of my kids by any chance? I perfected a spell that uses Water of Life to blow life into a doll. People call it the #bPet#k. If you have one with you, feel free to ask me questions.");
        }
        else if (status == 0)
            cm.sendSimple("What do you want to know more of?#b\r\n#L0#Tell me more about Pets.#l\r\n#L1#How do I raise Pets?#l\r\n#L2#Do Pets die too?#l\r\n#L3#What are the commands for Brown and Black Kitty?#l\r\n#L4#What are the commands for Brown Puppy?#l\r\n#L5#What are the commands for Pink and White Bunny?#l\r\n#L6#What are the commands for Mini Kargo?#l\r\n#L7#What are the commands for Rudolph and Dasher?#l\r\n#L8#What are the commands for Black Pig?#l\r\n#L9#What are the commands for Panda?#l\r\n#L10#What are the commands for Husky?#l\r\n#L11#What are the commands for Dino Boy and Dino Girl?#l\r\n#L12#What are the commands for Monkey?#l\r\n#L13#What are the commands for Turkey?#l\r\n#L14#What are the commands for White Tiger?#l\r\n#L15#What are the commands for Penguin?#l\r\n#L16#What are the commands for Golden Pig?#l\r\n#L17#What are the commands for Robot?#l\r\n#L18#What are the commands for Mini Yeti?#l\r\n#L19#What are the commands for Jr. Balrog?#l\r\n#L20#What are the commands for Baby Dragon?#l\r\n#L21#What are the commands for Green/Red/Blue Dragon?#l\r\n#L22#What are the commands for Black Dragon?#l\r\n#L23#What are the commands for Jr. Reaper?#l\r\n#L24#What are the commands for Porcupine?#l\r\n#L25#What are the commands for Snowman?#l\r\n#L26#What are the commands for Skunk?#l\r\n#L27#Please teach me about transferring pet ability points.#l");
        else if (status == 1) {
            sel = selection;
            if (selection == 0) {
                status = 3;
                cm.sendNext("So you want to know more about Pets. Long ago I made a doll, sprayed Water of Life on it, and cast spell on it to create a magical animal. I know it sounds unbelievable, but it's a doll that became an actual living thing. They understand and follow people very well.");
            } else if (selection == 1) {
                status = 6;
                cm.sendNext("Depending on the command you give, pets can love it, hate, and display other kinds of reactions to it. If you give the pet a command and it follows you well, your intimacy goes up. Double click on the pet and you can check the intimacy, level, fullness and etc...");
            } else if (selection == 2) {
                status = 11;
                cm.sendNext("Dying... well, they aren't technically ALIVE per se, so I don't know if dying is the right term to use. They are dolls with my magical power and the power of Water of Life to become a live object. Of course while it's alive, it's just like a live animal...");
            } else if (selection == 3)
                cm.sendNext("These are the commands for #rBrown Kitty and Black Kitty#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bcutie#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
            else if (selection == 4)
                cm.sendNext("These are the commands for #rBrown Puppy#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bpee#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
            else if (selection == 5)
                cm.sendNext("These are the commands for #rPink Bunny and White Bunny#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bhug#k (Level 10 ~ 30)\r\n#bsleep, sleepy, gotobed#k (Level 20 ~ 30)");
            else if (selection == 6)
                cm.sendNext("These are the commands for #rMini Kargo#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bpee#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bthelook, charisma#k (Level 10 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#bgoodboy, goodgirl#k (Level 20 ~ 30)");
            else if (selection == 7)
                cm.sendNext("These are the commands for #rRudolph and Dasher#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#bmerryxmas, merrychristmas#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 11 ~ 30)\r\n#blonely, alone#k (Level 11 ~ 30)\r\n#bcutie#k (Level 11 ~ 30)\r\n#bmush, go#k (Level 21 ~ 30)");
            else if (selection == 8)
                cm.sendNext("These are the commands for #rBlack Pig#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1~30)\r\n#bhand#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bsmile#k (Level 10 ~ 30)\r\n#bthelook, charisma#k (Level 20 ~ 30)");
            else if (selection == 9)
                cm.sendNext("These are the commands for #rPanda#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bchill, relax#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bletsplay#k (Level 10 ~ 30)\r\n#bmeh, bleh#k (Level 10 ~ 30)\r\n#bsleep#k (Level 20 ~ 30)");
            else if (selection == 10)
                cm.sendNext("These are the commands for #rHusky#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (Level 1 ~ 30)\r\n#bhand#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
            else if (selection == 11)
                cm.sendNext("These are the commands for #rDino Boy and Dino Girl#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bsmile, laugh#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bcutie#k (Level 10 ~ 30)\r\n#bsleep, nap, sleepy#k (Level 20 ~ 30)");
            else if (selection == 12)
                cm.sendNext("These are the commands for #rMonkey#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#brest#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpee#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bplay#k (Level 10 ~ 30)\r\n#bmelong#k (Level 10 ~ 30)\r\n#bsleep, gotobed, sleepy#k (Level 20 ~ 30)");
            else if (selection == 13)
                cm.sendNext("These are the commands for #rTurkey#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bno, rudeboy, mischief#k (Level 1 ~ 30)\r\n#bstupid#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#btalk, chat, gobble#k (Level 10 ~ 30)\r\n#byes, goodboy#k (Level 10 ~ 30)\r\n#bsleepy, birdnap, doze#k (Level 20 ~ 30)\r\n#bbirdeye, thanksgiving, fly, friedbird, imhungry#k (Level 30)");
            else if (selection == 14)
                cm.sendNext("These are the commands for #rWhite Tiger#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#brest, chill#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bactsad, sadlook#k (Level 10 ~ 30)\r\n#bwait#k (Level 20 ~ 30)");
            else if (selection == 15)
                cm.sendNext("These are the commands for #rPenguin#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bhug, hugme#k (Level 10 ~ 30)\r\n#bwing, hand#k (Level 10 ~ 30)\r\n#bsleep#k (Level 20 ~ 30)\r\n#bkiss, smooch, muah#k (Level 20 ~ 30)\r\n#bfly#k (Level 20 ~ 30)\r\n#bcute, adorable#k (Level 20 ~ 30)");
            else if (selection == 16)
                cm.sendNext("These are the commands for #rGolden Pig#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 11 ~ 30)\r\n#bloveme, hugme#k (Level 11 ~ 30)\r\n#bsleep, sleepy, gotobed#k (Level 21 ~ 30)\r\n#bignore / impressed / outofhere#k (Level 21 ~ 30)\r\n#broll, showmethemoney#k (Level 21 ~ 30)");
            else if (selection == 17)
                cm.sendNext("These are the commands for #rRobot#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#battack, charge#k (Level 1 ~ 30)\r\n#biloveyou#k (Level 1 ~ 30)\r\n#bgood, thelook, charisma#k (Level 11 ~ 30)\r\n#bspeack, talk, chat, say#k (Level 11 ~ 30)\r\n#bdisguise, change, transform#k (Level 11 ~ 30)");
            else if (selection == 18)
                cm.sendNext("These are the commands for #rMini Yeti#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bdance, boogie, shakeit#k (Level 1 ~ 30)\r\n#bcute, cutie, pretty, adorable#k (Level 1 ~ 30)\r\n#biloveyou, likeyou, mylove#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 11 ~ 30)\r\n#bsleep, nap, sleepy, gotobed#k (Level 11 ~ 30)");
            else if (selection == 19)
                cm.sendNext("These are the commands for #rJr. Balrog#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bliedown#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#biloveyou|mylove|likeyou#k (Level 1 ~ 30)\r\n#bcute|cutie|pretty|adorable#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bsmirk|crooked|laugh#k (Level 1 ~ 30)\r\n#bmelong#k (Level 11 ~ 30)\r\n#bgood|thelook|charisma#k (Level 11 ~ 30)\r\n#bspeak|talk|chat|say#k (Level 11 ~ 30)\r\n#bsleep|nap|sleepy#k (Level 11 ~ 30)\r\n#bgas#k (Level 21 ~ 30)");
            else if (selection == 20)
                cm.sendNext("These are the commands for #rBaby Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#biloveyou|loveyou#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 1 ~ 30)\r\n#bcutie#k (Level 11 ~ 30)\r\n#btalk|chat|say#k (Level 11 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 11 ~ 30)");
            else if (selection == 21)
                cm.sendNext("These are the commands for #rGreen/Red/Blue Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 15 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 15 ~ 30)\r\n#biloveyou|loveyou#k (Level 15 ~ 30)\r\n#bpoop#k (Level 15 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 15 ~ 30)\r\n#btalk|chat|say#k (Level 15 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 15 ~ 30)\r\n#bchange#k (Level 21 ~ 30)");
            else if (selection == 22)
                cm.sendNext("These are the commands for #rBlack Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 15 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 15 ~ 30)\r\n#biloveyou|loveyou#k (Level 15 ~ 30)\r\n#bpoop#k (Level 15 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 15 ~ 30)\r\n#btalk|chat|say#k (Level 15 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 15 ~ 30)\r\n#bcutie, change#k (Level 21 ~ 30)");
            else if (selection == 23)
                cm.sendNext("These are the commands for #rJr. Reaper#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#bplaydead, poop#k (Level 1 ~ 30)\r\n#btalk|chat|say#k (Level 1 ~ 30)\r\n#biloveyou, hug#k (Level 1 ~ 30)\r\n#bsmellmyfeet, rockout, boo#k (Level 1 ~ 30)\r\n#btrickortreat#k (Level 1 ~ 30)\r\n#bmonstermash#k (Level 1 ~ 30)");
            else if (selection == 24)
                cm.sendNext("These are the commands for #rPorcupine#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#biloveyou|hug|goodboy#k (Level 1 ~ 30)\r\n#btalk|chat|say#k (Level 1 ~ 30)\r\n#bcushion|sleep|knit|poop#k (Level 1 ~ 30)\r\n#bcomb|beach#k (Level 10 ~ 30)\r\n#btreeninja#k (Level 20 ~ 30)\r\n#bdart#k (Level 20 ~ 30)");
            else if (selection == 25)
                cm.sendNext("These are the commands for #rSnowman#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (Level 1 ~ 30)\r\n#bloveyou, mylove, ilikeyou#k (Level 1 ~ 30)\r\n#bmerrychristmas#k (Level 1 ~ 30)\r\n#bcutie, adorable, cute, pretty#k (Level 1 ~ 30)\r\n#bcomb, beach/bad, no, badgirl, badboy#k (Level 1 ~ 30)\r\n#btalk, chat, say/sleep, sleepy, gotobed#k (Level 10 ~ 30)\r\n#bchang#k (Level 20 ~ 30)");
            else if (selection == 26)
                cm.sendNext("These are the commands for #rSkunk#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bsit#k (Level 1 ~ 30)\r\n#bbad/no/badgirl/badboy#k (Level 1 ~ 30)\r\n#brestandrelax, poop#k (Level 1 ~ 30)\r\n#btalk/chat/say, iloveyou#k (Level 1 ~ 30)\r\n#bsnuggle/hug, sleep, goodboy#k (Level 1 ~ 30)\r\n#bfatty, blind, badbreath#k (Level 10 ~ 30)\r\n#bsuitup, bringthefunk#k (Level 20 ~ 30)");
            else if (selection == 27) {
                status = 14;
                cm.sendNext("In order to transfer the pet ability points, closeness and level, Pet AP Reset Scroll is required. If you take this\r\nscroll to Mar the Fairy in Ellinia, she will transfer the level and closeness of the pet to another one. I am especially giving it to you because I can feel your heart for your pet. However, I can't give this out for free. I can give you this book for 250,000 mesos. Oh, I almost forgot! Even if you have this book, it is no use if you do not have a new pet to transfer the Ability points.");
            }
            if(selection > 2 && selection < 27)
                cm.dispose();
        } else if (status == 2) {
            if(sel == 0)
                cm.sendNextPrev("But Water of Life only comes out little at the very bottom of the World Tree, so I can't give him too much time in life... I know, it's very unfortunate... but even if it becomes a doll again I can always bring life back into it so be good to it while you're with it.");
            else if (sel == 1)
                cm.sendNextPrev("Talk to the pet, pay attention to it and its intimacy level will go up and eventually his overall level will go up too. As the intimacy level rises, the pet's overall level will rise soon after. As the overall level rises, one day the pet may even talk like a person a little bit, so try hard raising it. Of course it won't be easy doing so...");
            else if (sel == 2)
                cm.sendNextPrev("After some time... that's correct, they stop moving. They just turn back to being a doll, after the effect of magic dies down and Water of Life dries out. But that doesn't mean it's stopped forever, because once you pour Water of Life over, it's going to be back alive.");
            else if (sel == 27)
                cm.sendYesNo("250,000 mesos will be deducted. Do you really want to buy?");
        } else if (status == 3) {
            if (sel == 0)
                cm.sendNextPrev("Oh yeah, they'll react when you give them special commands. You can scold them, love them... it all\r\ndepends on how you take care of them. They are afraid to leave their masters so be nice to them, show them love. They can get sad and lonely fast...");
            else if (sel == 1){
                cm.sendNextPrev("It may be a live doll but they also have life so they can feel the hunger too. #bFullness#k shows the level of hunger the pet's in. 100 is the max, and the lower it gets, it means that the pet is getting hungrier. After a while, it won't even follow your command and be on the offensive, so watch out over that.");
                return;
            }else if (sel == 2)
                cm.sendNextPrev("Even if it someday moves again, it's sad to see them stop altogether. Please be nice to them while they are alive and moving. Feed them well, too. Isn't it nice to know that there's something alive that follows and listens to only you?");
            else if (sel == 27){
                if (cm.getMeso() < 250000 || !cm.canHold(4160011))
                    cm.sendOk("Please check if your inventory has empty slot or you don't have enough mesos.");
                else {
                    cm.gainMeso(-250000);
                    cm.gainItem(4160011, 1);
                }
                cm.dispose();
            }
        } else if (status == 4){
            if(sel != 1)
                cm.dispose();
            cm.sendNextPrev("Oh yes! Pets can't eat the normal human food. Instead my disciple #bDoofus#k sells #bPet Food#k at the Henesys Market so if you need food for your pet, find Henesys. It'll be a good idea to buy the food in advance and feed the pet before it gets really hungry.");
        } else if (status == 5)
            cm.sendNextPrev("Oh, and if you don't feed the pet for a long period of time, it goes back home by itself. You can take it out of its home and feed it but it's not really good for the pet's health, so try feeding him on a regular basis so it doesn't go down to that level, alright? I think this will do.");
        else
            cm.dispose();
    }
}