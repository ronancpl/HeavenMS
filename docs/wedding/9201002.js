/**
---------------------------------------------------------------------------------------------------
* NPC Name: High Priest John - ID: 9201002.js
* @author Vcoc
* @author Ronan
---------------------------------------------------------------------------------------------------
 **/
importPackage(Packages.client);
importPackage(Packages.server);
importPackage(Packages.tools);

var status;
var minLevel = 10;
var maxLevel = 200;
var mySelection = -1;
var rings = Array(1112001, 1112002, 1112003, 1112005, 1112006);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (type == 1 && mode == 0)
            cm.sendOk("...");
        cm.dispose();
        return;
    }
	
    if (cm.getPlayer().getMapId() == 680000000) {
        if (status == 0) {
            cm.sendSimple("Olá #h #,\r\n#bO que você gostaria de fazer hoje?#b\r\n#L0#Eu quero me casar!#l\r\n#L1#Eu quero ver meus amigos no casamento!#l\r\n#L2#Quero trocar um Bilhete Premium de casamento por 5 convites!#l\r\n#L3#Eu quero comprar um Bilhete de Casamento!#l\r\n#L4#Gostaria de obter uma licença de casamento.#l");
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.getParty() == null) { // no party
                    cm.sendOk("Para se casar e necessario estar em grupo com a parceira(o)!");
                    cm.dispose();
                } else if (!cm.isLeader()) { // not party leader
                    cm.sendOk("Por favor, fale ao seu parceiro(a) para falar comigo.");
                    cm.dispose();
                } else {
                    var party = cm.getParty().getMembers();
                    var mapId = cm.getPlayer().getMapId();
                    var levelValid = 0;
                    var genderRight = 0;
                    var alreadyMarried = 0;
                    for (var i = 0; i < party.size(); i++) {
                        var pPlayer = party.get(i);
                        if (pPlayer.getLevel() >= minLevel && pPlayer.getLevel() <= maxLevel)
                            levelValid += 1;
                        if (pPlayer.getGender() == 0) {
                            genderRight += 1;
                        } else if (pPlayer.getGender() == 1) {
                            genderRight += 2;
                        }
                        if (pPlayer.isMarried() == 1) {
                            alreadyMarried += 1;
                        }
                    }
                    if (party.size() == 2) {
                        if (party.get(0).getGender() == 0) { // leader.
                            if (levelValid == 2 || cm.partyMembersInMap() == 2) {
                                if (genderRight == 3) {
                                    if (alreadyMarried == 0) {
                                        if (cm.haveItem(4031374, 1) && cm.haveItem(5251003, 1)) {
                                            // Kick it into action.  Slate says nothing here, just warps you in.
                                            var em = cm.getEventManager("CathedralWedding");
                                            if (em == null) {
                                                cm.sendOk("Indisponivel!");
                                            } else {
                                                // Begin the Wedding o.O
                                                em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                                                party = cm.getPlayer().getEventInstance().getPlayers();
                                                var hname = party.get(0).getName();
                                                var wname = party.get(1).getName();
                                                var StringLine = "[" + hname + " & " + wname + "] Irão se casar na Catedral, no canal (" + cm.getC().getChannel() + ").";
                                                cm.worldMessage(5, StringLine);
                                                var eim = cm.getChar().getEventInstance();
                                                eim.setProperty("husband", party.get(0).getName());
                                                eim.setProperty("wife", party.get(1).getName());
                                            }
                                        } else
                                            cm.sendOk("Você não tem os itens necessários, desculpe!");
                                    } else
                                        cm.sendOk("Você já está casado.");
                                } else
                                    cm.sendOk("O nosso servidor não suporta este tipo de casamento!");
                            } else
                                cm.sendOk("Vocês precisam estar no mesmo mapa e ser pelo menos no nível ["+minLevel+" ~ "+maxLevel+"]");
                        } else
                            cm.sendOk("Por favor faça o teu marido lider do grupo.");
                    } else
                        cm.sendOk("Somentos marido e mulher no grupo!");
                    cm.dispose();
                }
            } else if (selection == 1) {
                if (cm.haveItem(5251100, 1)) {
                    cm.sendGetText("Por favor, insira o nome de um dos membros do casamento .");
                } else {
                    cm.sendOk("Parece que o casal que você quer assistir não te deu um convite ainda.");
                    cm.dispose();
                }
            } else if (selection == 2) {
                if (cm.haveItem(5251003, 1)) {
                    cm.gainItem(5251003, -1);
                    cm.gainItem(5251100,5);
                } else {
                    cm.sendOk("Você não tem o Bilhete de Casamento Premium.");
                } 
                cm.dispose();
            } else if (selection == 3) {
                    cm.sendOk("Você pode comprar um Bilhete de Casamento com a Ria! Localizada no Mercado Livre.");
                
                cm.dispose();
            } else if (selection == 4) {
                if (cm.getPlayer().getMarriageQuestLevel() == 50) {
                    cm.sendNext("Por favor, vá visitar a Mom eo Dad em sua casa. Eles vivem em algum lugar Henesys Hunting Ground II.");
                    cm.getPlayer().addMarriageQuestLevel();
                } else if (cm.getPlayer().getMarriageQuestLevel() == 53) {
                    if (cm.haveItem(4031373, 1)) {
                        cm.sendNext("Pronto, aqui sua permissão!");
                        cm.removeAll(4031373);
                        cm.gainItem(4031374, 1);
                        cm.getPlayer().setMarriageQuestLevel(100);
                    } else {
                        cm.sendNext("Você não tem a benção de Mom e Dad!");
                    }
                } else {
                    cm.sendNext("Eu não sei o que você está falando.");
                }
                cm.dispose();
            }
        } else if (status == 2) {
            var chr = cm.getCharByName(cm.getText());
            if (chr != null) {
                if (chr.getMapId() == 680000200) {
                    var eim = chrr.getEventInstance();
                    eim.registerPlayer(cm.getPlayer());
                } else {
                    cm.sendOk("O casamento que você gostaria de participar não foi iniciado.");
                }
            } else
                cm.sendOk("Jogador não encontrado.");
            cm.dispose();
        }
    } else if (cm.getPlayer().getMapId() == 680000210) {
        var eim = cm.getPlayer().getEventInstance();

        var husbandName = eim.getProperty("husband");
        var wifeName = eim.getProperty("wife");

        var husband = cm.getCharByName(husbandName);
        var wife = cm.getCharByName(wifeName);
			
        var id = cm.getPlayer().getId();
            
        var hclicked = eim.getProperty("hclicked");
        var wclicked = eim.getProperty("hclicked");

        var otherChar = husband == cm.getPlayer() ? wife : husband;
			
        if (husband != null && wife != null) {
            if (status == 0) {
                if (id  != husband.getId() && id  != husband.getId()) {
                    cm.sendOk("Você não está se casando!");
                    cm.dispose();
                } else if (cm.getPlayer().isMarried() > 0) {
                    cm.sendOk("Você já foi casado.");
                    cm.dispose();
                } else if (hclicked == 1 && husbandName.equals(cm.getPlayer().getName())) {
                    cm.sendOk("Você já aceitou casar-se com sua esposa, pergunte à sua esposa a aceitar agora");
                    cm.dispose();
                } else if (wclicked == 1 && wifeName.equals(cm.getPlayer().getName())) {
                    cm.sendOk("Você já aceitou casar-se com o seu marido , pergunte ao seu marido a aceitar agora");
                    cm.dispose();
                } else {
                    cm.sendYesNo("Você deseja se casar com seu parceiro?\r\n\r\nIsso será uma decisão final.");
                }
            } else if (status == 1) {
                if (husband == cm.getPlayer())
                    eim.setProperty("hclicked", 1);
                else if (wife == cm.getPlayer())
                    eim.setProperty("wclicked", 1);
                else {
                    cm.sendOk("Como assim?");
                    cm.dispose();
                }

                if (eim.getProperty("hclicked") == 1 && eim.getProperty("hclicked") == 1) {
                    if (!cm.createMarriage(otherChar.getName())) {
                        cm.sendOk("O sistema não pode encontrar o seu parceiro.");
                        cm.dispose();
                        return;
                    }
                    cm.worldMessage(5, "Parabéns a "+husbandName+" e "+wifeName+". São recém-casados, você pode enchê-los de spam agora!");
                    cm.removeAll(4031374);
                    MapleInventoryManipulator.removeById(otherChar.getClient(), MapleInventoryType.USE, 4031374, otherChar.getItemQuantity(4031374, false), false, false);
                    cm.dispose();
                }
            }
        }
    }
}