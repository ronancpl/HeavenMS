/**
 * @author: Ronan
 * @npc: Abdula
 * @map: Multiple cities on Maplestory
 * @func: Job Skill / Mastery Book Drop Announcer
*/

var status;
var selected = 0;
var skillbook = [], masterybook = [], table = [];

function start() {
    status = -1;
    selected = 0;
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
            var greeting = "Hello, I'm #p9209000#, the Skill & Mastery Book announcer! ";
            skillbook = cm.getAvailableSkillBooks();
            masterybook = cm.getAvailableMasteryBooks();

            if(skillbook.length == 0 && masterybook.length == 0) {
                cm.sendOk(greeting + "There are no more books available to further improve your job skills for now. Either you #bmaxed out everything#k or #byou didn't reach the minimum requisites to use some skill books#k yet.");
                cm.dispose();

            } else if(skillbook.length > 0 && masterybook.length > 0) {
                var sendStr = greeting + "New opportunities for skill improvement have been located for you to improve your skills! Pick a type to take a look onto.\r\n\r\n#b";

                sendStr += "#L1# Skill Book#l\r\n";
                sendStr += "#L2# Mastery Book#l\r\n";

                cm.sendSimple(sendStr);
            } else if(skillbook.length > 0) {
                selected = 1;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill learns available for now.");
            } else {
                selected = 2;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill upgrades available.");
            }

        } else if(status == 1) {
            var sendStr = "The following books are currently available:\r\n\r\n";
            if(selected == 0) selected = selection;

            table = (selected == 1) ? skillbook : masterybook;
            for(var i = 0; i < table.length; i++) {
                sendStr += "  #L" + i + "# #i" + table[i] + "#  #t" + table[i] + "##l\r\n";
            }

            cm.sendSimple(sendStr);

        } else if(status == 2) {
            selected = selection;
            var mobList = cm.getNamesWhoDropsItem(table[selected]);

            var sendStr;
            if(mobList.length == 0) {
                sendStr = "No mobs drop '#b#t" + table[selected] + "##k'.";

            } else {
                sendStr = "The following mobs drop '#b#t" + table[selected] + "##k':\r\n\r\n";

                for(var i = 0; i < mobList.length; i++) {
                    sendStr += "  #L" + i + "# " + mobList[i] + "#l\r\n";
                }
            }

            cm.sendOk(sendStr);
            cm.dispose();
        }
    }
}
