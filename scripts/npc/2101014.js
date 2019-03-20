/*2101014.js - Lobby and Entrance
 * @author Jvlaple
 * For Jvlaple's AriantPQ
 */
importPackage(java.lang);
importPackage(Packages.server.expeditions);

var status = 0;
var toBan = -1;
var choice;
var arena;
var arenaName;
var type;
var map;
var exped = MapleExpeditionType.ARIANT;
var exped1 = MapleExpeditionType.ARIANT1;
var exped2 = MapleExpeditionType.ARIANT2;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (cm.getPlayer().getMapId() == 980010000) {
            var expedicao = cm.getExpedition(exped);
            var expedicao1 = cm.getExpedition(exped1);
            var expedicao2 = cm.getExpedition(exped2);
            if (status == 0) {
                var toSnd = "Você gostaria de participar do Desafio #eAriant Coliseu#n?\r\n\r\n#e#r       (Escolha uma arena)#n#k\r\n#b";

                if (cm.getClient().getChannelServer().getMapFactory().getMap(980010100).getCharacters().size() == 0 && expedicao == null) {
                    toSnd += "#L0#Comece Ariant Coliseu (1)#l\r\n";
                } else if (expedicao != null && cm.getClient().getChannelServer().getMapFactory().getMap(980010101).getCharacters().size() == 0) {
                    toSnd += "#L0#Junte-se ao Ariant Coliseu (1)  Dono (" + expedicao.getLeader().getName() + ")" + " Membros Atuais: " + cm.getNomeDosMembrosExpedition(exped) + "\r\n";
                }
                if (cm.getClient().getChannelServer().getMapFactory().getMap(980010200).getCharacters().size() == 0 && expedicao1 == null) {
                    toSnd += "#L1#Comece Ariant Coliseu (2)#l\r\n";
                } else if (expedicao1 != null && cm.getClient().getChannelServer().getMapFactory().getMap(980010201).getCharacters().size() == 0) {
                    toSnd += "#L1#Junte-se ao Ariant Coliseu (2)  Dono (" + expedicao1.getLeader().getName() + ")" + " Membros Atuais: " + cm.getNomeDosMembrosExpedition(exped1) + "\r\n";
                }
                if (cm.getClient().getChannelServer().getMapFactory().getMap(980010300).getCharacters().size() == 0 && expedicao2 == null) {
                    toSnd += "#L2#Comece Ariant Coliseu (3)#l\r\n";
                } else if (expedicao2 != null && cm.getClient().getChannelServer().getMapFactory().getMap(980010301).getCharacters().size() == 0) {
                    toSnd += "#L2#Junte-se ao Ariant Coliseu (3)  Dono (" + expedicao2.getLeader().getName() + ")" + " Membros Atuais: " + cm.getNomeDosMembrosExpedition(exped2) + "\r\n";
                }
                if (toSnd.equals("Você gostaria de participar do Desafio #eAriant Coliseu#n?\r\n\r\n#e#r       (Escolha uma arena)#n#k\r\n#b")) {
                    cm.sendOk("Todas as arenas esta ocupadas agora. Eu sugiro que você volte mais tarde ou mudar de canal.");
                    cm.dispose();
                } else {
                    cm.sendSimple(toSnd);
                }
            } else if (status == 1) {
                switch (selection) {
                    case 0 :
                        exped = MapleExpeditionType.ARIANT;
                        expedicao = cm.getExpedition(exped);
                        map = 980010100;
                        break;
                    case 1 :
                        exped = MapleExpeditionType.ARIANT1;
                        expedicao = cm.getExpedition(exped);
                        map = 980010200;
                        break;
                    case 2 :
                        exped = MapleExpeditionType.ARIANT2;
                        expedicao = cm.getExpedition(exped);
                        map = 980010300;
                        break;
                    default :
                        exped = null;
                        map = 0;
                        return;
                        break;
                }
                if (expedicao == null) {
                    cm.createExpedition(exped);
                    cm.warp(map, 0);
                    cm.getPlayer().dropMessage("Sua Arena foi criada. Aguarde as pessoas entrarem agora!");
                    cm.dispose();
                } else {
                    var playerAdd = expedicao.addMemberInt(cm.getPlayer());
                    if (playerAdd == 3) {
                        cm.sendOk("Desculpe, a Lobby esta cheia agora.");
                        cm.dispose();
                    } else {
                        if (playerAdd == 0) {
                            cm.warp(map, 0);
                            cm.dispose();
                        } else if (playerAdd == 2) {
                            cm.sendOk("Desculpe, mas o líder pediu para nao ser autorizado a entrar.");
                            cm.dispose();
                        } else {
                            cm.sendOk("erro.");
                            cm.dispose();
                        }
                    }
                } 
            }
        }
    }
}