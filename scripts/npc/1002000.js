var status = 0;
var imaps = [104000000, 102000000, 100000000, 101000000, 103000000, 120000000, 105040300];
var maps = [102000000, 100000000, 101000000, 103000000, 120000000];
var cost = [1000, 1000, 800, 1000, 800];
var townText = [["The town you are at is Lith Harbor! Alright I'll explain to you more about #bLith Harbor#k. It's the place you landed on Victoria Island by riding The Victoria. That's Lith Harbor. A lot of beginners who just got here from Maple Island start their journey here.", "It's a quiet town with the wide body of water on the back of it, thanks to the fact that the harbor is located at the west end of the island. Most of the people here are, or used to be fisherman, so they may look intimidating, but if you strike up a conversation with them, they'll be friendly to you.", "Around town lies a beautiful prairie. Most of the monsters there are small and gentle, perfect for beginners. If you haven't chosen your job yet, this is a good place to boost up your level."],["Alright I'll explain to you more about #bPerion#k. It's a warrior-town located at the northern-most part of Victoria Island, surrounded by rocky mountains. With an unfriendly atmosphere, only the strong survives there.", "Around the highland you'll find a really skinny tree, a wild hog running around the place, and monkeys that live all over the island. There's also a deep valley, and when you go deep into it, you'll find a humongous dragon with the power to match his size. Better go in there very carefully, or don't go at all.", "If you want to be a #bWarrior#k then find #rDances with Balrog#k, the chief of Perion. If you're level 10 or higher, along with a good STR level, he may make you a warrior after all. If not, better keep training yourself until you reach that level."], ["Alright I'll explain to you more about #bEllinia#k. It's a magician-town located at the far east of Victoria Island, and covered in tall, mystic trees. You'll find some fairies there, too. They don't like humans in general so it'll be best for you to be on their good side and stay quiet.", "Near the forest you'll find green slimes, walking mushrooms, monkeys and zombie monkeys all residing there. Walk deeper into the forest and you'll find witches with the flying broomstick navigating the skies. A word of warning: Unless you are really strong, I recommend you don't go near them.", "If you want to be a #bMagician#k, search for #rGrendel the Really Old#k, the head wizard of Ellinia. He may make you a wizard if you're at or above level 8 with a decent amount of INT. If that's not the case, you may have to hunt more and train yourself to get there."], ["Alright I'll explain to you more about #bHenesys#k. It's a bowman-town located at the southernmost part of the island, made on a flatland in the midst of a deep forest and prairies. The weather's just right, and everything is plentiful around that town, perfect for living. Go check it out.", "Around the prairie you'll find weak monsters such as snails, mushrooms, and pigs. According to what I hear, though, in the deepest part of the Pig Park, which is connected to the town somewhere, you'll find a humongous, powerful mushroom called Mushmom every now and then.", "If you want to be a #bBowman#k, you need to go see #rAthena Pierce#k at Henesys. With a level at or above 10 and a decent amount of DEX, she may make you be one afterall. If not, go train yourself, make yourself stronger, then try again."], ["Alright I'll explain to you more about #bKerning City#k. It's a thief-town located at the northwest part of Victoria Island, and there are buildings up there that have just this strange feeling around them. It's mostly covered in black clouds, but if you can go up to a really high place, you'll be able to see a very beautiful sunset there.", "From Kerning City, you can go into several dungeons. You can go to a swamp where alligators and snakes are abound, or hit the subway full of ghosts and bats. At the deepest part of the underground, you'll find Lace, who is just as big and dangerous as a dragon.", "If you want to be a #bThief#k, seek #rDark Lord#k, the heart of darkness of Kerning City. He may well make you a thief if you're at or above level 10 with a good amount of DEX. If not, go hunt and train yourself to reach there."], ["Here's a little information on #b#m120000000##k. It's a submarine that's currently parked in between Ellinia and Henesys in Victoria Island. That submarine serves as home to numerous pirates. You can have just as beautiful a view of the ocean there as you do here in Lith Harbor.", "#m120000000# is parked in between Henesys and Ellinia, so if you step out just a bit, you'll be able to enjoy the view of both towns. All the pirates you'll meet in town are very gregarious and friendly as well.", "If you are serious about becoming a #bPirate#k, then you better meet the captain of #m120000000#, #r#p1090000##k. If you are over Level 10 with 20 DEX, then she may let you become one. If you aren't up to that level, then you'll need to train harder to get there!"], ["Alright I'll explain to you more about #bSleepywood#k. It's a forest town located at the southeast side of Victoria Island. It's pretty much in between Henesys and the ant-tunnel dungeon. There's a hotel there, so you can rest up after a long day at the dungeon ... it's a quiet town in general.", "In front of the hotel there's an old buddhist monk by the name of #rChrishrama#k. Nobody knows a thing about that monk. Apparently he collects materials from the travelers and create something, but I am not too sure about the details. If you have any business going around that area, please check that out for me.", "From Sleepywood, head east and you'll find the ant tunnel connected to the deepest part of the Victoria Island. Lots of nasty, powerful monsters abound so if you walk in thinking it's a walk in the park, you'll be coming out as a corpse. You need to fully prepare yourself for a rough ride before going in.", "And this is what I hear ... apparently, at Sleepywood there's a secret entrance leading you to an unknown place. Apparently, once you move in deep, you'll find a stack of black rocks that actually move around. I want to see that for myself in the near future ..."]];
var selectedMap = -1;
var town = false;

