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
            menuStr = generateSelectionMenu(["Gostaria de verificar meus pontos de batalha / adquirir a minha Cadeira de Praia com Palmeira", "Gostaria de saber mais sobre os pontos da Arena de Batalha"]);
            cm.sendSimple("Olá, o que posso fazer por você?\r\n\r\n" + menuStr);
        } else if (status == 1) {
            if (selection == 0) {
                apqpoints = cm.getPlayer().getAriantPoints();
                if (apqpoints < 100) {
                    cm.sendOk("A sua Pontua��o de Arena de Batalha � #b" + apqpoints + "#k Pontos. Voc� precisa ultrapassar os #b100 Pontos#k para que eu possa lhe dar a #bCadeira de Praia com Palmeira#k. Fale comigo novamente somente quando voc� tiver pontos suficientes.");
                    cm.dispose();
                } else if (apqpoints + arena.getAriantRewardTier(cm.getPlayer()) >= 100) {
                    cm.sendOk("A sua Pontua��o de Arena de Batalha � #b" + apqpoints + "#k Pontos, e voc� praticamente já possui essa pontuação! Converse com minha esposa, #p2101016#, para adquiri-los e então torne a conversar comigo!");
                    cm.dispose();
                } else {
                    cm.sendNext("Uaaau, parece que voc� conseguiu os #b100 Pontos#k necess�rios para troca, vamos l�?!");
                }
            } else if (selection == 1) {
                cm.sendOk("O objetivo maior das Arenas de Batalha é permitir ao jogador acumular pontos para então trocá-los honrosamente pelo prêmio maior: a #bCadeira de Praia com Palmeira#k. Acumule pontos durante as batalhas e fale comigo quando chegar a hora de adquirir seu item.\r\n\r\nEm cada batalha, é dado ao jogador a oportunidade de #bsomar pontos baseando-se na quantidade de joias#k que o jogador possui ao final. Contudo tome cuidado! Se sua distância de pontos dentre os outros jogadores #rfor muito alto#k, isso terá sido tudo por nada e você ganhará mero #r1 ponto#k.");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.getPlayer().gainAriantPoints(-100);
            cm.gainItem(3010018, 1);
            cm.dispose();
        }
    }
}

function generateSelectionMenu(array) {     // nice tool for generating a string for the sendSimple functionality
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "##b" + array[i] + "#l#k\r\n";
    }
    return menu;
}