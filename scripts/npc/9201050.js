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
/* Icebyrd Slimm
	Masteria: New Leaf City (600000000)
	Handles the quiz quest. (4900)
 */

var minlevel = 10;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
            if (cm.isQuestCompleted(4911)) {
                cm.sendNext("Good job! You've solved all of my questions about NLC. Enjoy your trip!");
                cm.dispose();
            } else if (cm.isQuestCompleted(4900) || cm.isQuestStarted(4900)) {  // thanks imbee for pointing out the quiz leak
                cm.sendNext("Hey, pay attention, I'm trying to quiz you on another question, fam!");
                cm.dispose();
            } else {
                var selStr = "What up! Name's Icebyrd Slimm, mayor of New Leaf City! Happy to see you accepted my invite. So, what can I do for you?#b"
                var info = new Array("What is this place?","Who is Professor Foxwit?","What's a Foxwit Door?","Where are the MesoGears?","What is the Krakian Jungle?","What's a Gear Portal?","What do the street signs mean?","What's the deal with Jack Masque?","Lita Lawless looks like a tough cookie, what's her story?","When will new boroughs open up in the city?","I want to take the quiz!");
                for (var i = 0; i < info.length; i++)
                    selStr += "\r\n#L" + i + "# " + info[i] + "#l";
                cm.sendSimple(selStr);
            }
        } else if(status == 1) {
            switch (selection) {
                case 0:
                    cm.sendNext("I've always dreamed of building a city. Not just any city, but one where everyone was welcome. I used to live in Kerning City, so I decided to see if I could create a city. As I went along in finding the means to do so, I encountered many people, some of whom I've come to regard as friends. Like Professor Foxwit-he's our resident genius; saved him from a group of man-eating plants. Jack Masque is an old hunting buddy from Amoria-almost too smooth of a talker for his own good. Lita and I are old friends from Kerning City-she's saved me a few times with that weapon of hers; so I figured she was a perfect choice for Town Sheriff. It took a bit of persuasion, but she came to believe her destiny lies here. About our resident explorer, Barricade came searching for something; he agreed to bring whatever he found to the museum. I'd heard stories about him and his brother when I was still in Kerning City. And Elpam...well, let's just say he's not from around here. At all. We've spoken before, and he seems to mean well, so I've allowed him to stay. I just realized that I've rambled quite a bit! What else would you like to know?");
                    status -= 2;
                    break;
                case 1:
                    cm.sendNext("A pretty spry guy for being 97. He's a time-traveller I ran into outside the city one day. Old guy had a bit of trouble with some jungle creatures-like they tried to eat him. In return for me saving him, he agreed to build a time museum. I get the feeling that he's come here for another reason, as he's mentioned more than a few times that New Leaf City has an interesting role to play in the future. Maybe you can find out a bit more...");
                    status -= 2;
                    break;
                case 2:
                    cm.sendNext("Heh, I asked the same thing when I saw the Professor building them. They're warp points. Pressing Up will warp you to another location. I recommend getting the hang of them, they're our transport system.");
                    status -= 2;
                    break;
                case 3:
                    cm.sendNext("The MesoGears are beneath Bigger Ben. It's a monster-infested section of Bigger Ben that Barricade discovered. It seems to reside in a separate section of the tower-quite strange if you ask me. I hear he needs a bit of help exploring it, you should see him. Be careful though, the Wolf Spiders in there are no joke.");
                    status -= 2;
                    break;
                case 4:
                    cm.sendNext("Ah...well. The Krakian Jungle is located on the outskirts of New Leaf City. Many new and powerful creatures roam those areas, so you'd better be prepared to fight if you head out there. It's at the right end of town. Rumors abound that the Jungle leads to a lost city, but we haven't found anything yet.");
                    status -= 2;
                    break;
                case 5:
                    cm.sendNext("Well, when John found himself in the MesoGears portion of Bigger Ben, he stood on one and went to another location. However, he could only head back and forth-they don't cycle through like the Foxwit Door. Ancient tech for you.");
                    status -= 2;
                    break;
                case 6:
                    cm.sendNext("Well, you'll see them just about everywhere. They're areas under construction. The Red lights mean it's not finished, but the Green lights mean it's open. Check back often, we're always building!");
                    status -= 2;
                    break;
                case 7:
                    cm.sendNext("Ah, Jack. You know those guys that are too cool for school? The ones who always seem to get away with everything? AND get the girl? Well, that's Jack, but without the girl. He thinks he blew his chance, and began wearing that mask to hide his true identity. My lips are sealed about who he is, but he's from Amoria. He might tell you a bit more if you ask him.");
                    status -= 2;
                    break;
                case 8:
                    cm.sendNext("I've known Lita for a while, though we've just recently rekindled our friendship. I didn't see her for quite a bit, but I understand why. She trained for a very, very long time as a Thief. Matter of fact, that's how we first met! I was besieged by a group of wayward Mushrooms, and she jumped in to help. When it was time to pick a sheriff, it was a no-brainer. She's made a promise to help others in their training and protect the city, so if you're interested in a bit of civic duty, speak with her.");
                    status -= 2;
                    break;
                case 9:
                    cm.sendNext("Soon, my friend. Even though you can't see them, the city developers are hard at work. When they're ready, we'll open them. I know you're looking forward to it and so am I!");
                    status -= 2;
                    break;
                case 10:
                    if (cm.getLevel() >= minlevel) {
                        cm.sendNext("No problem. I'll give you something nice if you answer them correctly!");
                        cm.startQuest(4900);
                    } else {
                        cm.sendNext("Eager, are we? How about you explore a bit more before I let you take the quiz?");
                    }
                    
                    cm.dispose();
                    break;
            }
        }
    }
}