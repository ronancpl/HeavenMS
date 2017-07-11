/**
 *9201098 - Lukan
 *@author Ronan
 */
 
function start() {
    if(cm.getQuestStatus(8223) == 2) {
        if(cm.haveItem(3992041)) cm.sendOk("We, defenders of Yore, are currenly meeting at the Inner Sactum inside the Keep, about to start an offensive against the Twisted Masters and their army. Join us there anytime.");
        else {
            if(!cm.canHold(3992041)) cm.sendOk("Please make a slot on your SETUP ready for the key I have to give to you. It is fundamental to enter the Inner Sanctum, inside the Keep.");
            else {
                cm.sendOk("So you did lost your key, right? Very well, I will craft you another one, but please don't lose it again. It is fundamental to enter the Inner Sanctum, inside the Keep.");
                cm.gainItem(3992041, 1);
            }
        }
    } else {
        cm.sendOk("O, brave adventurer. The Stormcasters house, from which I belong, guards the surrounding area of Yore, this landscape, from the forces of the Twisted Masters' guard that daily threathens the citizens. Please help us on the defense of Yore.");
    }
    
    cm.dispose();
}
