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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import server.events.gm.MapleEvent;
import server.gachapon.MapleGachapon;
import server.gachapon.MapleGachapon.MapleGachaponItem;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.quest.MapleQuest;
import tools.LogHelper;
import tools.DatabaseConnection;
import tools.FilePrinter;
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
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ExpTable;
import constants.ServerConstants;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

	private int npc;
	private String scriptName;
	private String getText;

	public NPCConversationManager(MapleClient c, int npc, String scriptName) {
		super(c);
		this.npc = npc;
		this.scriptName = scriptName;
	}

	public int getNpc() {
		return npc;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void dispose() {
		NPCScriptManager.getInstance().dispose(this);
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

	public MapleJob getJob(){
		return getPlayer().getJob();
	}

	public void startQuest(short id) {
		try {
			MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
		} catch (NullPointerException ex) {
		}
	}

	public void completeQuest(short id) {
		try {
			MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
		} catch (NullPointerException ex) {
		}
	}

	public void startQuest(int id) {
		try {
			MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
		} catch (NullPointerException ex) {
		}
	}

	public void completeQuest(int id) {
		try {
			MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
		} catch (NullPointerException ex) {
		}
	}

	public int getMeso() {
		return getPlayer().getMeso();
	}

	public void gainMeso(int gain) {
		if (gain > 0 && ServerConstants.USE_AUTOBAN == true) {
			FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " gained " + gain + " mesos from NPC " + npc + "\r\n");
		}
		getPlayer().gainMeso(gain, true, false, true);
	}

	public void gainExp(int gain) {
		getPlayer().gainExp(gain, true, true);
	}

	public int getLevel() {
		return getPlayer().getLevel();
	}

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
		return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
	}

	public void displayGuildRanks() {
		MapleGuild.displayGuildRanks(getClient(), npc);
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
			if (pet.getCloseness() > 30000) {
				pet.setCloseness(30000);
				return;
			}
			pet.gainCloseness(closeness);
			while (pet.getCloseness() > ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
				pet.setLevel((byte) (pet.getLevel() + 1));
				byte index = getPlayer().getPetIndex(pet);
				getClient().announce(MaplePacketCreator.showOwnPetLevelUp(index));
				getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showPetLevelUp(getPlayer(), index));
			}
			Item petz = getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
			getPlayer().forceUpdateItem(petz);
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

	public void changeJob(MapleJob job){
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

	public void maxMastery() {
		for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
			try {
				Skill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
				getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
			} catch (NumberFormatException nfe) {
				break;
			} catch (NullPointerException npe) {
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
		
		if (item.getTier() > 0){ //Uncommon and Rare
			Server.getInstance().broadcastMessage(MaplePacketCreator.gachaponMessage(itemGained, map, getPlayer()));
		}
	}

	public void disbandAlliance(MapleClient c, int allianceId) {
		PreparedStatement ps = null;
		try {
			ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM `alliance` WHERE id = ?");
			ps.setInt(1, allianceId);
			ps.executeUpdate();
			ps.close();
			Server.getInstance().allianceMessage(c.getPlayer().getGuild().getAllianceId(), MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
			Server.getInstance().disbandAlliance(allianceId);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			try {
				if (ps != null && !ps.isClosed()) {
					ps.close();
				}
			} catch (SQLException ex) {
			}
		}
	}

	public boolean canBeUsedAllianceName(String name) {
		if (name.contains(" ") || name.length() > 12) {
			return false;
		}
		try {
			ResultSet rs;
			try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM alliance WHERE name = ?")) {
				ps.setString(1, name);
				rs = ps.executeQuery();
				if (rs.next()) {
					ps.close();
					rs.close();
					return false;
				}
			}
			rs.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static MapleAlliance createAlliance(MapleCharacter chr1, MapleCharacter chr2, String name) {
		int id;
		int guild1 = chr1.getGuildId();
		int guild2 = chr2.getGuildId();
		try {
			try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `alliance` (`name`, `guild1`, `guild2`) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, name);
				ps.setInt(2, guild1);
				ps.setInt(3, guild2);
				ps.executeUpdate();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					rs.next();
					id = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		MapleAlliance alliance = new MapleAlliance(name, id, guild1, guild2);
		try {
			Server.getInstance().setGuildAllianceId(guild1, id);
			Server.getInstance().setGuildAllianceId(guild2, id);
			chr1.setAllianceRank(1);
			chr1.saveGuildStatus();
			chr2.setAllianceRank(2);
			chr2.saveGuildStatus();
			Server.getInstance().addAlliance(id, alliance);
			Server.getInstance().allianceMessage(id, MaplePacketCreator.makeNewAlliance(alliance, chr1.getClient()), -1, -1);
		} catch (Exception e) {
			return null;
		}
		return alliance;
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
		MapleCharacter target =  Server.getInstance().getWorld(c.getWorld()).getChannel(c.getChannel()).getPlayerStorage().getCharacterByName(player);
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
}
