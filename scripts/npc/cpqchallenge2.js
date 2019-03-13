var status = 0;
var party;

function start(chrs) {
    status = -1;
    party = chrs;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.getChar().setChallenged(false);
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendOk("Come back once you have thought about it some more.");
            cm.getChar().setChallenged(false);
            cm.dispose();
            return;
        }
    }
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getParty().getMembers().size() == party.size()) {
                cm.getPlayer().setChallenged(true);
                var snd = "";
                for (var i = 0; i < party.size(); i++)
                    snd += "#bNome: " + party.get(i).getName() + " / (Level: " + party.get(i).getLevel() + ") / " + party.get(i).getJobNameById(party.get(i).getJobId()) + "#k\r\n\r\n";
                cm.sendAcceptDecline(snd + "Gostaria de lutar contra este grupo no Festival de Monstros?");
            } else {
                return;
            }
        } else if (status == 1) {
            var ch = cm.getChrById(party.get(0).getId());
            cm.startCPQ2(ch, ch.getMapId() + 1);
            ch.getParty().setEnemy(cm.getPlayer().getParty());
            cm.getChar().getParty().setEnemy(ch.getParty());
            cm.getChar().setChallenged(false);
            cm.dispose();
        }
    }
}