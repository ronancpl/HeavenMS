importPackage(Packages.server.maps);

var status = 0;
var rnk = -1;
var n1 = 50; //???
var n2 = 40; //??? ???
var n3 = 7; //35
var n4 = 10; //40
var n5 = 20; //50

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (cm.getPlayer().getMapId() == 980000010) {
            if (status == 0) {
                cm.sendNext("Eu espero que você tinha divertido na Folia dos Monstros!");
            } else if (status > 0) {
                cm.warp(980000000, 0);
                cm.dispose();
            }
        } else if (cm.getChar().getMap().isCPQLoserMap()) {
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
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 10:
                        cm.warp(980000000, 0);
                        cm.gainExp(17500);
                        cm.dispose();
                        break;
                    case 20:
                        cm.warp(980000000, 0);
                        cm.gainExp(1200);
                        cm.dispose();
                        break;
                    case 30:
                        cm.warp(980000000, 0);
                        cm.gainExp(5000);
                        cm.dispose();
                        break;
                    case 40:
                        cm.warp(980000000, 0);
                        cm.gainExp(2500);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
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
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 1:
                        cm.warp(980000000, 0);
                        cm.gainExp(50000);
                        cm.dispose();
                        break;
                    case 2:
                        cm.warp(980000000, 0);
                        cm.gainExp(25500);
                        cm.dispose();
                        break;
                    case 3:
                        cm.warp(980000000, 0);
                        cm.gainExp(21000);
                        cm.dispose();
                        break;
                    case 4:
                        cm.warp(980000000, 0);
                        cm.gainExp(19505);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else {
            if (status == 0) {
               // cm.sendSimple("O que gostaria de fazer? Se você nunca participou da Folia de Monstros, você precisará saber de algumas coisas antes de participar.\r\n#b#L0# Ir para o campo da Folia de Monstros 1.#l\r\n#L1# Aprender sobre a Folia de Monstros.#l\r\n#L2# Trocar #t4001129#.#l");
                cm.sendSimple("O que gostaria de fazer? Se você nunca participou da Folia de Monstros, você precisará saber de algumas coisas antes de participar.\r\n#b#L0# Ir para o campo da Folia de Monstros 1.#l\r\n#L3# Ir para o campo da Folia de Monstros 2.#l\r\n#L1# Aprender sobre a Folia de Monstros.#l\r\n#L2# Trocar #t4001129#.#l");
            } else if (status == 1) {
                if (selection == 0) {
                    if ((cm.getLevel() > 29 && cm.getLevel() < 51) || cm.getPlayer().isGM()) {
                        cm.getChar().saveLocation("MONSTER_CARNIVAL");
                        cm.warp(980000000, 0);
                        cm.dispose();
                        return;
                    } else if (cm.getLevel() < 30) {
                        cm.sendOk("Você precisa ser no mínimo nível 30 para participar da Folia de Monstros. Fale comigo quando for forte o bastante.");
                        cm.dispose();
                        return;
                    } else {
                        cm.sendOk("Sinto muito, mas apenas os jogadores de nível 30~50 podem participar da Folia de Monstros.");
                        cm.dispose();
                        return;
                    }
                } else if (selection == 1) {
                    status = 60;
                    cm.sendSimple("O que gostaria de fazer?\r\n#b#L0# O que é a Folia de Monstros?#l\r\n#L1# Visão geral sobre a Folia de Monstros#l\r\n#L2# Informações detalhadas sobre a Folia de Monstros#l\r\n#L3# Nada, de verdade. Mudei de ideia.#l");
                } else if (selection == 2) {
                    cm.sendSimple("Lembre-se se você possui #t4001129#, você pode trocá-las por itens. Tenha certeza que você possui #t4001129# suficientes para o item que você deseja. Selecione o item que você gostaria de trocá-las! \r\n#b#L0# #t1122007#(" + n1 + " moedas)#l\r\n#L1# #t2041211#(" + n2 + " moedas)#l\r\n#L2# Armas para Guerreiros#l\r\n#L3# Armas para Bruxos#l\r\n#L4# Armas para Arqueiros#l\r\n#L5# Armas para Gatunos#l");
                } else if (selection == 3) {
                    cm.getChar().saveLocation("MONSTER_CARNIVAL");
                    cm.warp(980030000, 0);
                    cm.dispose();
                    return;
                }

            } else if (status == 2) {
                select = selection;
                if (select == 0) {
                    if (cm.haveItem(4001129, n1) && cm.canHold(4001129)) {
                        cm.gainItem(1122007, 1);
                        cm.gainItem(4001129, -n1);
                        cm.dispose();
                    } else {
                        cm.sendOk("Verifique e veja se estão faltando #b#t4001129##k ou se seu inventário de Equipamentos está cheio.");
                        cm.dispose();
                    }
                } else if (select == 1) {
                    if (cm.haveItem(4001129, n2) && cm.canHold(2041211)) {
                        cm.gainItem(2041211, 1);
                        cm.gainItem(4001129, -n2);
                        cm.dispose();
                    } else {
                        cm.sendOk("Verifique e veja se estão faltando #b#t4001129##k ou se seu inventário de Uso está cheio.");
                        cm.dispose();
                    }
                } else if (select == 2) {//S2 Warrior 26 S3 Magician 6 S4 Bowman 6 S5 Thief 8
                    status = 10;
                    cm.sendSimple("Por favor tenha certeza que você possui #t4001129# para a arma que você deseja. Selecione a arma que você gostaria de trocar #t4001129# por. As opções que tenho são realmente boas, e eu não sou eu que falo é o povo que diz! \r\n#b#L0# #z1302004#(" + n3 + " moedas)#l\r\n#L1# #z1402006#(" + n3 + " moedas)#l\r\n#L2# #z1302009#(" + n4 + " moedas)#l\r\n#L3# #z1402007#(" + n4 + " moedas)#l\r\n#L4# #z1302010#(" + n5 + " moedas)#l\r\n#L5# #z1402003#(" + n5 + " moedas)#l\r\n#L6# #z1312006#(" + n3 + " moedas)#l\r\n#L7# #z1412004#(" + n3 + " moedas)#l\r\n#L8# #z1312007#(" + n4 + " moedas)#l\r\n#L9# #z1412005#(" + n4 + " moedas)#l\r\n#L10# #z1312008#(" + n5 + " moedas)#l\r\n#L11# #z1412003#(" + n5 + " moedas)#l\r\n#L12# Ir para a próxima página(1/2)#l");
                } else if (select == 3) {
                    status = 20;
                    cm.sendSimple("Selecione a arma que você gostaria de trocar. As armas que eu tenho aqui são extremamente atraentes. Veja você mesmo! \r\n#b#L0# #z1372001#(" + n3 + " moedas)#l\r\n#L1# #z1382018#(" + n3 + " moedas)#l\r\n#L2# #z1372012#(" + n4 + "moedas)#l\r\n#L3# #z1382019#(" + n4 + "moedas)#l\r\n#L4# #z1382001#(" + n5 + " moedas)#l\r\n#L5# #z1372007#(" + n5 + " moedas)#l");
                } else if (select == 4) {
                    status = 30;
                    cm.sendSimple("Selecione a arma que você gostaria de trocar. As armas que eu tenho aqui são extremamente atraentes. Veja você mesmo! \r\n#b#L0# #z1452006#(" + n3 + " moedas)#l\r\n#L1# #z1452007#(" + n4 + " moedas)#l\r\n#L2# #z1452008#(" + n5 + " moedas)#l\r\n#L3# #z1462005#(" + n3 + " moedas)#l\r\n#L4# #z1462006#(" + n4 + " moedas)#l\r\n#L5# #z1462007#(" + n5 + " moedas)#l");
                } else if (select == 5) {
                    status = 40;
                    cm.sendSimple("Selecione a arma que você gostaria de trocar por. As armas que eu tenho são da maior qualidade. Seleciona a mais atraente para você! \r\n#b#L0# #z1472013#(" + n3 + " moedas)#l\r\n#L1# #z1472017#(" + n4 + "moedas)#l\r\n#L2# #z1472021#(" + n5 + " moedas)#l\r\n#L3# #z1332014#(" + n3 + " moedas)#l\r\n#L4# #z1332031#(" + n4 + "moedas)#l\r\n#L5# #z1332011#(" + n4 + "moedas)#l\r\n#L6# #z1332016#(" + n5 + " moedas)#l\r\n#L7# #z1332003#(" + n5 + " moedas)#l");
                }
            } else if (status == 11) {
                if (selection == 12) {
                    cm.sendSimple("Selecione a arma que você gostaria de trocar. As armas que eu tenho aqui são extremamente úteis. Dá uma olhada! \r\n#b#L0# #z1322015#(" + n3 + " moedas)#l\r\n#L1# #z1422008#(" + n3 + " moedas)#l\r\n#L2# #z1322016#(" + n4 + "moedas)#l\r\n#L3# #z1422007#(" + n4 + "moedas)#l\r\n#L4# #z1322017#(" + n5 + " moedas)#l\r\n#L5# #z1422005#(" + n5 + " moedas)#l\r\n#L6# #z1432003#(" + n3 + " moedas)#l\r\n#L7# #z1442003#(" + n3 + " moedas)#l\r\n#L8# #z1432005#(" + n4 + "moedas)#l\r\n#L9# #z1442009#(" + n4 + "moedas)#l\r\n#L10# #z1442005#(" + n5 + " moedas)#l\r\n#L11# #z1432004#(" + n5 + " moedas)#l\r\n#L12# Voltar para a página inicial(2/2)#l");
                } else {
                    var item = new Array(1302004, 1402006, 1302009, 1402007, 1302010, 1402003, 1312006, 1412004, 1312007, 1412005, 1312008, 1412003);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("Você ou não possui #b#t4001129##k suficientes, ou seu inventário está cheio. Verifique novamente.");
                        cm.dispose();
                    }
                }
            } else if (status == 12) {
                if (selection == 12) {
                    status = 10;
                    cm.sendSimple("Por favor tenha certeza que você possui #t4001129# para a arma que você deseja. Selecione a arma que você gostaria de trocar #t4001129# por. As opções que tenho são realmente boas, e eu não sou eu que falo é o povo que diz! \r\n#b#L0# #z1302004#(" + n3 + " moedas)#l\r\n#L1# #z1402006#(" + n3 + " moedas)#l\r\n#L2# #z1302009#(" + n4 + " moedas)#l\r\n#L3# #z1402007#(" + n4 + " moedas)#l\r\n#L4# #z1302010#(" + n5 + " moedas)#l\r\n#L5# #z1402003#(" + n5 + " moedas)#l\r\n#L6# #z1312006#(" + n3 + " moedas)#l\r\n#L7# #z1412004#(" + n3 + " moedas)#l\r\n#L8# #z1312007#(" + n4 + " moedas)#l\r\n#L9# #z1412005#(" + n4 + " moedas)#l\r\n#L10# #z1312008#(" + n5 + " moedas)#l\r\n#L11# #z1412003#(" + n5 + " moedas)#l\r\n#L12# Ir para a próxima página(1/2)#l");
                } else {
                    var item = new Array(1322015, 1422008, 1322016, 1422007, 1322017, 1422005, 1432003, 1442003, 1432005, 1442009, 1442005, 1432004);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("Você ou não possui #b#t4001129##k suficientes, ou seu inventário está cheio. Verifique novamente.");
                        cm.dispose();
                    }
                }
            } else if (status == 21) {
                var item = new Array(1372001, 1382018, 1372012, 1382019, 1382001, 1372007);
                var cost = new Array(n3, n3, n4, n4, n5, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("Ou você não possui #b#t4001129##k suficientes, ou seu inventário está cheio. Verifique novamente.");
                    cm.dispose();
                }
            } else if (status == 31) {
                var item = new Array(1452006, 1452007, 1452008, 1462005, 1462006, 1462007);
                var cost = new Array(n3, n4, n5, n3, n4, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("Ou você não possui #b#t4001129##k suficientes, ou seu inventário está cheio. Verifique novamente.");
                    cm.dispose();
                }
            } else if (status == 41) {
                var item = new Array(1472013, 1472017, 1472021, 1332014, 1332031, 1332011, 1332016, 1332003);
                var cost = new Array(n3, n4, n5, n3, n4, n4, n5, n5);
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("Ou você não possui #b#t4001129##k suficientes, ou seu inventário está cheio. Verifique novamente.");
                    cm.dispose();
                }
            } else if (status == 61) {
                select = selection;
                if (selection == 0) {
                    cm.sendNext("Haha! Eu sou Spiegelmann, o líder dessa Folia. Eu comecei a primeira #bFolia de Monstros#k aqui, aguardando por viajantes como você para participar dessa extravaganza!");
                } else if (selection == 1) {
                    cm.sendNext("#bFolia de Monstros#k consiste em 2 grupos entrando no campo de batalha, e caçando os monstros invocados pelo outro grupo. É uma #bmissão de combate que determina o vitorioso pela quantia de Pontos de Folia (CP) recebidos#k.");
                } else if (selection == 2) {
                    cm.sendNext("Quando entrar no Campo da Folia, você verá a janela da Folia de Monstros aparecer. Tudo que precisa fazer é #bselecionar o que vocêe quer usar, e pressionar OK#k. Muito fácil, né?");
                } else {
                    cm.dispose();
                }
            } else if (status == 62) {
                if (select == 0) {
                    cm.sendNext("O que é a #bFolia de Monstros#k? Hahaha! Vamos dizer que é uma experiência que jamais esquecerá! É uma #bbatalha contra outros viajantes assim como você!#k");
                } else if (select == 1) {
                    cm.sendNext("Quando entrar no Campo da Folia, sua tarefa é #breceber CP caçando os monstros do grupo oposto, e usar estes CP's para distrair o grupo oposto de caçar monstros.#k.");
                } else if (select == 2) {
                    cm.sendNext("Assim que se acostumar com os comandos, tente usar #bas teclas TAB e F1 ~ F12#k. #bTAB alterna entre Invocação de Monstros/Habilidades/Protetor,#k e, #bF1~ F12 possibilita-o de acessar uma das janelas diretamente#k.");
                }
            } else if (status == 63) {
                if (select == 0) {
                    cm.sendNext("Eu sei que é muito perigoso para vocês lutarem uns com os outros usando armas de verdade; e eu não sugeriria um ato tão barbárico. Não meu amigo, o que eu ofereço é competição. A emoção da batalha e a emoção de competir contra pessoas tão fortes e motivadas. Eu ofereço a premissa de que seu grupo e o grupo oposto ambos #binvoquem os monstros, e derrote os monstros invocados pelo grupo adversário. Essa é a essência da Folia de Monstros. Além disso, você pode usar Maple Coins ganhos durante a Folia de Monstros para obter novos itens e armas! #k");
                } else if (select == 1) {
                    cm.sendNext("Existem 3 maneiras de distrair o grupo adversário: #bInvodar um monstro, Habilidade, and Protetor#k. Vou dar-lhe um olhar mais aprofundado, se você quiser saber mais sobre 'Instruções detalhadas'.");
                } else if (select == 2) {
                    cm.sendNext("#bInvocar um Monstro#k chama um monstro que ataca o grupo adversário, sob seu controle. Use CP para trazer um Monstro Invocado, e ele irá aparecer na mesma área, atacando o grupo oposto.");
                }
            } else if (status == 64) {
                if (select == 0) {
                    cm.sendNext("Claro, não é tão simples assim. Existem outras maneiras de prevenir o outro grupo de caçar monstros, e cabe a você descobrir como fazê-lo. O que acha? Interessado em uma competição amigável?");
                    cm.dispose();
                } else if (select == 1) {
                    cm.sendNext("Por favor lembre-se. Nunca é uma boa ideia guardar seus CP's. #bOs CP's que você usou irão ajudar a determinar o vencedor e o perdedor da Folia.");
                } else if (select == 2) {
                    cm.sendNext("#bHabilidade#k é uma opção de usar habilidades tais como Escuridão, Fraqueza, e outras para prevenir o grupo oposto de matar outros monstros. São necessários muitos CP's, mas vale muito a pena. O único problema é que eles não duram muito. Use essa tática com sabedoria!");
                }
            } else if (status == 65) {
                if (select == 1) {
                    cm.sendNext("Oh, e não se preocupe em tranformar-se em um fantasma. Na Folia de Monstros, #bvocê não perderá EXP após a morte#k. É realmente uma experência como nenhuma outra!");
                    cm.dispose();
                } else if (select == 2) {
                    cm.sendNext("#bProtetor#k é basicamente um item invocado que aumenta drásticamente as habilidades dos monstros invocados pelo seu grupo. Protetor funciona enquanto não for demolido pelo grupo oposto, então eu surigo que você invoque vários monstros primeiro, e então traga o Protetor.");
                }
            } else if (status == 66) {
                cm.sendNext("Por último, enquanto estiver na Folia de Monstros, #bvocê não pode usar items/poções de recuperação que você leva por ai contigo.#k Entretanto, os monstros deixam esses items cair de vez em quando, e #bassim que pegá-los, o item ativará imediatamente#k. É por isso que é importante saber quando pegar estes items.");
                cm.dispose();
            }
        }
    }
}

