/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Karcasa
	Map(s): 		The Burning Sands: Tents of the Entertainers(260010600)
	Description: 		Warps to Victoria Island
*/
var towns = new Array(100000000,101000000,102000000,103000000,104000000);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (mode == 0) {
		cm.sendNext("Aye...are you scared of speed or heights? You can't trust my flying skills? Trust me, I've worked out all the kinks!");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		cm.sendNext("I don't know how you found out about this, but you came to the right place! For those that wandered around Nihal Desert and are getting homesick, I am offering a flight straight to Victorial Island, non-stop! Don't worry about the flying ship--it's only fallen once or twice! Don't you feel claustrophobic being in a long flight on that small ship?");
	} else if(status == 1){
		cm.sendYesNo("Please remember two things. One, this line is actually for overseas shipping, so #rI cannot gurantee exactly which town you'll land#k. Two, since I am putting you in this special flight, it'll be a bit expensive. The service charge is #e#b10,000 mesos#n#k. There's a flight thats about to take off. Are you interested in this direct flight?");
	} else if(status == 2){
		cm.sendNext("Okay, ready to takeoff~");
	} else if(status == 3){
		if(cm.getMeso() >= 10000){
			cm.gainMeso(-10000);
			cm.warp(towns[Math.floor(Math.random() * towns.length)]);
		} else{
			cm.sendNextPrev("Hey, are you short on cash? I told you you'll need #b10,000#k mesos to get on this.");
			cm.dispose();
			}
		}
	}
}