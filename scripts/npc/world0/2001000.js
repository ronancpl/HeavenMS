/*
 *  Cliff - Happy Ville NPC
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status > 0) {
	    status--;
	} else {
	    cm.dispose();
	    return;
	}
    }
    if (status == 0) {
	cm.sendNext("Do you see a bunch of snowmen standing around there? Go talk to one of them, and it'll take you to the famous Christmas tree here that is just humongous. The tree can be decorated using various kinds of ornaments. What do you think? Sounds fun, right?");
    } else if (status == 1) {
	cm.sendNextPrev("Only 6 can be at the place where the tree is at once, and you can't #btrade or open store#k there. The ornaments that you drop can only be picked back up by yourself, so don't worry about losing your ornaments here.");
    } else if (status == 2) {
	cm.sendNextPrev("Of course, the items that are dropped in there will never disappear. Once you get out of there through the snowman that's inside, all the items you've dropped at that map will come back to you, so you won't have to pick all those items up before leaving the place. Isn't that sweet?");
    } else if (status == 3) {
	cm.sendPrev("Well then, go see #p2002001#, buy some Christmas ornaments there, and then decorate the tree with those~ Oh yeah! The biggest, the most beautiful ornament cannot be bought from him. It's probably ... taken by a monster ... huh huh ..");
	cm.dispose();
    }
}