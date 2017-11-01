/**  *  [MENTION=19862]id[/MENTION] 2042000
 *    [MENTION=806871]NPC[/MENTION] Spiegelmann
 *    [MENTION=836108]Function[/MENTION] Monster Carnival Lobby NPC
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
    if (cm.getMapId() != 980000000) {
        MCTracker.log("Spiegelmann called on invalid map " + cm.getMapId() + " by player " + cm.getName());
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
            cm.sendOk("You are not in a party.");
            cm.dispose();
            return;
        } else if (!cm.isLeader()) {
            cm.sendOk("If you want to try Carnival PQ, please tell the #bleader of your party#k to talk to me.");
            cm.dispose();
            return;
        }
        carnival = MonsterCarnival.getMonsterCarnival(cm.getChannel());
        cm.sendSimple(carnival.getNPCAvailableFields());
    } else if (status == 1) {
        room = selection;
        if (room < 1 || room > 6) {
            cm.sendOk("That is not a valid room.");
            cm.dispose();
            return;
        }
        var code = carnival.registerStatus(cm.getParty(), selection);
        if (code == MonsterCarnival.STATUS_FIELD_FULL) {
            cm.sendOk("This room is currently full.")
        } else if (code == MonsterCarnival.STATUS_PARTY_SIZE) {
            cm.sendOk("Your party is not the right size for this field.");
        } else if (code == MonsterCarnival.STATUS_PARTY_LEVEL) {
            cm.sendOk("Please check to see that the members in your party are between level 30 and 50.");
        } else if (code == MonsterCarnival.STATUS_PARTY_MISSING) {
            cm.sendOk("Please make sure everyone in your party is in this lobby.");
        } else if (code == MonsterCarnival.STATUS_FIELD_INVALID) {
            cm.sendOk("Unauthorized request.");
        }

        if (code == MonsterCarnival.STATUS_PROCEED) {
            field = carnival.getField(room);
            party = carnival.createParty(cm.getParty());
            field.register(party, MCTeam.RED);
            cm.sendOk("You will have 3 minutes to accept challenges from other parties.");
        } else if (code == MonsterCarnival.STATUS_REQUEST) {
            cm.sendOk("Sending request to room " + room + ". You will be automatically warped in if they accept your challenge.");
            field = carnival.getField(room);
            party = carnival.createParty(cm.getParty());
            field.request(party);
        }
        cm.dispose();
    }
}