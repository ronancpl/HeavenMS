/*
 * @Author - Sparrow
 * @NPC - 1012008 - Casey the Game Master
 * @Map - 100000203 - Henesys Game Park
 */

var status;
var current;
var omok =       [4080000, 4080001, 4080002, 4080003, 4080004, 4080005];
var omok1piece = [4030000, 4030000, 4030000, 4030010, 4030011, 4030011];
var omok2piece = [4030001, 4030010, 4030011, 4030001, 4030010, 4030001];
var omokamount = 99;
var text = "The set also differs based on what kind of pieces you want to use for the game. Which set would you like to make?"

function start() {
    current = 0;
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection) {
    if(mode == -1 && current > 0) {
        cm.dispose();
        return;
    } else {
        if(mode == 1) { 
            status++;
        } else {
            status--;
        }
    }

    if (status == 0) {
        cm.sendSimple("Hey, you look like you need a breather. You should be enjoying the life, just like I am. Well, if you have a couple of items, I can trade you for an item you can play minigames with. Now... what can I do for you?#b\r\n#L0#Create a minigame item#l\r\n#L1#Explain to me what the minigames are about#l#k");
      
    } else if (status == 1) {
        if (selection == 0) {
            cm.sendSimple("You want to make the minigame item? Minigames aren't something you can just go ahead and play right off the bat. For each minigame, you'll need a specific set of items. Which minigame it em do you want to make?#b\r\n#L4#Omok Set#l\r\n#L5#A Set of Match Cards#l#k");
        } else if (selection == 1) {
            cm.sendSimple("You want to learn more about the minigames? Awesome! Ask me anything. Which minigame do you want to know more about?#b\r\n#L2#Omok#l\r\n#L3#Match Cards#l#k");
        }

    } else if (status == 2) {
        if (selection == 2) {
            current = 1;
            cm.sendNext("Here are the rules for Omok, so listen carefully. Omok is a game in which you and your opponent take turns laying a piece on the table until someone finds a way to lay 5 consecutive pieces in a line, be it horizontal, diagonal, or vertical. For starters, only the ones with an #bOmok Set#k can open a game room.");
        } else if (selection == 3) {
            current = 2;
            cm.sendNext("Here are the rules for Match Cards, so listen carefully. As the name suggests, Match Cards is simply finding a matching pair among the number of cards laid on the table. When all the matching pairs are found, then the person with more matching pairs will win the game. Just like Omok, you'll need #bA set of Match Cards#k to open the game room.");

        } else if (selection == 4) {
            current = 3;
            cm.sendNext("You want to play #bOmok#k, huh? To play it, you'll need the Omok Set. Only the ones with that item can open the room for a game of Omok, and you can play this game almost anywhere except for a few places at the market place.");

        } else if (selection == 5) {
            current = 4;
            if (cm.haveItem(4030012, 15)) {
                cm.gainItem(4030012, -15);
                cm.gainItem(4080100, 1);
            } else {
                cm.sendNext("You want #bA set of Match Cards#k? Hmm...to make A set of Match Cards, you'll need some #bMonster Cards#k. Monster Card can be obtained by taking out the monsters all around the island. Collect 15 Monster Cards and you can make a set of A set of Match Cards."); //Lmfao a set of A set xD
                cm.dispose();
            }
        }

         
    } else if (status == 3) {
        if (current == 1) {
            cm.sendNextPrev("Every game of Omok will cost you #r100 mesos#k. Even if you don't have an #bOmok Set#k, you can enter the room and play. However, if you don't possess 100 mesos, then you won't be allowed to enter in the room at all. The person opening the game room also needs 100 mesos to open the room (or else there's no game). If you run out of mesos during the game, then you're automatically kicked out of the room!");
        } else if (current == 2) {
            cm.sendNextPrev("Every game of Match Cards will cost you #r100 mesos#k. Even if you don't have #bA set of Match Cards#k, you can enter the room and play. However, if you don't possess 100 mesos, then you won't be allowed to enter in the room at all. The person opening the game room also needs 100 mesos to open the room (or else there's no game). If you run out of mesos during the game, then you're automatically kicked out of the room!");

        } else if (current == 3) {
            for (var i = 0; i < omok.length; i++)
                text += "\r\n#L"+i+"##b#t"+omok[i]+"##k#l";
            cm.sendSimple(text);
        }

    } else if (status == 4) {
        if (current == 1 || current == 2) {
            cm.sendNextPrev("Enter the room, and when you're ready to play, click on #bReady#k.\r\nOnce the visitor clicks on #bReady#k, the room owner can press #bStart#k to begin the game. If an unwanted visitor walks in, and you don't want to play with that person, the room owner has the right to kick the visitor out of the room. There will be a square box with x written on the right of that person. Click on that for a cold goodbye, okay?"); //Oh yeah, because people WALK in Omok Rooms.
        }
        else if (current == 3) {
            if (cm.haveItem(omok1piece[selection], 99) && cm.haveItem(omok2piece[selection], 99) && cm.haveItem(4030009, 1)) {
                cm.gainItem(omok1piece[selection], -omokamount);
                cm.gainItem(omok2piece[selection], -omokamount);
                cm.gainItem(4030009, -1);
                cm.gainItem(omok[selection], 1);
                cm.dispose();
            } else {
                cm.sendNext("#bYou want to make #t" + omok[selection] + "##k? Hmm...get me the materials, and I can do just that. Listen carefully, the materials you need will be: #r" + omokamount + " #t" + omok1piece[selection] + "#, " + omokamount + " #t" + omok2piece[selection] + "#, 1 #t" + 4030009 + "##k. The monsters will probrably drop those every once in a while...");
                cm.dispose();
            }
        }

    } else if (status == 5) {
        if (current == 1) {
            cm.sendNextPrev("When the first fame starts, #bthe room owner goes first#k. Beward that you'll be given a time limit, and you may lose your turn if you don't make your move on time. Normally, 3 x 3 is not allowed, but if there comes a point that it's absolutely necessary to put your piece there or face ending the game, then you can put it there. 3 x 3 is allowed as the last line of defense! Oh, and it won't count if it's #r6 or 7 straight#k. Only 5!");
        } else if (current == 2)  {
            cm.sendNextPrev("Oh, and unlike Omok, when you create the game room for Match Cards, you'll need to set your game on the number of cards you'll use for the game. There are 3 modes avaliable, 3x4, 4x5, and 5x6, which will require 12, 20, and 30 cards respectively. Remember that you won't beable to change it up once the room is open, so if you really wish to change it up, you may have to close the room and open another one.");
        }

    } else if (status == 6) {
        if (current == 1) {
            cm.sendNextPrev("If you know your back is against the wall, you can request a #bRedo#k. If the opponent accepts your request, then you and your opponent's last moves will cancel out. If you ever feel the need to go to the bathroom, or take an extended break, you can request a #btie#k. The game will end in a tie if the opponent accepts the request. Tip: this may be a good way to keep your friendships in tact.");
        } else if (current == 2) {
            cm.sendNextPrev("When the first game starts, #bthe room owner goes first.#k Beware that you'll be given a time limit, and you may lose your turn if you don't make your move on time. When you find a matching pair on your turn, you'll get to keep your turn, as long as you keep finding a pair of matching cards. Use your memorizing skills to make a streak.");
        }
        
    } else if (status == 7) {
        if (current == 1) {
            cm.sendPrev("When the next game starts, the loser will go first. Also, no one is allowed to leave in the middle of a game. If you do, you may need to request either a #bforfeit or tie#k. (Of course, if you request a forfeit, you'll lose the game.) And if you click on 'Leave' in the middle of the game and call to leave after the game, you'll leave the room right after the game is over. This will be a much more useful way to leave.");
        } else if (current == 2) {
            cm.sendNextPrev("If you and your opponent have the same number of matched pairs, then whoever had a longer streak of matched pairs will win. If you ever feel the need to go to the bathroom, or take an extended break, you can request a #btie#k. The game will end in a tie if the opponent accepts the request. Tip: this may be a good way to keep your friendships in tact.");
        }
    } else if (status == 8) {
        if (current == 2) {
            cm.sendPrev("When the next game starts, the loser will go first. Also, no one is allowed to leave in the middle of a game. If you do, you may need to request either a #bforfeit or tie#k. (Of course, if you request a forfeit, you'll lose the game.) And if you click on 'Leave' in the middle of the game and call to leave after the game, you'll leave the room right after the game is over. This will be a much more useful way to leave.");
        }
    }
}  