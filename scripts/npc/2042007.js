importPackage(net.sf.odinms.server.maps);

var status = 0;
var rnk = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("Alright then, I hope we can chat later next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.getChar().getMap().isCPQLoserMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shiu = "";
                    if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rA#k";
                        cm.sendOk("Infelizmente, você ou empatou ou perdeu a batalha, apesar da sua excelente performance. A vitória pode ser sua da próxima vez.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 50 && cm.getPlayer().getFestivalPoints() < 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("Infelizmente, você ou empatou ou perdeu a batalha, mesmo com sua ótima performance. Só mais um pouquinho, e a vitória poderia ter sido sua.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 30 && cm.getPlayer().getFestivalPoints() < 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("Infelizmente, você ou empatou ou perdeu a batalha. A vitória está para aqueles que se esforçam. Vejo seus esforços, então a vitória não está tão longe do seu alcance. Continue assim!\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("Infelizmente, você ou empatou ou perdeu a batalha, e sua performance claramente reflete nisso. Espero mais de você da próxima vez.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                    }
                } else {
                    cm.warp(980030000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 10:
                        cm.warp(980030000, 0);
                        cm.gainExp(35000);
                        cm.dispose();
                        break;
                    case 20:
                        cm.warp(980030000, 0);
                        cm.gainExp(25000);
                        cm.dispose();
                        break;
                    case 30:
                        cm.warp(980030000, 0);
                        cm.gainExp(12500);
                        cm.dispose();
                        break;
                    case 40:
                        cm.warp(980030000, 0);
                        cm.gainExp(3500);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980030000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getChar().getMap().isCPQWinnerMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shi = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shi += "#rA#k";
                        rnk = 1;
                        cm.sendOk("Parabéns pela sua vitória!!! Que ótima performance! O grupo adversário não pôde fazer nada! Espero o mesmo bom trabalho da próxima vez!\r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100 && cm.getPlayer().getFestivalPoints() < 300) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("Parabéns pela sua vitória! Isso foi impressionante! Você fez um bom trabalho contra o grupo adversário! Só mais um pouco, e você definitivamente vai conseguir um A na próxima vez. \r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50 && cm.getPlayer().getFestivalPoints() < 100) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("Parabéns pela sua vitória. Você fez algumas coisas cá e lá, mas essa não pode ser considerada uma boa vitória. Espero mais de ti da próxima vez.\r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("Parabéns pela sua vitória, entretanto sua performance não refletiu muito bem isso. Seja mais ativo na sua próxima participação da Folia de Monstros!\r\n\r\n#bNota da Folia de Monstros : " + shi);
                    }
                } else {
                    cm.warp(980030000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 1:
                        cm.warp(980030000, 0);
                        cm.gainExp(875000);
                        cm.dispose();
                        break;
                    case 2:
                        cm.warp(980030000, 0);
                        cm.gainExp(700000);
                        cm.dispose();
                        break;
                    case 3:
                        cm.warp(980030000, 0);
                        cm.gainExp(555000);
                        cm.dispose();
                        break;
                    case 4:
                        cm.warp(980030000, 0);
                        cm.gainExp(100000);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980030000, 0);
                        cm.dispose();
                        break;
                }
            }
        }
    }
}  