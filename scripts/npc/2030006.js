/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
/* Holy Stone
	Holy Ground at the Snowfield (211040401)
	3rd job advancement - Question trial.
 */

var questionTree = [
        //Questions Related to CHARACTERS
        ["In MapleStory, what is the EXP needed to level up from Lv1 to Lv2?", ["20", "15", "4", "12", "16"], 1],
        ["In 1st job adv. which of the following is WRONG requirement?", ["Magician - Level 8", "Pirate - 20 DEX or more", "Archer - 25 DEX or more", "Thief - 20 LUK or more", "Swordman - 35 STR or more"], 3],
        ["When you hit by monster, which of the following is not fully explained?", ["Sealed - skills become disabled", "Undead - turns undead & halved recovery amounts", "Weaken - slow down moving speed", "Cursed - EXP received are decreased", "Stunned - cannot move"], 2],
        ["For the 1st job adv. Which job fully states the job adv. requirement?", ["Pirate - 25 LUK", "Magician - Level 10", "Thief - 25 LUK", "Warrior - 30 STR", "Bowman - 25 DEX"], 4],

        //Questions Related to ITEMS
        ["Which of following monsters got CORRECT item corresponding to the monster?", ["Royal cactus - Needle", "Wild Boar - Boar fang", "Lazy Buffy - Buffy hat", "Chipmunk - Nut", "Stirge - Stirge's wing"], 4],
        ["Which of following monsters got WRONG item corresponding to the monster?", ["Greatest Oldies - Greatest oldies", "Nependeath - Nependeath's leaf", "Ghost stump - Seedling", "Sparker - Seal tooth", "Miner Zombie - Zombie's lost tooth"], 1],
        ["In GM Event, how many FRUIT CAKE you can get as reward?", ["20", "200", "5", "25", "100"], 2],
        ["Which of following potions got CORRECT info.?", ["Warrior Elixir - Attack +5 for 3 minutes", "Pure Water - Recover 700 MP", "Cake - Recover 150 HP & MP", "Salad - Recover 300 MP", "Pizza - Recover 400 HP"], 4],
        ["Which of following potions got WRONG info.?", ["Mana Elixir - Recover 300 MP", "Tonic - Cures state of weakness", "Apple - Recover 30 HP", "Sunrise Dew - Recover 3000 MP", "Ramen - Recover 1000 HP"], 3],

        //Questions Related to MONSTERS
        ["Green Mushroom, Tree Stump, Bubbling, Axe Stump, Octopus, which is highest level of all?", ["Tree Stump", "Bubbling", "Axe Stump", "Octopus", "Green Mushroom"], 2],
        ["Which monster will be seen during the ship trip to Orbis/Ellinia?", ["Werewolf", "Slime", "Crimson Balrog", "Zakum", "Star Pixie"], 2],
        ["Maple Island doesn't have which following monsters?", ["Green Mushroom", "Blue Snail", "Orange Mushroom", "Red Snail", "Pig"], 0],
        ["Which monster is not at Victoria Island and Sleepywood?", ["Evil Eye", "Sentinel", "Jr. Balrog", "Ghost Stump", "Snail"], 1],
        ["El Nath doesn't have which following monsters?", ["Dark Yeti", "Dark Ligator", "Yeti & Pepe", "Bain", "Coolie Zombie"], 1],
        ["Which of following monsters can fly?", ["Malady", "Ligator", "Cold Eye", "Meerkat", "Alishar"], 0],
        ["Which of these monsters will you NOT be facing in Ossyria?", ["Lunar Pixie", "Lioner", "Cellion", "Croco", "Hector"], 3],
        ["Which monster has not appeared in Maple Island?", ["Snail", "Shroom", "Evil Eye", "Orange Mushroom", "Blue Snail"], 2],

        //Questions Related to QUESTS
        ["Which material doesn't need for awaken Hero's Gladius?", ["Flaming Feather", "Old Gladius", "Piece of Ice", "Ancient Scroll", "Fairy Wing"], 4],
        ["Which of following quests can be repeated?", ["Mystery of Niora Hospital", "Rightful Donation Culture", "The Ghost Whereabout", "Arwen and the Glass Shoe", "Maya and the Weird Medicine"], 3],
        ["Which of following are not 2nd job adv.?", ["Mage", "Cleric", "Assassin", "Gunslinger", "Fighter"], 0],
        ["Which of following is the highest level quest?", ["Cupid's Courier", "Lost in the Ocean", "Alcaster and the Dark Crystal", "Eliminating the Drumming Bunny", "War of Pang Pang"], 2],

        //Questions Related to TOWN/NPC
        ["Which town is not at Victoria Island?", ["Florina Beach or Nautilus", "Amherst or Southperry", "Kerning City & Square", "Perion or Ellinia", "Sleepywood"], 1],
        ["Which is the first NPC you meet in Maple Island?", ["Sera", "Heena", "Lucas", "Roger", "Shanks"], 1],
        ["Which NPC cannot be seen in El Nath?", ["Vogen", "Sophia", "Pedro", "Master Sergeant Fox", "Rumi"], 1],
        ["Which NPC cannot be seen in El Nath snowfield?", ["Hidden Rock", "Glibber", "Jeff", "Holy Stone", "Elma the Housekeeper"], 4],
        ["Which NPC cannot be seen in Perion?", ["Ayan", "Sophia", "Mr. Smith", "Francois", "Manji"], 3],
        ["Which NPC cannot be seen in Henesys?", ["Teo", "Vicious", "Mia", "Doofus", "Casey"], 0],
        ["Which NPC cannot be seen in Ellinia?", ["Mr. Park", "Mar the Fairy", "Roel", "Ria", "Shane"], 2],
        ["Which NPC cannot be seen in Kerning City?", ["Dr. Faymus", "Mong from Kong", "Ervine", "Luke", "Nella"], 3],
        ["Which NPC is not related to pets?", ["Doofus", "Vicious", "Patricia", "Weaver", "Cloy"], 1],
        ["In Kerning City, who is the father of Alex, the runaway kid?", ["Chief Stan", "JM From tha Streetz", "Dr. Faymus", "Vicious", "Luke"], 0],
        ["Which NPC is not belong to Alpha Platoon's Network of Communication?", ["Staff Sergeant Charlie", "Sergeant Bravo", "Corporal Easy", "Master Sergeant Fox", "Peter"], 4],
        ["What do you receive in return from giving 30 Dark Marbles to the 2nd job advancement NPC?", ["Old Ring", "Memory Powder", "Fairy Dust", "Proof of Hero", "Scroll of Secrets"], 3],
        ["Which item you give Maya at Henesys in order to cure her sickness?", ["Apple", "Power Elixir", "Weird Medicine", "Chrysanthemum", "Orange Juice"], 2],
        ["Which of following NPC is not related to item synthesis/refine?", ["Neve", "Serryl", "Shane", "Francois", "JM From tha Streetz"], 2],
        ["Which NPC cannot be seen in Maple Island?", ["Bari", "Teo", "Pio", "Sid", "Maria"], 1],
        ["Who do you see in the monitor in the navigation room with Kyrin?", ["Lucas", "Dr. Kim", "Chief Stan", "Scadur", "Professor Foxwit"], 1],
        ["You know Athena Pierce in Henesys? What color are her eyes?", ["Blue", "Green", "Brown", "Red", "Black"], 1],
        ["How many feathers are there on Dances with Barlog's Hat?", ["7", "8", "3", "13", "16"], 3],
        ["What's the color of the marble Grendel the Really Old from Ellinia carries with him?", ["White", "Orange", "Blue", "Purple", "Green"], 2]
    ];
    
