/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.npc;

import java.io.File;
import java.sql.SQLException;

import constants.ServerConstants;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import scripting.AbstractPlayerInteraction;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.MapleShopFactory;
import server.events.gm.MapleEvent;
import server.gachapon.MapleGachapon;
import server.gachapon.MapleGachapon.MapleGachaponItem;
import server.life.MaplePlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.quest.MapleQuest;
import tools.LogHelper;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleSkinColor;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MaplePet;
import constants.ItemConstants;
import constants.LinguaConstants;
import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.server.channel.Channel;
import scripting.event.EventInstanceManager;
import server.MapleSkillbookInformationProvider;
import server.MapleSkillbookInformationProvider.SkillBookEntry;
import server.TimerManager;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.partyquest.MonsterCarnival;
import tools.FilePrinter;
import tools.packets.Wedding;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private int npc;
    private int npcOid;
    private String scriptName;
    private String getText;
    private boolean itemScript;
    private List<MaplePartyCharacter> otherParty;

    public NPCConversationManager(MapleClient c, int npc, String scriptName) {
        this(c, npc, -1, scriptName, false);
    }

    public NPCConversationManager(MapleClient c, int npc, List<MaplePartyCharacter> otherParty, boolean test) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.otherParty = otherParty;
    }

    public NPCConversationManager(MapleClient c, int npc, int oid, String scriptName, boolean itemScript) {
        super(c);
        this.npc = npc;
        this.npcOid = oid;
        this.scriptName = scriptName;
        this.itemScript = itemScript;
    }

    public int getNpc() {
        return npc;
    }

    public int getNpcObjectId() {
        return npcOid;
    }

    public String getScriptName() {
        return scriptName;
    }

    public boolean isItemScript() {
        return itemScript;
    }

    public void resetItemScript() {
        this.itemScript = false;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
        getClient().announce(MaplePacketCreator.enableActions());
    }

    public void sendNext(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public void sendYesNo(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().announce(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().announce(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalkText(npc, text, ""));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().announce(MaplePacketCreator.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public int getJobId() {
        return getPlayer().getJob().getId();
    }

    public MapleJob getJob() {
        return getPlayer().getJob();
    }

    public void startQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void completeQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public boolean forceStartQuest(int id) {
        return MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
    }

    public boolean forceCompleteQuest(int id) {
        return MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
    }

    public void startQuest(int id) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void completeQuest(int id) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void startQuest(short id, int npcId) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npcId);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void startQuest(int id, int npcId) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npcId);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void completeQuest(short id, int npcId) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void completeQuest(int id, int npcId) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    @Override
    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(effect, 3));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(MapleSkinColor.getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    public boolean canSpawnPlayerNpc(int mapid) {
        MapleCharacter chr = getPlayer();
        return !ServerConstants.PLAYERNPC_AUTODEPLOY && chr.getLevel() >= chr.getMaxClassLevel() && !chr.isGM() && MaplePlayerNPC.canSpawnPlayerNpc(chr.getName(), mapid);
    }

    public MaplePlayerNPC getPlayerNPCByScriptid(int scriptId) {
        for (MapleMapObject pnpcObj : getPlayer().getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
            MaplePlayerNPC pn = (MaplePlayerNPC) pnpcObj;

            if (pn.getScriptId() == scriptId) {
                return pn;
            }
        }

        return null;
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet != null) {
                pet.gainClosenessFullness(getPlayer(), closeness, 0, 0);
            }
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void changeJob(MapleJob job) {
        getPlayer().changeJob(job);
    }

    public MapleJob getJobName(int id) {
        return MapleJob.getById(id);
    }

    public MapleStatEffect getItemEffect(int itemId) {
        return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void openShopNPC(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(c);
    }

    public void maxMastery() {
        for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                Skill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                break;
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                continue;
            }
        }
    }

    public void doGachapon() {
        int[] maps = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};

        MapleGachaponItem item = MapleGachapon.getInstance().process(npc);

        Item itemGained = gainItem(item.getId(), (short) (item.getId() / 10000 == 200 ? 100 : 1), true, true); // For normal potions, make it give 100.

        sendNext("You have obtained a #b#t" + item.getId() + "##k.");

        String map = c.getChannelServer().getMapFactory().getMap(maps[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapName();

        LogHelper.logGacha(getPlayer(), item.getId(), map);

        if (item.getTier() > 0) { //Uncommon and Rare
            Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.gachaponMessage(itemGained, map, getPlayer()));
        }
    }

    public void upgradeAlliance() {
        MapleAlliance alliance = Server.getInstance().getAlliance(c.getPlayer().getGuild().getAllianceId());
        alliance.increaseCapacity(1);

        Server.getInstance().allianceMessage(alliance.getId(), MaplePacketCreator.getGuildAlliances(alliance, c.getWorld()), -1, -1);
        Server.getInstance().allianceMessage(alliance.getId(), MaplePacketCreator.allianceNotice(alliance.getId(), alliance.getNotice()), -1, -1);
    }

    public void disbandAlliance(MapleClient c, int allianceId) {
        MapleAlliance.disbandAlliance(allianceId);
    }

    public boolean canBeUsedAllianceName(String name) {
        return MapleAlliance.canBeUsedAllianceName(name);
    }

    public MapleAlliance createAlliance(String name) {
        return MapleAlliance.createAlliance(getParty(), name);
    }

    public int getAllianceCapacity() {
        return Server.getInstance().getAlliance(getPlayer().getGuild().getAllianceId()).getCapacity();
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        if (getPlayer().getMerchantMeso() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showFredrick() {
        c.announce(MaplePacketCreator.getFredrick(getPlayer()));
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public MapleEvent getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }

    public MapleCharacter getMapleCharacter(String player) {
        MapleCharacter target = Server.getInstance().getWorld(c.getWorld()).getChannel(c.getChannel()).getPlayerStorage().getCharacterByName(player);
        return target;
    }

    public void logLeaf(String prize) {
        LogHelper.logLeaf(getPlayer(), true, prize);
    }

    public boolean createPyramid(String mode, boolean party) {//lol
        PyramidMode mod = PyramidMode.valueOf(mode);

        MapleParty partyz = getPlayer().getParty();
        MapleMapFactory mf = c.getChannelServer().getMapFactory();

        MapleMap map = null;
        int mapid = 926010100;
        if (party) {
            mapid += 10000;
        }
        mapid += (mod.getMode() * 1000);

        for (byte b = 0; b < 5; b++) {//They cannot warp to the next map before the timer ends (:
            map = mf.getMap(mapid + b);
            if (map.getCharacters().size() > 0) {
                continue;
            } else {
                break;
            }
        }

        if (map == null) {
            return false;
        }

        if (!party) {
            partyz = new MapleParty(-1, new MaplePartyCharacter(getPlayer()));
        }
        Pyramid py = new Pyramid(partyz, mod, map.getId());
        getPlayer().setPartyQuest(py);
        py.warp(mapid);
        dispose();
        return true;
    }

    public boolean itemExists(int itemid) {
        return MapleItemInformationProvider.getInstance().getName(itemid) != null;
    }

    public int getCosmeticItem(int itemid) {
        if (itemExists(itemid)) {
            return itemid;
        }

        int baseid;
        if (itemid < 30000) {
            baseid = (itemid / 1000) * 1000 + (itemid % 100);
        } else {
            baseid = (itemid / 10) * 10;
        }

        return itemid != baseid && itemExists(baseid) ? baseid : -1;
    }

    private int getEquippedItemid(int itemid) {
        if (itemid < 30000) {
            return getPlayer().getFace();
        } else {
            return getPlayer().getHair();
        }
    }

    public boolean isCosmeticEquipped(int itemid) {
        return getEquippedItemid(itemid) == itemid;
    }

    public boolean isUsingOldPqNpcStyle() {
        return ServerConstants.USE_OLD_GMS_STYLED_PQ_NPCS && this.getPlayer().getParty() != null;
    }

    public Object[] getAvailableMasteryBooks() {
        return MapleItemInformationProvider.getInstance().usableMasteryBooks(this.getPlayer()).toArray();
    }

    public Object[] getAvailableSkillBooks() {
        return MapleItemInformationProvider.getInstance().usableSkillBooks(this.getPlayer()).toArray();
    }

    public Object[] getNamesWhoDropsItem(Integer itemId) {
        return MapleItemInformationProvider.getInstance().getWhoDrops(itemId).toArray();
    }

    public String getSkillBookInfo(int itemid) {
        SkillBookEntry sbe = MapleSkillbookInformationProvider.getInstance().getSkillbookAvailability(itemid);
        return sbe != SkillBookEntry.UNAVAILABLE ? "    Obtainable through #rquestline#k." : "";
    }

    
    // By Drago/Dragohe4rt CPQ + WED
    
    public int calcAvgLvl(int map) {
        int num = 0;
        int avg = 0;
        for (MapleMapObject mmo
                : c.getChannelServer().getMapFactory().getMap(map).getAllPlayer()) {
            avg += ((MapleCharacter) mmo).getLevel();
            num++;
        }
        avg /= num;
        return avg;
    }

    public void sendCPQMapLists() {
        String msg = LinguaConstants.Linguas(getPlayer()).CPQInicioEscolha;
        for (int i = 0; i < 6; i++) {
            if (fieldTaken(i)) {
                if (fieldLobbied(i)) {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (nível: "
                            + calcAvgLvl(980000100 + i * 100) + " / "
                            + getPlayerCount(980000100 + i * 100) + "x"
                            + getPlayerCount(980000100 + i * 100) + ")  #l\\r\\n";
                } else {
                    continue;
                }
            } else {
                if (i == 0 || i == 1 || i == 2 || i == 3) {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (2x2) #l\\r\\n";
                } else {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (3x3) #l\\r\\n";
                }
            }
        }
        sendSimple(msg);
    }

    public boolean fieldTaken(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000101 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000102 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean fieldLobbied(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public void cpqLobby(int field) {
        try {
            final MapleMap map, mapsaida;
            Channel cs = c.getChannelServer();
            map = cs.getMapFactory().getMap(980000100 + 100 * field);
            mapsaida = cs.getMapFactory().getMap(980000000);
            for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                final MapleCharacter mc;
                mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                if (mc != null) {
                    mc.changeMap(map, map.getPortal(0));
                    mc.getClient().getSession().write(MaplePacketCreator.serverNotice(6, LinguaConstants.Linguas(mc).CPQEntradaLobby));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new Runnable() {
                        @Override
                        public void run() {
                            mapClock(3 * 60);
                        }
                    }, 1500);
                }
                mc.timer = TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        mc.changeMap(mapsaida, mapsaida.getPortal(0));
                    }
                }, 3 * 60 * 1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MapleCharacter getChrById(int id) {
        Channel cs = c.getChannelServer();
        return cs.getPlayerStorage().getCharacterById(id);
    }

    public void cancelarSaida() {
        Channel cs = c.getChannelServer();
        for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
            MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
            if (mc.timer != null) {
                mc.timer.cancel(true);
                mc.timer = null;
            }
        }
    }

    public void startCPQ(final MapleCharacter challenger, int field) {
        try {
            cancelarSaida();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("NÃ£o existe oponente!");
                }
                for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
                    MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
                    if (mc != null) {
                        mc.changeMap(getPlayer().getMap(), getPlayer().getMap().getPortal(0));
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(new Runnable() {
                            @Override
                            public void run() {
                                mapClock(10);
                            }
                        }, 1500);
                    }
                }
                for (MaplePartyCharacter mpc : getPlayer().getParty().getMembers()) {
                    MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
                    if (mc != null) {
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(new Runnable() {
                            @Override
                            public void run() {
                                mapClock(10);
                            }
                        }, 1500);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 1;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    Channel cs = c.getChannelServer();
                    for (MaplePartyCharacter mpc : getPlayer().getParty().getMembers()) {
                        MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                        mc.setMonsterCarnival(null);
                    }
                    for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
                        MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                        mc.setMonsterCarnival(null);
                    }
                    new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid, true);
                }
            }, 11000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCPQ2(final MapleCharacter challenger, int field) {
        try {
            cancelarSaida();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("Não existe oponente!");
                }
                for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
                    MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
                    if (mc != null) {
                        mc.changeMap(getPlayer().getMap(), getPlayer().getMap().getPortal(0));
                        mapClock(10);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 100;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(new Runnable() {
                @Override
                public void run() {
                    Channel cs = c.getChannelServer();
                    for (MaplePartyCharacter mpc : getPlayer().getParty().getMembers()) {
                        MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                        mc.setMonsterCarnival(null);
                    }
                    for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
                        MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                        mc.setMonsterCarnival(null);
                    }
                    new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid, false);
                }
            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCPQMapLists2() {
        String msg = LinguaConstants.Linguas(getPlayer()).CPQInicioEscolha;
        for (int i = 0; i < 3; i++) {
            if (fieldTaken2(i)) {
                if (fieldLobbied2(i)) {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (Nível: "
                            + calcAvgLvl(980031000 + i * 1000) + "#l\\r\\n";
                } else {
                    continue;
                }
            } else {
                if (i == 0 || i == 1) {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (2x2) #l\\r\\n";
                } else {
                    msg += "#b#L" + i + "#Map " + (i + 1) + " (3x3) #l\\r\\n";
                }
            }
        }
        sendSimple(msg);
    }

    public boolean fieldTaken2(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean fieldLobbied2(int field) {
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        return false;
    }

    public void cpqLobby2(int field) {
        try {
            final MapleMap map, mapsaida;
            Channel cs = c.getChannelServer();
            mapsaida = cs.getMapFactory().getMap(980030000);
            map = cs.getMapFactory().getMap(980031000 + 1000 * field);
            for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                final MapleCharacter mc;
                mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                if (mc != null) {
                    mc.changeMap(map, map.getPortal(0));
                    mc.getClient().getSession().write(MaplePacketCreator.serverNotice(6, LinguaConstants.Linguas(mc).CPQEntradaLobby));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new Runnable() {
                        @Override
                        public void run() {
                            mapClock(3 * 60);
                        }
                    }, 1500);
                }
                mc.timer = TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        mc.changeMap(mapsaida, mapsaida.getPortal(0));
                    }
                }, 3 * 60 * 1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void challengeParty2(int field) {
        MapleCharacter leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980031000 + 1000 * field);
        for (MapleMapObject mmo : map.getAllPlayer()) {
            MapleCharacter mc = (MapleCharacter) mmo;
            if (mc.getParty() == null) {
                sendOk(LinguaConstants.Linguas(mc).CPQEscolha);
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                List<MaplePartyCharacter> members = new LinkedList<>();
                for (MaplePartyCharacter fucker : c.getPlayer().getParty().getMembers()) {
                    members.add(fucker);
                }
                NPCScriptManager.getInstance().start("cpqchallenge2", leader.getClient(), npc, members);
            } else {
                sendOk(LinguaConstants.Linguas(leader).CPQInicioEscolhaEmEscolha);
            }
        } else {
            sendOk(LinguaConstants.Linguas(leader).CPQLiderNaoEncontrado);
        }
    }

    public void mapClock(int time) {
        //getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.getClock(time));
    }

    public void challengeParty(int field) {
        MapleCharacter leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980000100 + 100 * field);
        if (map.getAllPlayer().size() != getPlayer().getParty().getMembers().size()) {
            sendOk("erro");
            return;
        }
        for (MapleMapObject mmo : map.getAllPlayer()) {
            MapleCharacter mc = (MapleCharacter) mmo;
            if (mc.getParty() == null) {
                sendOk(LinguaConstants.Linguas(mc).CPQEscolha);
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                List<MaplePartyCharacter> members = new LinkedList<>();
                for (MaplePartyCharacter fucker : c.getPlayer().getParty().getMembers()) {
                    members.add(fucker);
                }
                NPCScriptManager.getInstance().start("cpqchallenge", leader.getClient(), npc, members);
            } else {
                sendOk(LinguaConstants.Linguas(leader).CPQInicioEscolhaEmEscolha);
            }
        } else {
            sendOk(LinguaConstants.Linguas(leader).CPQLiderNaoEncontrado);
        }
    }

    public MapleCharacter getCharByName(String namee) {
        try {
            return getClient().getChannelServer().getPlayerStorage().getCharacterByName(namee);
        } catch (Exception e) {
            return null;
        }
    }

    public void enviarLista() {
        EventInstanceManager eim = getEventInstance();
        if(eim != null) {
            String name = eim.getProperty("brideId");
            MapleCharacter chr = getChrById(Integer.parseInt(name));
            //MapleCharacter chr = getChrById(3);
            if (chr != null) {
                if (chr.getId() == getPlayer().getId()) {
                    getPlayer().announce(Wedding.OnWeddingGiftResult((byte) 0xA, chr.getItens(), chr.getItem()));
                } else {
                    getPlayer().announce(Wedding.OnWeddingGiftResult((byte) 0x09, chr.getItens(), chr.getItem()));
                }
            }
        }
    }
    
    public void criarLista() {
        getClient().getSession().write(Wedding.sendWishList());
    }

}
