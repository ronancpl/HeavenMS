/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.partyquest.mcpq;

import community.MapleParty;
import handling.channel.ChannelServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.LoggerFactory;
import client.player.Player;
import client.player.buffs.Disease;


/**
 * 
 * @author Sammy Guergachi <sguergachi at gmail.com>
 */
/**
 * Processes game logic for Monster Carnival PQ.
 *
 * TODO: Display IGNs/Jobs/Level in Pending Requests
 * TODO: fix reactor handling and make it less hacky
 * TODO: fix cube of darkness and make it less hacky
 *
 * @author s4nta
 */
public class MonsterCarnival {

    // Logger
    static org.slf4j.Logger log = LoggerFactory.getLogger(MonsterCarnival.class);

    // Map of channel to a MonsterCarnival instance.
    private static final HashMap<Integer, MonsterCarnival> instances = new HashMap<>();

    /**
     * Returns the MonsterCarnival instance for a channel. Creates a new one and maps it
     * if it does not exist.
     *  [MENTION=2000183830]para[/MENTION]m channel Channel to check for.
     *  [MENTION=850422]return[/MENTION] MonsterCarnival instance for a channel.
     * @param channel
     * @return 
     */
    public static MonsterCarnival getMonsterCarnival(int channel) {
        // TODO: synchronization?
        if (channel < 1 || channel > 20) {
            log.warn("Attempting to get a Monster Carnival instance for invalid channel.");
            return null;
        }
        if (instances.containsKey(channel)) {
            return instances.get(channel);
        }
        ChannelServer cserv = ChannelServer.getInstance(channel);
        if (cserv == null) {
            log.error("ChannelServer instance for channel " + channel + " is null.");
            return null;
        }
        MonsterCarnival inst = new MonsterCarnival(cserv);
        instances.put(channel, inst);
        return inst;
    }

    // Instance variables
    private ChannelServer cserv;
    private Map<Integer, MCField> fields = new HashMap<>();

    /**
     * Constructor for a MonsterCarnival instance.
     *  [MENTION=2000183830]para[/MENTION]m cserv Channel server for this instance.
     */
    public MonsterCarnival(ChannelServer cserv) {
        this.cserv = cserv;
        this.initFields();
    }

    /**
     * Initializes empty fields for the instance.
     */
    private void initFields() {
        for (int i = 1; i <= NUM_FIELDS; i++) {
            fields.put(i, new MCField(i, this.cserv, null, null));
        }
    }

    /**
     * Gets the field with a specified ID.
     *  [MENTION=2000183830]para[/MENTION]m id ID of field to retrieve.
     *  [MENTION=850422]return[/MENTION]
     */
    public MCField getField(int id) {
        if (id >= 1 && id <= NUM_FIELDS) {
            return fields.get(id);
        }
        return null;
    }

    /**
     * Checks if a party can join a field or not.
     *
     *  [MENTION=2000183830]para[/MENTION]m pty Party to register.
     *  [MENTION=2000183830]para[/MENTION]m room Room to join.
     *  [MENTION=850422]return[/MENTION] Different code based on status. OK if successful.
     */
    public int registerStatus(MapleParty pty, int room) {
        if (!isValidField(room)) {
            return STATUS_FIELD_INVALID;
        }
        MCField field = this.getField(room);
        if (field.isFull()) {
            return STATUS_FIELD_FULL;
        }
        MCParty party = new MCParty(pty);
        if (!sizeCheck(party.getSize(), room)) {
            return STATUS_PARTY_SIZE;
        }
        boolean levelCheck = party.checkLevels();
        if (!levelCheck) {
            return STATUS_PARTY_LEVEL;
        }
        boolean chanCheck = party.checkChannels();
        if (!chanCheck) {
            return STATUS_PARTY_MISSING;
        }
        boolean mapCheck = party.checkMaps();
        if (!mapCheck) {
            return STATUS_PARTY_MISSING;
        }
        if (field.needsRequest()) {
            return STATUS_REQUEST;
        }
        return STATUS_PROCEED;
    }

    /**
     * Creates a new MCParty based on a regular MapleParty object.
     *  [MENTION=2000183830]para[/MENTION]m pty Party to base off of.
     *  [MENTION=850422]return[/MENTION] Newly created MCParty.
     */
    public MCParty createParty(MapleParty pty) {
        return new MCParty(pty);
    }

    public void resetPlayer(Player chr) {
        MCParty.deregisterPlayer(chr);
        chr.changeMap(MAP_LOBBY);
    }

    /**
     * Returns a String containing information about lobby waiting rooms.
     *
     *  [MENTION=850422]return[/MENTION] String containing lobby information formatted for NPC.
     */
    public String getNPCAvailableFields() {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the #bCarnival PQ#k! Rooms 1-4 can hold 2-4 people, and rooms 5-6 can hold 3-6.\r\n#b");
        for (int i = 1; i <= NUM_FIELDS; i++) {
            MCField field = this.fields.get(i);
            sb.append(field.getStatus());
        }

        return sb.toString();
    }

    // Reference Information

    // Game Constants
    public static final int CP_LOSS_ON_DEATH = 10;
    public static final int TIME_PREBATTLE = 10;
    public static final int TIME_BATTLE = 600;
    public static final int TIME_LOBBYWAIT = 180;

    public static final int TAB_SPAWNS   = 0;
    public static final int TAB_DEBUFF   = 1;
    public static final int TAB_GUARDIAN = 2;

    /**
     * Gets a random debuff for (Mini) Cube of Darkness.
     *  [MENTION=850422]return[/MENTION] Random MapleDisease.
     */
    public static Disease getRandomDebuff() {
        return DEBUFFS[new Random().nextInt(DEBUFFS.length)];
    }