var status;
var question;

var questionPool;
var questionPoolCursor;

var questionAnswer;

function start() {
    status = -1;
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

        if(status == 0) {
            if(cm.getPlayer().gotPartyQuestItem("JBQ") && !cm.haveItem(4031058, 1)) {
                if(cm.haveItem(4005004, 1)) {
                    if(!cm.canHold(4031058)) {
                        cm.sendNext("Have a free ETC slot available before accepting this trial.");
                        cm.dispose();
                    } else {
                        cm.sendNext("Alright... I'll be testing out your wisdom here. Answer all the questions correctly, and you will pass the test BUT, if you even lie to me once, then you'll have to start over again ok, here we go.");
                    }
                } else {
                    cm.sendNext("Bring me a #b#t4005004##k to proceed with the trial.");
                    cm.dispose();
                }
            } else {
                cm.dispose();
            }
        } else if(status == 1) {
            cm.gainItem(4005004, -1);
            instantiateQuestionPool();
            
            question = fetchNextQuestion();
            var questionHead = generateQuestionHeading();
            var questionEntry = questionTree[question][0];
            
            var questionData = generateSelectionMenu(questionTree[question][1], questionTree[question][2]);
            var questionOptions = questionData[0];
            questionAnswer = questionData[1];
            
            cm.sendSimple(questionHead + questionEntry + "\r\n\r\n#b" + questionOptions + "#k");
        } else if(status >= 2 && status <= 5) {
            if(!evaluateAnswer(selection)) {
                cm.sendNext("You have failed the question.");
                cm.dispose();
                return;
            }
            
            question = fetchNextQuestion();
            var questionHead = generateQuestionHeading();
            var questionEntry = questionTree[question][0];
            
            var questionData = generateSelectionMenu(questionTree[question][1], questionTree[question][2]);
            var questionOptions = questionData[0];
            questionAnswer = questionData[1];
            
            cm.sendSimple(questionHead + questionEntry + "\r\n\r\n#b" + questionOptions + "#k");
        } else if(status == 6) {
            if(!evaluateAnswer(selection)) {
                cm.sendNext("You have failed the question.");
                cm.dispose();
                return;
            }
            
            cm.sendOk("Alright. All your answers have been proven as the truth. Your wisdom has been proven.\r\nTake this necklace and go back.");
            cm.gainItem(4031058, 1);
            cm.dispose();
        } else {
            cm.sendOk("Unexpected branch.");
            cm.dispose();
        }
    }
}

function evaluateAnswer(selection) {
    return selection == questionAnswer;
}

function generateQuestionHeading() {
    return "Here's the " + (status) + (status == 1 ? "st" : status == 2 ? "nd" : status == 3 ? "rd" : "th") + " question. ";
}

function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

function instantiateQuestionPool() {
    questionPool = [];
    
    for(var i = 0; i < questionTree.length; i++) {
        questionPool.push(i);
    }
    
    shuffleArray(questionPool);
    questionPoolCursor = 0;
}

function fetchNextQuestion() {
    var next = questionPool[questionPoolCursor];
    questionPoolCursor++;
    
    return next;
}

function shuffle(array) {
    var currentIndex = array.length, temporaryValue, randomIndex;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
    }

    return array;
}

function generateSelectionMenu(array, answer) {
    var answerStr = array[answer], answerPos = -1;
    
    shuffle(array);
    
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
        if (answerStr == array[i]) {
            answerPos = i;
        }
    }
    return [menu, answerPos];
}