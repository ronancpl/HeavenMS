/* A Familiar Lady
    Hidden Street : Gloomy Forest (922220000)
 */

var status;
 
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
                if(cm.getQuestProgress(23647, 0) != 0) {
                    cm.dispose();
                    return;
                }
                
                if(!cm.haveItem(4031793, 1)) {
                    cm.sendOk("Umm... Hey... Would you help me find a #bsoft and shiny silver fur#k that I lost on the woods? I need it, I need it, I need it sooooo much!");
                    cm.dispose();
                    return;
                }
                
                cm.sendYesNo("Hey... Umm... Would you help me find a #bsoft and shiny silver fur#k that I lost on the woods? I need it, I need it, I need it sooooo much! ... Oh you found it!!! Will you give it to me?");
            } else if(status == 1) {
                cm.sendNext("Teehehee~ That's your reward for taking it from me, serves you well.");
                cm.gainItem(4031793, -1);
                cm.gainFame(-5);
                cm.setQuestProgress(23647, 0, 1);
                
                cm.dispose();
            }
        }
}