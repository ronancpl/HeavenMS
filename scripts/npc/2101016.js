
var status = 0;

importPackage(Packages.client);

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
            copns = cm.getPlayer().countItem(4031868);
            if (copns < 1) {
                cm.sendOk("Que pena, você nao conseguiu nenhuma jóia!")
                cm.dispose();
            }
            if (copns > 0 || !cm.getPlayer().isGM()) {
                cm.sendNext("Ok, vamos ver...Você foi muito bem, e você trouxe #b" + copns + "#k jóias que eu adoro. Como você completou a partida, vou recompensá-lo com a pontuação da Arena de Batalhas de #b5 Pontos#k. Se você quiser saber mais sobre a pontuação de Arena de Batalha, então fale com #b#p2101015##k.");
            }
        } else if (status == 1) {
            //cm.warp(980010020, 0);
            cm.removeAll(4031868);
            cm.getPlayer().gainExp(92.7 * cm.getPlayer().getExpRate() * copns, true, true);
            cm.getPlayer().gainAriantPontos(3);
            cm.dispose();
        }
    }
}