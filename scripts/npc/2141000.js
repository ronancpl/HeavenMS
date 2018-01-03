/*
 * Time Temple - Kirston
 * Twilight of the Gods
 */

function start() {
    cm.sendAcceptDecline("If only I had the Mirror of Goodness then I can re-summon the Black Wizard! \r\nWait! something's not right! Why is the Black Wizard not summoned? Wait, what's this force? I feel something... totally different from the Black Wizard Ahhhhh!!!!! \r\n\r\n #b(Places a hand on the shoulder of Kryston.)");
}

function action(mode, type, selection) {
    if (mode == 1) {
	cm.removeNpc(270050100, 2141000);
	cm.forceStartReactor(270050100, 2709000);
    }
    cm.dispose();

// If accepted, = summon PB + Kriston Disappear + 1 hour timer
// If deny = NoTHING HAPPEN
}