
var status = 0;
var rnk = -1;
var n1 = 50; //???
var n2 = 40; //??? ???
var n3 = 7; //35
var n4 = 10; //40
var n5 = 20; //50

// Ronan's custom ore refiner NPC
var refineRocks = true;     // enables moon rock, star rock
var refineCrystals = true;  // enables common crystals
var refineSpecials = true;  // enables lithium, special crystals
var feeMultiplier = 7.0;

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
                cm.sendNext("Eu espero que voc� tenha divertido na Folia dos Monstros!");
            } else if (status > 0) {
                cm.warp(980000000, 0);
                cm.dispose();
            }
        } else if (cm.getChar().getMap().isCPQLoserMap()) {
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
                var talk = "O que gostaria de fazer? Se voc� nunca participou da Folia de Monstros, voc� precisar� saber de algumas coisas antes de participar.\r\n#b#L0# Ir para o campo da Folia de Monstros 1.#l\r\n#L3# Ir para o campo da Folia de Monstros 2.#l\r\n#L1# Aprender sobre a Folia de Monstros.#l\r\n#L2# Trocar #t4001129#.#l";
                if (Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
                    talk += "\r\n#L4# ... Can I just refine my ores?#l";
                }
                cm.sendSimple(talk);
            } else if (status == 1) {
                if (selection == 0) {
                    if ((cm.getLevel() > 29 && cm.getLevel() < 51) || cm.getPlayer().isGM()) {
                        cm.getChar().saveLocation("MONSTER_CARNIVAL");
                        cm.warp(980000000, 0);
                        cm.dispose();
                        return;
                    } else if (cm.getLevel() < 30) {
                        cm.sendOk("Voc� precisa ser no m�nimo n�vel 30 para participar da Folia de Monstros. Fale comigo quando for forte o bastante.");
                        cm.dispose();
                        return;
                    } else {
                        cm.sendOk("Sinto muito, mas apenas os jogadores de n�vel 30~50 podem participar da Folia de Monstros.");
                        cm.dispose();
                        return;
                    }
                } else if (selection == 1) {
                    status = 60;
                    cm.sendSimple("O que gostaria de fazer?\r\n#b#L0# O que � a Folia de Monstros?#l\r\n#L1# Vis�o geral sobre a Folia de Monstros#l\r\n#L2# Informa��es detalhadas sobre a Folia de Monstros#l\r\n#L3# Nada, de verdade. Mudei de ideia.#l");
                } else if (selection == 2) {
                    cm.sendSimple("Lembre-se se voc� possui #t4001129#, voc� pode troc�-las por itens. Tenha certeza que voc� possui #t4001129# suficientes para o item que voc� deseja. Selecione o item que voc� gostaria de troc�-las! \r\n#b#L0# #t1122007#(" + n1 + " moedas)#l\r\n#L1# #t2041211#(" + n2 + " moedas)#l\r\n#L2# Armas para Guerreiros#l\r\n#L3# Armas para Bruxos#l\r\n#L4# Armas para Arqueiros#l\r\n#L5# Armas para Gatunos#l");
                } else if (selection == 3) {
                    cm.getChar().saveLocation("MONSTER_CARNIVAL");
                    cm.warp(980030000, 0);
                    cm.dispose();
                    return;
                } else if (selection == 4) {
                    var selStr = "Very well, instead I offer a steadfast #bore refining#k service for you, taxing #r" + ((feeMultiplier * 100) | 0) + "%#k over the usual fee to synthetize them. What will you do?#b";

                    var options = new Array("Refine mineral ores","Refine jewel ores");
                    if(refineCrystals) {
                        options.push("Refine crystal ores");
                    }
                    if(refineRocks) {
                        options.push("Refine plates/jewels");
                    }

                    for (var i = 0; i < options.length; i++){
                        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
                    }

                    cm.sendSimple(selStr);
                    
                    status = 76;
                }
            } else if (status == 2) {
                select = selection;
                if (select == 0) {
                    if (cm.haveItem(4001129, n1) && cm.canHold(4001129)) {
                        cm.gainItem(1122007, 1);
                        cm.gainItem(4001129, -n1);
                        cm.dispose();
                    } else {
                        cm.sendOk("Verifique e veja se est�o faltando #b#t4001129##k ou se seu invent�rio de Equipamentos est� cheio.");
                        cm.dispose();
                    }
                } else if (select == 1) {
                    if (cm.haveItem(4001129, n2) && cm.canHold(2041211)) {
                        cm.gainItem(2041211, 1);
                        cm.gainItem(4001129, -n2);
                        cm.dispose();
                    } else {
                        cm.sendOk("Verifique e veja se est�o faltando #b#t4001129##k ou se seu invent�rio de Uso est� cheio.");
                        cm.dispose();
                    }
                } else if (select == 2) {//S2 Warrior 26 S3 Magician 6 S4 Bowman 6 S5 Thief 8
                    status = 10;
                    cm.sendSimple("Por favor tenha certeza que voc� possui #t4001129# para a arma que voc� deseja. Selecione a arma que voc� gostaria de trocar #t4001129# por. As op��es que tenho s�o realmente boas, e eu n�o sou eu que falo � o povo que diz! \r\n#b#L0# #z1302004#(" + n3 + " moedas)#l\r\n#L1# #z1402006#(" + n3 + " moedas)#l\r\n#L2# #z1302009#(" + n4 + " moedas)#l\r\n#L3# #z1402007#(" + n4 + " moedas)#l\r\n#L4# #z1302010#(" + n5 + " moedas)#l\r\n#L5# #z1402003#(" + n5 + " moedas)#l\r\n#L6# #z1312006#(" + n3 + " moedas)#l\r\n#L7# #z1412004#(" + n3 + " moedas)#l\r\n#L8# #z1312007#(" + n4 + " moedas)#l\r\n#L9# #z1412005#(" + n4 + " moedas)#l\r\n#L10# #z1312008#(" + n5 + " moedas)#l\r\n#L11# #z1412003#(" + n5 + " moedas)#l\r\n#L12# Ir para a pr�xima p�gina(1/2)#l");
                } else if (select == 3) {
                    status = 20;
                    cm.sendSimple("Selecione a arma que voc� gostaria de trocar. As armas que eu tenho aqui s�o extremamente atraentes. Veja voc� mesmo! \r\n#b#L0# #z1372001#(" + n3 + " moedas)#l\r\n#L1# #z1382018#(" + n3 + " moedas)#l\r\n#L2# #z1372012#(" + n4 + "moedas)#l\r\n#L3# #z1382019#(" + n4 + "moedas)#l\r\n#L4# #z1382001#(" + n5 + " moedas)#l\r\n#L5# #z1372007#(" + n5 + " moedas)#l");
                } else if (select == 4) {
                    status = 30;
                    cm.sendSimple("Selecione a arma que voc� gostaria de trocar. As armas que eu tenho aqui s�o extremamente atraentes. Veja voc� mesmo! \r\n#b#L0# #z1452006#(" + n3 + " moedas)#l\r\n#L1# #z1452007#(" + n4 + " moedas)#l\r\n#L2# #z1452008#(" + n5 + " moedas)#l\r\n#L3# #z1462005#(" + n3 + " moedas)#l\r\n#L4# #z1462006#(" + n4 + " moedas)#l\r\n#L5# #z1462007#(" + n5 + " moedas)#l");
                } else if (select == 5) {
                    status = 40;
                    cm.sendSimple("Selecione a arma que voc� gostaria de trocar por. As armas que eu tenho s�o da maior qualidade. Seleciona a mais atraente para voc�! \r\n#b#L0# #z1472013#(" + n3 + " moedas)#l\r\n#L1# #z1472017#(" + n4 + "moedas)#l\r\n#L2# #z1472021#(" + n5 + " moedas)#l\r\n#L3# #z1332014#(" + n3 + " moedas)#l\r\n#L4# #z1332031#(" + n4 + "moedas)#l\r\n#L5# #z1332011#(" + n4 + "moedas)#l\r\n#L6# #z1332016#(" + n5 + " moedas)#l\r\n#L7# #z1332003#(" + n5 + " moedas)#l");
                }
            } else if (status == 11) {
                if (selection == 12) {
                    cm.sendSimple("Selecione a arma que voc� gostaria de trocar. As armas que eu tenho aqui s�o extremamente �teis. D� uma olhada! \r\n#b#L0# #z1322015#(" + n3 + " moedas)#l\r\n#L1# #z1422008#(" + n3 + " moedas)#l\r\n#L2# #z1322016#(" + n4 + "moedas)#l\r\n#L3# #z1422007#(" + n4 + "moedas)#l\r\n#L4# #z1322017#(" + n5 + " moedas)#l\r\n#L5# #z1422005#(" + n5 + " moedas)#l\r\n#L6# #z1432003#(" + n3 + " moedas)#l\r\n#L7# #z1442003#(" + n3 + " moedas)#l\r\n#L8# #z1432005#(" + n4 + "moedas)#l\r\n#L9# #z1442009#(" + n4 + "moedas)#l\r\n#L10# #z1442005#(" + n5 + " moedas)#l\r\n#L11# #z1432004#(" + n5 + " moedas)#l\r\n#L12# Voltar para a p�gina inicial(2/2)#l");
                } else {
                    var item = new Array(1302004, 1402006, 1302009, 1402007, 1302010, 1402003, 1312006, 1412004, 1312007, 1412005, 1312008, 1412003);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("Voc� ou n�o possui #b#t4001129##k suficientes, ou seu invent�rio est� cheio. Verifique novamente.");
                        cm.dispose();
                    }
                }
            } else if (status == 12) {
                if (selection == 12) {
                    status = 10;
                    cm.sendSimple("Por favor tenha certeza que voc� possui #t4001129# para a arma que voc� deseja. Selecione a arma que voc� gostaria de trocar #t4001129# por. As op��es que tenho s�o realmente boas, e eu n�o sou eu que falo � o povo que diz! \r\n#b#L0# #z1302004#(" + n3 + " moedas)#l\r\n#L1# #z1402006#(" + n3 + " moedas)#l\r\n#L2# #z1302009#(" + n4 + " moedas)#l\r\n#L3# #z1402007#(" + n4 + " moedas)#l\r\n#L4# #z1302010#(" + n5 + " moedas)#l\r\n#L5# #z1402003#(" + n5 + " moedas)#l\r\n#L6# #z1312006#(" + n3 + " moedas)#l\r\n#L7# #z1412004#(" + n3 + " moedas)#l\r\n#L8# #z1312007#(" + n4 + " moedas)#l\r\n#L9# #z1412005#(" + n4 + " moedas)#l\r\n#L10# #z1312008#(" + n5 + " moedas)#l\r\n#L11# #z1412003#(" + n5 + " moedas)#l\r\n#L12# Ir para a pr�xima p�gina(1/2)#l");
                } else {
                    var item = new Array(1322015, 1422008, 1322016, 1422007, 1322017, 1422005, 1432003, 1442003, 1432005, 1442009, 1442005, 1432004);
                    var cost = new Array(n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5, n5);
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("Voc� ou n�o possui #b#t4001129##k suficientes, ou seu invent�rio est� cheio. Verifique novamente.");
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
                    cm.sendOk("Ou voc� n�o possui #b#t4001129##k suficientes, ou seu invent�rio est� cheio. Verifique novamente.");
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
                    cm.sendOk("Ou voc� n�o possui #b#t4001129##k suficientes, ou seu invent�rio est� cheio. Verifique novamente.");
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
                    cm.sendOk("Ou voc� n�o possui #b#t4001129##k suficientes, ou seu invent�rio est� cheio. Verifique novamente.");
                    cm.dispose();
                }
            } else if (status == 61) {
                select = selection;
                if (selection == 0) {
                    cm.sendNext("Haha! Eu sou Spiegelmann, o l�der dessa Folia. Eu comecei a primeira #bFolia de Monstros#k aqui, aguardando por viajantes como voc� para participar dessa extravaganza!");
                } else if (selection == 1) {
                    cm.sendNext("#bFolia de Monstros#k consiste em 2 grupos entrando no campo de batalha, e ca�ando os monstros invocados pelo outro grupo. � uma #bmiss�o de combate que determina o vitorioso pela quantia de Pontos de Folia (CP) recebidos#k.");
                } else if (selection == 2) {
                    cm.sendNext("Quando entrar no Campo da Folia, voc� ver� a janela da Folia de Monstros aparecer. Tudo que precisa fazer � #bselecionar o que voc�e quer usar, e pressionar OK#k. Muito f�cil, n�?");
                } else {
                    cm.dispose();
                }
            } else if (status == 62) {
                if (select == 0) {
                    cm.sendNext("O que � a #bFolia de Monstros#k? Hahaha! Vamos dizer que � uma experi�ncia que jamais esquecer�! � uma #bbatalha contra outros viajantes assim como voc�!#k");
                } else if (select == 1) {
                    cm.sendNext("Quando entrar no Campo da Folia, sua tarefa � #breceber CP ca�ando os monstros do grupo oposto, e usar estes CP's para distrair o grupo oposto de ca�ar monstros.#k.");
                } else if (select == 2) {
                    cm.sendNext("Assim que se acostumar com os comandos, tente usar #bas teclas TAB e F1 ~ F12#k. #bTAB alterna entre Invoca��o de Monstros/Habilidades/Protetor,#k e, #bF1~ F12 possibilita-o de acessar uma das janelas diretamente#k.");
                }
            } else if (status == 63) {
                if (select == 0) {
                    cm.sendNext("Eu sei que � muito perigoso para voc�s lutarem uns com os outros usando armas de verdade; e eu n�o sugeriria um ato t�o barb�rico. N�o meu amigo, o que eu ofere�o � competi��o. A emo��o da batalha e a emo��o de competir contra pessoas t�o fortes e motivadas. Eu ofere�o a premissa de que seu grupo e o grupo oposto ambos #binvoquem os monstros, e derrote os monstros invocados pelo grupo advers�rio. Essa � a ess�ncia da Folia de Monstros. Al�m disso, voc� pode usar Maple Coins ganhos durante a Folia de Monstros para obter novos itens e armas! #k");
                } else if (select == 1) {
                    cm.sendNext("Existem 3 maneiras de distrair o grupo advers�rio: #bInvodar um monstro, Habilidade, and Protetor#k. Vou dar-lhe um olhar mais aprofundado, se voc� quiser saber mais sobre 'Instru��es detalhadas'.");
                } else if (select == 2) {
                    cm.sendNext("#bInvocar um Monstro#k chama um monstro que ataca o grupo advers�rio, sob seu controle. Use CP para trazer um Monstro Invocado, e ele ir� aparecer na mesma �rea, atacando o grupo oposto.");
                }
            } else if (status == 64) {
                if (select == 0) {
                    cm.sendNext("Claro, n�o � t�o simples assim. Existem outras maneiras de prevenir o outro grupo de ca�ar monstros, e cabe a voc� descobrir como faz�-lo. O que acha? Interessado em uma competi��o amig�vel?");
                    cm.dispose();
                } else if (select == 1) {
                    cm.sendNext("Por favor lembre-se. Nunca � uma boa ideia guardar seus CP's. #bOs CP's que voc� usou ir�o ajudar a determinar o vencedor e o perdedor da Folia.");
                } else if (select == 2) {
                    cm.sendNext("#bHabilidade#k � uma op��o de usar habilidades tais como Escurid�o, Fraqueza, e outras para prevenir o grupo oposto de matar outros monstros. S�o necess�rios muitos CP's, mas vale muito a pena. O �nico problema � que eles n�o duram muito. Use essa t�tica com sabedoria!");
                }
            } else if (status == 65) {
                if (select == 1) {
                    cm.sendNext("Oh, e n�o se preocupe em tranformar-se em um fantasma. Na Folia de Monstros, #bvoc� n�o perder� EXP ap�s a morte#k. � realmente uma exper�ncia como nenhuma outra!");
                    cm.dispose();
                } else if (select == 2) {
                    cm.sendNext("#bProtetor#k � basicamente um item invocado que aumenta dr�sticamente as habilidades dos monstros invocados pelo seu grupo. Protetor funciona enquanto n�o for demolido pelo grupo oposto, ent�o eu surigo que voc� invoque v�rios monstros primeiro, e ent�o traga o Protetor.");
                }
            } else if (status == 66) {
                cm.sendNext("Por �ltimo, enquanto estiver na Folia de Monstros, #bvoc� n�o pode usar items/po��es de recupera��o que voc� leva por ai contigo.#k Entretanto, os monstros deixam esses items cair de vez em quando, e #bassim que peg�-los, o item ativar� imediatamente#k. � por isso que � importante saber quando pegar estes items.");
                cm.dispose();
            } else if (status == 77) {
                var allDone;

                if (selection == 0) {
                    allDone = refineItems(0); // minerals
                } else if (selection == 1) {
                    allDone = refineItems(1); // jewels
                } else if (selection == 2 && refineCrystals) {
                    allDone = refineItems(2); // crystals
                } else if (selection == 2 && !refineCrystals || selection == 3) {
                    allDone = refineRockItems(); // moon/star rock
                }

                if(allDone) {
                    cm.sendOk("Done. Thanks for showing up~.");
                } else {
                    cm.sendOk("Done. Be aware some of the items #rcould not be synthetized#k because either you have a lack of space on your ETC inventory or there's not enough mesos to cover the fee.");
                }
                cm.dispose();
            }
        }
    }
}

