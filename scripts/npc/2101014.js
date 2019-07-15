/*2101014.js - Lobby and Entrance
 * @author Jvlaple
 * For Jvlaple's AriantPQ
 */

importPackage(Packages.server.expeditions);

var status = 0;
var toBan = -1;
var choice;
var arenaType;
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
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (cm.getPlayer().getMapId() == 980010000) {
            if (cm.getLevel() > 30) {
                cm.sendOk("You are already over #rlevel 30#k, therefore you can't participate in this instance anymore.");
                cm.dispose();
                return;
            }
            
            if (status == 0) {
                var expedicao = cm.getExpedition(exped);
                var expedicao1 = cm.getExpedition(exped1);
                var expedicao2 = cm.getExpedition(exped2);
                
                var channelMaps = cm.getClient().getChannelServer().getMapFactory();
                var startSnd = "What would you like to do? \r\n\r\n\t#e#r(Choose a Battle Arena)#n#k\r\n#b";
                var toSnd = startSnd;

                if (expedicao == null) {
                    toSnd += "#L0#Battle Arena (1) (Empty)#l\r\n";
                } else if (channelMaps.getMap(980010101).getCharacters().isEmpty()) {
                    toSnd += "#L0#Join Battle Arena (1)  Owner (" + expedicao.getLeader().getName() + ")" + " Current Member: " + cm.getExpeditionMemberNames(exped) + "\r\n";
                }
                if (expedicao1 == null) {
                    toSnd += "#L1#Battle Arena (2) (Empty)#l\r\n";
                } else if (channelMaps.getMap(980010201).getCharacters().isEmpty()) {
                    toSnd += "#L1#Join Battle Arena (2)  Owner (" + expedicao1.getLeader().getName() + ")" + " Current Member: " + cm.getExpeditionMemberNames(exped1) + "\r\n";
                }
                if (expedicao2 == null) {
                    toSnd += "#L2#Battle Arena (3) (Empty)#l\r\n";
                } else if (channelMaps.getMap(980010301).getCharacters().isEmpty()) {
                    toSnd += "#L2#Join Battle Arena (3)  Owner (" + expedicao2.getLeader().getName() + ")" + " Current Member: " + cm.getExpeditionMemberNames(exped2) + "\r\n";
                }
                if (toSnd.equals(startSnd)) {
                    cm.sendOk("All the Battle Arena is currently occupied. I suggest you to come back later or change channels.");
                    cm.dispose();
                } else {
                    cm.sendSimple(toSnd);
                }
            } else if (status == 1) {
                arenaType = selection;
                expedicao = fetchArenaType();
                if (expedicao == "") {
                    cm.dispose();
                    return;
                }
                
                if (expedicao != null) {
                    enterArena(-1);
                } else {
                    cm.sendGetText("Up to how many partipants can join in this match? (2~5 people)");
                }
            } else if (status == 2) {
                var players = parseInt(cm.getText());   // AriantPQ option limit found thanks to NarutoFury (iMrSiN)
                if (isNaN(players)) {
                    cm.sendNext("Please enter a numeric limit value of allowed players in your instance.");
                    status = 0;
                } else if (players < 2) {
                    cm.sendNext("The numeric limit value should not be less than 2 players.");
                    status = 0;
                } else {
                    enterArena(players);
                } 
            }
        }
    }
}

function fetchArenaType() {
    switch (arenaType) {
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
            expedicao = "";
    }
    
    return expedicao;
}

function enterArena(arenaPlayers) {
    expedicao = fetchArenaType();
    if (expedicao == "") {
        cm.dispose();
        return;
    } else if (expedicao == null) {
        if (arenaPlayers != -1) {
            var res = cm.createExpedition(exped, true, 0, arenaPlayers);
            if (res == 0) {
                cm.warp(map, 0);
                cm.getPlayer().dropMessage("Your arena was created successfully. Wait for people to join the battle.");
            } else if (res > 0) {
                cm.sendOk("Sorry, you've already reached the quota of attempts for this expedition! Try again another day...");
            } else {
                cm.sendOk("An unexpected error has occurred when starting the expedition, please try again later.");
            }
        } else {
            cm.sendOk("An unexpected error has occurred when locating the expedition, please try again later.");
        }
        
        cm.dispose();
    } else {
        if (playerAlreadyInLobby(cm.getPlayer())) {
            cm.sendOk("Sorry, you're already inside the lobby.");
            cm.dispose();
            return;
        }

        var playerAdd = expedicao.addMemberInt(cm.getPlayer());
        if (playerAdd == 3) {
            cm.sendOk("Sorry, the lobby is full now");
            cm.dispose();
        } else {
            if (playerAdd == 0) {
                cm.warp(map, 0);
                cm.dispose();
            } else if (playerAdd == 2) {
                cm.sendOk("Sorry, the leader do not allowed you to enter.");
                cm.dispose();
            } else {
                cm.sendOk("Error.");
                cm.dispose();
            }
        }
    }
}

function playerAlreadyInLobby(player) {
    return cm.getExpedition(MapleExpeditionType.ARIANT) != null && cm.getExpedition(MapleExpeditionType.ARIANT).contains(player) ||
            cm.getExpedition(MapleExpeditionType.ARIANT1) != null && cm.getExpedition(MapleExpeditionType.ARIANT1).contains(player) ||
            cm.getExpedition(MapleExpeditionType.ARIANT2) != null && cm.getExpedition(MapleExpeditionType.ARIANT2).contains(player);
}