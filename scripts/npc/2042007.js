
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
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shiu += "#rA#k";
                        cm.sendOk("Infelizmente, voc� ou empatou ou perdeu a batalha, apesar da sua excelente performance. A vit�ria pode ser sua da pr�xima vez.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("Infelizmente, voc� ou empatou ou perdeu a batalha, mesmo com sua �tima performance. S� mais um pouquinho, e a vit�ria poderia ter sido sua.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("Infelizmente, voc� ou empatou ou perdeu a batalha. A vit�ria est� para aqueles que se esfor�am. Vejo seus esfor�os, ent�o a vit�ria n�o est� t�o longe do seu alcance. Continue assim!\r\n\r\n#bNota da Folia de Monstros : " + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("Infelizmente, voc� ou empatou ou perdeu a batalha, e sua performance claramente reflete nisso. Espero mais de voc� da pr�xima vez.\r\n\r\n#bNota da Folia de Monstros : " + shiu);
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
                        cm.sendOk("Parab�ns pela sua vit�ria!!! Que �tima performance! O grupo advers�rio n�o p�de fazer nada! Espero o mesmo bom trabalho da pr�xima vez!\r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("Parab�ns pela sua vit�ria! Isso foi impressionante! Voc� fez um bom trabalho contra o grupo advers�rio! S� mais um pouco, e voc� definitivamente vai conseguir um A na pr�xima vez. \r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("Parab�ns pela sua vit�ria. Voc� fez algumas coisas c� e l�, mas essa n�o pode ser considerada uma boa vit�ria. Espero mais de ti da pr�xima vez.\r\n\r\n#bNota da Folia de Monstros : " + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("Parab�ns pela sua vit�ria, entretanto sua performance n�o refletiu muito bem isso. Seja mais ativo na sua pr�xima participa��o da Folia de Monstros!\r\n\r\n#bNota da Folia de Monstros : " + shi);
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