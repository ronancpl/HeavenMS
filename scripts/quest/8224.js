/* ===========================================================
			Ronan Lana
	NPC Name: 		Taggrin
	Description: 	Quest - The Fallen Woods
=============================================================
Version 1.0 - Script Done.(10/7/2017)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
                status -= 2;
            else{
                    qm.sendOk("Okay, then. See you around.");
                    qm.dispose();
                    return;
            }
    }
    if (status == 0)
            qm.sendAcceptDecline("Hey traveler, come here! I am Taggrin, leader of the Raven Ninja Clan. We are mercenaries currently under the payload of the New Leaf City county. Our job here is to hunt down those creatures that have been lurking around here these days. Are you interested to make a little errand for us? Of course, the pay off will be advantageous for both parties.");
    else if (status == 1){
            qm.sendOk("Ok. I need you to hunt down #bthose fake trees#k in the forest, and collect 50 of their drops as proof that you made your part on this.");
            qm.forceStartQuest();
            qm.dispose();
    }
}
