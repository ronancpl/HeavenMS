/**
 *9201103 - Sage
 *@author Ronan
 */
 
function start() {
    if(cm.getLevel() >= 100) cm.sendOk("Expeditions are frequently being held inside the Crimsonwood Keep by adventurers like you, where many people from many parties cooperate together, solving puzzles therein and taking down strong enemies, being able to get many prizes in the process. To find more info about this, go ahead inside the keep at the top-right room there.");
    else cm.sendOk("Inside the Keep, expeditions can be formed to attempt the Crimsonwood Keep PQ, which requires maplers from level 100 or more. It seems you are not suitable for attempting it yet, train some more if you want to attempt it.");
    cm.dispose();
}
