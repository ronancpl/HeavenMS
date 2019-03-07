var map = 980030000;
var minLvl = 30;
var maxLvl = 255;
var minAmt = 0;
var maxAmt = 6;

function start() {
    status = -1;
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
            if (cm.getParty() == null) {
                status = 10;
                cm.sendOk("#eÉ necessário criar um grupo antes de começar o Festival de Monstros!#k");
            } else if (!cm.isLeader()) {
                status = 10;
                cm.sendOk("Se você quer começar o Festival, avise o #blíder do grupo#k para falar comigo.");
            } else {
                var party = cm.getParty().getMembers();
                var inMap = cm.partyMembersInMap();
                var lvlOk = 0;
                var isInMap = 0;
                for (var i = 0; i < party.size(); i++) {
                    if (party.get(i).getLevel() >= minLvl && party.get(i).getLevel() <= maxLvl) {
                        lvlOk++;
                    }
                    if (party.get(i).getPlayer().getMapId()!= 980030000) {
                        //isInMap = false;
                        isInMap++
                    }
                }

                if (party >= 1) {
                    status = 10;
                    cm.sendOk("Você não tem número suficiente de pessoas em seu grupo. Você precisa de um grupo com #b" + minAmt + "#k - #r" + maxAmt + "#k membros e eles devem estar no mapa com você.");
                } else if (lvlOk != inMap) {
                    status = 10;
                    cm.sendOk("Certifique se todos em seu grupo estão dentre os níveis corretos (" + minLvl + "~" + maxLvl + ")!");
                } else if (isInMap > 0) {
                    status = 10;
                    cm.sendOk("Existe alguém do grupo que não esta no mapa!");
                } else {
                    cm.sendCPQMapLists2();
                }
            }
        } else if (status == 1) {
            if (cm.fieldTaken2(selection)) {
                if (cm.fieldLobbied2(selection)) {
                    cm.challengeParty2(selection);
                    cm.dispose();
                } else {
                    cm.sendOk("A sala esta cheia.");
                    cm.dispose();
                }
            } else {
                var party = cm.getParty().getMembers();
                if ((selection === 0 || selection === 1 ) && party.size() < 2) {
                    cm.sendOk("Você precisa de no mínimo 2 player para entrar na competição.");
                } else if ((selection === 2 ) && party.size() < 3) {
                    cm.sendOk("Você precisa de no mínimo 3 player para entrar na competição.");
                } else {
                    cm.cpqLobby2(selection);
                }
                cm.dispose();
            }
        } else if (status == 11) {
            cm.dispose();
        }
    }
}