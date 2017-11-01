/** *  [MENTION=19862]id[/MENTION] 2042003
 *    [MENTION=806871]NPC[/MENTION] Assistant Red
 *    [MENTION=836108]Function[/MENTION] Monster Carnival Waiting Room NPC
 * @author s4nta
 */

// Relevant Monster Carnival classes
var MonsterCarnival = net.sf.odinms.server.partyquest.mcpq.MonsterCarnival;
var MCTracker = net.sf.odinms.server.partyquest.mcpq.MCTracker;
var MCParty = net.sf.odinms.server.partyquest.mcpq.MCParty;
var MCField = net.sf.odinms.server.partyquest.mcpq.MCField;
var MCTeam = net.sf.odinms.server.partyquest.mcpq.MCField.MCTeam;

// NPC variables
var status = -1;
var carnival, field;
var room = -1;

function start() {
    if (!MonsterCarnival.isLobbyMap(cm.getMapId())) {
        MCTracker.log("Assistant called on invalid map " + cm.getMapId() + " by player " + cm.getName());
        cm.sendOk("You are not authorized to do this.");
        cm.dispose();
        return;
    }
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 1) status++;
    else status--;

    if (status == 0) {
        if (cm.getParty() == null) {
            cm.warp(MonsterCarnival.MAP_LOBBY);
            cm.dispose();
            return;
        }
        options = ["#L1#Leave the room #r#e(WARNING: Abusing this will block you from future Carnival PQs)#b#n.#l",
                   "#L2#Close NPC#l"];
        if (cm.isLeader()) {
            options.unshift("#L0#View pending challenges#l");
        }

        text = "Welcome to Carnival PQ. I am #rAssistant Red#k. What can I do for you?#b\r\n";
        for (var i = 0; i < options.length; i++) {
            text += options[i];
            text += "\r\n";
        }
        cm.sendSimple(text);
    } else if (status == 1) {
        field = cm.getChar().getMCPQField();
        if (selection == 0) {
            if (!cm.isLeader()) {
                cm.sendOk("You are not authorized to do this.");
                cm.dispose();
                return;
            }
            if (!field.hasPendingRequests()) {
                cm.sendOk("There are no pending requests at this time.");
                cm.dispose();
                return;
            }
            cm.sendSimple(field.getNPCRequestString());
        } else if (selection == 1) {
            if (field != null) {
                field.deregister(true);
            } else {
                cm.warp(MonsterCarnival.MAP_EXIT);
            }
            cm.dispose();
        } else {
            cm.dispose();
        }
    } else if (status == 2) {
        var code = field.acceptRequest(selection);
        if (code == 1) {
            cm.sendOk("The challenge was accepted.");
        } else {
            cm.sendOk("An unknown error occurred.");
        }
        cm.dispose();
    }
}