function start() {
    cm.sendNext("Do you wanna head over to some other town? With a little money involved, I can make it happen. It's a tad expensive, but I run a special 90% discount for beginners.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if((mode == 0 && !town) || mode == -1){
            if(type == 1 && mode != -1)
                cm.sendNext("There's a lot to see in this town, too. Let me know if you want to go somewhere else.");
            cm.dispose();
            return;
        }else {
            status -= 2;
            
            if(status < 1) {
                cm.dispose();
                return;
            }
        }
            
    }
    if (status == 1)
        cm.sendSimple("It's understandable that you may be confused about this place if this is your first time around. If you got any questions about this place, fire away.\r\n#L0##bWhat kind of towns are here in Victoria Island?#l\r\n#L1#Please take me somewhere else.#k#l");
    else if (status == 2){
        if (selection == 0){
            town = true;
            var text = "There are 7 big towns here in Victoria Island. Which of those do you want to know more of?#b";
            for(var i = 0; i < imaps.length; i++)
                text += "\r\n#L" + i + "##m" + imaps[i] + "##l";
            cm.sendSimple(text);
        }else if (selection == 1) {
            var selStr = cm.getJobId() == 0 ? "There's a special 90% discount for all beginners. Alright, where would you want to go?#b" : "Oh you aren't a beginner, huh? Then I'm afraid I may have to charge you full price. Where would you like to go?#b";
            for (var i = 0; i < maps.length; i++)
                selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (cost[i] / (cm.getJobId() == 0 ? 10 : 1)) + " mesos)#l";
            cm.sendSimple(selStr);
        }
    } else if (town){
        if (selectedMap == -1)
            selectedMap = selection;
        if(status == 3)
            cm.sendNext(townText[selectedMap][status-3]);
        else
            townText[selectedMap][status-3] == undefined ? cm.dispose() : cm.sendNextPrev(townText[selectedMap][status-3]);
    }else if(status == 3){
        selectedMap = selection;
        cm.sendYesNo("I guess you don't need to be here. Do you really want to move to #b#m" + maps[selection] + "##k? Well it'll cost you #b" + (cost[selection] / (cm.getJobId() == 0 ? 10 : 1)) + " mesos#k. What do you think?");
    }else if (status == 4) {
        if (cm.getMeso() < (cost[selectedMap] / (cm.getJobId() == 0 ? 10 : 1)))
            cm.sendNext("You don't have enough mesos. With your abilities, you should have more than that!");
        else {
            cm.gainMeso(-(cost[selectedMap] / (cm.getJobId() == 0 ? 10 : 1)));
            cm.warp(maps[selectedMap]);
        }
        cm.dispose();
    }
}