var arena;
var status = 0;

importPackage(Packages.client);

function start() {
    arena = cm.getPlayer().getAriantColiseum();
    if (arena == null) {
        cm.sendOk("Ei, não vi você em campo durante as atividades do coliseu! O que você está fazendo aqui?");
        cm.dispose();
        return;
    }
    
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
            copns = arena.getAriantScore(cm.getPlayer());
            if (copns < 1 && !cm.getPlayer().isGM()) {
                cm.sendOk("Que pena, voc� nao conseguiu nenhuma j�ia!");
                cm.dispose();
            } else {
                cm.sendNext("Ok, vamos ver...Voc� foi muito bem, e voc� trouxe #b" + copns + "#k j�ias que eu adoro. Como voc� completou a partida, vou recompens�-lo com a pontua��o da Arena de Batalhas de #b" + arena.getAriantRewardTier(cm.getPlayer()) + " Pontos#k. Se voc� quiser saber mais sobre a pontua��o de Arena de Batalha, ent�o fale com #b#p2101015##k.");
            }
        } else if (status == 1) {
            //cm.warp(980010020, 0);
            copns = arena.getAriantRewardTier(cm.getPlayer());
            arena.clearAriantRewardTier(cm.getPlayer());
            arena.clearAriantScore(cm.getPlayer());
            cm.removeAll(4031868);
            
            cm.getPlayer().gainExp(92.7 * cm.getPlayer().getExpRate() * copns, true, true);
            cm.getPlayer().gainAriantPoints(copns);
            cm.dispose();
        }
    }
}