function getRefineFee(fee) {
    return ((feeMultiplier * fee) | 0);
}

function isRefineTarget(refineType, refineItemid) {
    if(refineType == 0) { //mineral refine
        return refineItemid >= 4010000 && refineItemid <= 4010007 && !(refineItemid == 4010007 && !refineSpecials);
    } else if(refineType == 1) { //jewel refine
        return refineItemid >= 4020000 && refineItemid <= 4020008 && !(refineItemid == 4020008 && !refineSpecials);
    } else if(refineType == 2) { //crystal refine
        return refineItemid >= 4004000 && refineItemid <= 4004004 && !(refineItemid == 4004004 && !refineSpecials);
    }
    
    return false;
}

function getRockRefineTarget(refineItemid) {
    if(refineItemid >= 4011000 && refineItemid <= 4011006) {
        return 0;
    } else if(refineItemid >= 4021000 && refineItemid <= 4021008) {
        return 1;
    }
    
    return -1;
}

function refineItems(refineType) {
    var allDone = true;
    
    var refineFees = [[300,300,300,500,500,500,800,270],[500,500,500,500,500,500,500,1000,3000],[5000,5000,5000,5000,1000000]];
    var itemCount = {};
    
    var iter = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();

        if(isRefineTarget(refineType, itemid)) {
            var ic = itemCount[itemid];
            
            if(ic != undefined) {
                itemCount[itemid] += it.getQuantity();
            } else {
                itemCount[itemid] = it.getQuantity();
            }
        }
    }
    
    for(var key in itemCount) {
        var itemqty = itemCount[key];
        var itemid = parseInt(key);
        
        var refineQty = ((itemqty / 10) | 0);
        if(refineQty <= 0) continue;
        
        while(true) {
            itemqty = refineQty * 10;
        
            var fee = getRefineFee(refineFees[refineType][(itemid % 100) | 0] * refineQty);
            if(cm.canHold(itemid + 1000, refineQty, itemid, itemqty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);
                cm.gainItem(itemid, -itemqty);
                cm.gainItem(itemid + (itemid != 4010007 ? 1000 : 1001), refineQty);
                
                break;
            } else if(refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }
    
    return allDone;
}

function refineRockItems() {
    var allDone = true;
    var minItems = [[0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0, 0, 0]];
    var minRocks = [2147483647, 2147483647];
    
    var rockItems = [4011007, 4021009];
    var rockFees = [10000, 15000];

    var iter = cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();
        var rockRefine = getRockRefineTarget(itemid);
        if(rockRefine >= 0) {
            var rockItem = ((itemid % 100) | 0);
            var itemqty = it.getQuantity();
            
            minItems[rockRefine][rockItem] += itemqty;
        }
    }
    
    for(var i = 0; i < minRocks.length; i++) {
        for(var j = 0; j < minItems[i].length; j++) {
            if(minRocks[i] > minItems[i][j]) {
                minRocks[i] = minItems[i][j];
            }
        }
        if(minRocks[i] <= 0 || minRocks[i] == 2147483647) continue;
        
        var refineQty = minRocks[i];
        while(true) {
            var fee = getRefineFee(rockFees[i] * refineQty);
            if(cm.canHold(rockItems[i], refineQty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);

                var j;
                if(i == 0) {
                    for(j = 4011000; j < 4011007; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                } else {
                    for(j = 4021000; j < 4021009; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                }
                
                break;
            } else if(refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }
    
    return allDone;
}