    /**
     * Checks party size. Information from hidden-street MCPQ page.
     *  [MENTION=2000183830]para[/MENTION]m size Size of the party.
     *  [MENTION=2000183830]para[/MENTION]m field Field to check for.
     *  [MENTION=850422]return[/MENTION] True if party size is okay, False otherwise.
     */
    public static final boolean sizeCheck(int size, int field) {
        if (DEBUG) {
            return true;
        }
        switch (field) {
            case 1:
            case 2:
            case 3:
            case 4:
               // return size >= 2 && size <= 4;
                return size >= 1 && size <= 4;
            case 5:
            case 6:
               // return size >= 3 && size <= 6;
                return size >= 1 && size <= 6;
            default:
                return false;
        }
    }

    public static final boolean isValidField(int field) {
        return field >= 1 && field <= 6;
    }

    public static final int getLobbyMap(int field) {
        if (field < 1 || field > NUM_FIELDS) {
            log.warn("Attempting to get lobby map for invalid field.");
            return MAP_EXIT;
        }
        return 980000000 + field * 100;
    }

    public static final boolean isLobbyMap(int mapid) {
        switch (mapid) {
            case 980000100:
            case 980000200:
            case 980000300:
            case 980000400:
            case 980000500:
            case 980000600:
                return true;
            default:
                return false;
        }
    }

    public static final int getBattleFieldMap(int field) {
        if (field < 1 || field > NUM_FIELDS) {
            log.warn("Attempting to get battlefield map for invalid field.");
            return MAP_EXIT;
        }
        return 980000000 + field * 100 + 1;
    }

    public static final boolean isBattlefieldMap(int mapid) {
        switch (mapid) {
            case 980000101:
            case 980000201:
            case 980000301:
            case 980000401:
            case 980000501:
            case 980000601:
                return true;
            default:
                return false;
        }
    }

    public static final int getResurrectionMap(int field) {
        if (field < 1 || field > NUM_FIELDS) {
            log.warn("Attempting to get resurrection map for invalid field.");
            return MAP_EXIT;
        }
        return 980000000 + field * 100 + 2;
    }

    public static final int getVictoriousMap(int field) {
        if (field < 1 || field > NUM_FIELDS) {
            log.warn("Attempting to get victory map for invalid field.");
            return MAP_EXIT;
        }
        return 980000000 + field * 100 + 3;
    }

    public static final int getDefeatedMap(int field) {
        if (field < 1 || field > NUM_FIELDS) {
            log.warn("Attempting to get defeat map for invalid field.");
            return MAP_EXIT;
        }
        return 980000000 + field * 100 + 4;
    }

    public static final boolean isCPQConsumeItem(int itemid) {
        switch (itemid) {
            case ITEM_CP_1:
            case ITEM_CP_2:
            case ITEM_CP_3:
            case ITEM_PTY_ELIX:
            case ITEM_PTY_PELIX:
            case ITEM_PTY_ALLC:
            case ITEM_MINICUBE:
            case ITEM_DARKCUBE:
            case ITEM_STUNNER:
            case ITEM_IND_WHITE:
            case ITEM_IND_MANA:
            case ITEM_IND_ELIX:
            case ITEM_IND_PELIX:
            case ITEM_IND_ALLC:
            case ITEM_PTY_MANA:
                return true;
        }
        return false;
    }

    // Error Codes
    // Note: These would be in an enum, but since these will be used in a NPC, they are not.
    public static final int STATUS_FIELD_FULL    = 0;
    public static final int STATUS_PARTY_SIZE    = 1;
    public static final int STATUS_PARTY_LEVEL   = 2;
    public static final int STATUS_PARTY_MISSING = 3;
    public static final int STATUS_FIELD_INVALID = 4;
    public static final int STATUS_REQUEST       = 98;
    public static final int STATUS_PROCEED       = 99;

    // Maps
    public static final int MAP_LOBBY = 980000000;
    public static final int MAP_EXIT  = 980000010;

    // NPCs
    public static final int NPC_LOBBY     = 2042000;
    public static final int NPC_ENTER     = 2042001; // Warp in from outside
    public static final int NPC_INFO      = 2042002;
    public static final int NPC_ASST_RED  = 2042003;
    public static final int NPC_ASST_BLUE = 2042004;

    // Items
    public static final int ITEM_CP_1      = 2022157;
    public static final int ITEM_CP_2      = 2022158;
    public static final int ITEM_CP_3      = 2022159;
    public static final int ITEM_PTY_MANA  = 2022160;
    public static final int ITEM_PTY_ELIX  = 2022161;
    public static final int ITEM_PTY_PELIX = 2022162;
    public static final int ITEM_PTY_ALLC  = 2022163;
    public static final int ITEM_MINICUBE  = 2022164;
    public static final int ITEM_DARKCUBE  = 2022165;
    public static final int ITEM_STUNNER   = 2022166;
    public static final int ITEM_IND_WHITE = 2022174;
    public static final int ITEM_IND_ELIX  = 2022175;
    public static final int ITEM_IND_PELIX = 2022176;
    public static final int ITEM_IND_MANA  = 2022177;
    public static final int ITEM_IND_ALLC  = 2022178;

    // Guardians
    public static final int GUARDIAN_RED = 9980000;
    public static final int GUARDIAN_BLUE = 9980001;

    // Debuffs
    public static final Disease[] DEBUFFS = {Disease.STUN, Disease.DARKNESS, Disease.WEAKEN}; // intentionally leave out a few

    // Miscellaneous
    public static final int MIN_LEVEL  = 30;
    public static final int MAX_LEVEL  = 50;
    public static final int NUM_FIELDS = 6;

    // Debug
    public static final boolean DEBUG = false;
}  
