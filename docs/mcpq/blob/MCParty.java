/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.partyquest.mcpq;

import community.MapleParty;
import community.MaplePartyCharacter;
import handling.channel.ChannelServer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import packet.creators.CarnivalPackets;
import packet.creators.PacketCreator;
import packet.transfer.write.OutPacket;
import client.player.Player;
import client.player.buffs.Disease;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.Field;
import server.partyquest.mcpq.MCField.MCTeam;



/**
 * 
 * @author Sammy Guergachi <sguergachi at gmail.com>
 */
/**
 * Provides an interface for Monster Carnival-specific party methods and variables.
 *
 * @author s4nta
 */
public class MCParty {

    private MapleParty party;
    private List<Player> characters = new ArrayList<>();
    private int availCP = 0;
    private int totalCP = 0;
    private MCField.MCTeam team = MCField.MCTeam.NONE;
    private MCField field;
    private MCParty enemy;

    public MCParty(MapleParty party) {
        this.party = party;
        for (MaplePartyCharacter chr : party.getMembers()) {
            if (!chr.isOnline()) continue;
            Player c = ChannelServer.getInstance(chr.getChannel()).getPlayerStorage().getCharacterById(chr.getId());

            characters.add(c);
        }
    }

    public int getSize() {
        return this.characters.size();
    }

    /**
     * Checks if the underlying MapleParty still exists in the same way it did when it was created.
     * That is, if there were no players who left the party.
     *
     * [MENTION=850422]return[/MENTION] True if the underlying MapleParty still exists in its original format.
     */
    public boolean exists() {
        Collection<Player> members = getMembers();
        for (Player chr : members) {
            if (chr.getParty() == null || chr.getParty() != this.party) {
                return false;
            }
        }
        return true;
    }

    public int getAverageLevel() {
        int sum = 0, num = 0;
        for (Player chr : getMembers()) {
            sum += chr.getLevel();
            num += 1;
        }
        return sum / num;
    }

    public boolean checkLevels() {
        if (MonsterCarnival.DEBUG) {
            return true;
        }
        for (Player chr : getMembers()) {
            int lv = chr.getLevel();
            if (lv < MonsterCarnival.MIN_LEVEL || lv > MonsterCarnival.MAX_LEVEL) {
                return false;
            }
        }
        return true;
    }

    public boolean checkChannels() {
        if (MonsterCarnival.DEBUG) {
            return true;
        }
        for (Player chr : getMembers()) {
            if (chr.getClient().getChannel() != party.getLeader().getChannel()) return false;
        }
        return true;
    }

    public boolean checkMaps() {
        if (MonsterCarnival.DEBUG) {
            return true;
        }
        for (Player chr : getMembers()) {
            if (chr.getMapId() != MonsterCarnival.MAP_LOBBY) return false;
        }
        return true;
    }

    public void warp(int map) {
        for (Player chr : getMembers()) {
            chr.changeMap(map);
        }
    }

    public void warp(Field map) {
        for (Player chr : getMembers()) {
            chr.changeMap(map, map.getPortal(0));
        }
    }

    public void warp(Field map, String portal) {
        for (Player chr : getMembers()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public void warp(MCField.MCMaps type) {
        Field m = this.field.getMap(type);
        for (Player chr : getMembers()) {
            chr.changeMap(m, m.getPortal(0));
        }
    }

    public void clock(int secs) {
        for (Player chr : getMembers()) {
            chr.getClient().announce(PacketCreator.GetClock(secs));
        }
    }

    public void notice(String msg) {
        broadcast(PacketCreator.ServerNotice(6, msg));
    }

    public void broadcast(OutPacket pkt) {
        for (Player chr : getMembers()) {
            chr.getClient().announce(pkt);
        }
    }

    /**
     * Sets MCPQTeam, MCPQParty, and MCPQField for a given character.
     * [MENTION=2000183830]para[/MENTION]m chr Character to update.
     */
    public void updatePlayer(Player chr) {
        chr.setMCPQTeam(this.team);
        chr.setMCPQParty(this);
        chr.setMCPQField(this.field);
    }

    /**
     * Sets MCPQTeam, MCPQParty, and MCPQ field for all characters in the party.
     * Unlike deregisterPlayers, this method does NOT warp players to the lobby map.
     */
    public void updatePlayers() {
        for (Player chr : getMembers()) {
            this.updatePlayer(chr);
        }
    }

    /**
     * Resets MCPQ variables for a given character.
     * [MENTION=2000183830]para[/MENTION]m chr Character to reset.
     */
    public static void deregisterPlayer(Player chr) {
        chr.setMCPQTeam(MCTeam.NONE);
        chr.setMCPQParty(null);
        chr.setMCPQField(null);

        chr.setAvailableCP(0);
        chr.setTotalCP(0);
    }

    /**
     * Resets MCPQ variables for all characters in the party.
     * Unlike updatePlayers, this method DOES warp players to the lobby map.
     */
    public void deregisterPlayers() {
        for (Player chr : getMembers()) {
            MCParty.deregisterPlayer(chr);
            chr.changeMap(MonsterCarnival.MAP_EXIT);
        }
    }

    public void removePlayer(Player chr) {
        characters.remove(chr);
        deregisterPlayer(chr);
    }

    public void startBattle() {
        for (Player chr : characters) {
            chr.getClient().getSession().write(CarnivalPackets.StartMonsterCarnival(chr));
        }
    }

    /**
     * Uses some amount of available CP.
     * [MENTION=2000183830]para[/MENTION]m use A positive integer to be subtracted from available CP.
     */
    public void loseCP(int use) {
        // TODO: locks?
        if (use < 0) {
            System.err.println("Attempting to use negative CP.");
        }
        this.availCP -= use;
    }

    public void gainCP(int gain) {
        // TODO: locks?
        this.availCP += gain;
        this.totalCP += gain;
    }

    public MCParty getEnemy() {
        return enemy;
    }

    public void setEnemy(MCParty enemy) {
        this.enemy = enemy;
    }

    /**
     * Applies a MCSkill to the entire team. This is used on the team's own players
     * because it is called when the enemy team uses a debuff/cube of darkness.
     * [MENTION=2000183830]para[/MENTION]m skill Skill to apply.
     * [MENTION=850422]return[/MENTION] True if skill was applied, false otherwise.
     */
    public boolean applyMCSkill(MCSkill skill) {
        MobSkill s = MobSkillFactory.getMobSkill(skill.getMobSkillID(), skill.getLevel());
        Disease disease = Disease.getType(skill.getMobSkillID());
        if (disease == null) {
            disease = Disease.DARKNESS;
            s = MobSkillFactory.getMobSkill(121, 6); // HACK: darkness
        } else if (disease == Disease.POISON) {
            return false;
        }

        // We only target players on the battlefield map.
        if (skill.getTarget() == 2) {
            for (Player chr : getMembers()) {
                if (MonsterCarnival.isBattlefieldMap(chr.getMapId())) {
                    chr.giveDebuff(disease, s);
                }
            }
            return true;
        } else {
            if (getRandomMember() != null) {
                getRandomMember().giveDebuff(disease, 1, 30000L, disease.getDisease(), 1);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setField(MCField field) {
        this.field = field;
    }

    public void setTeam(MCTeam newTeam) {
        this.team = newTeam;
    }

    public MCTeam getTeam() {
        return team;
    }

    /**
     * Returns a collection of online members in the party.
     * [MENTION=850422]return[/MENTION] Online MCParty members.
     */
    public Collection<Player> getMembers() {
        return this.characters;
    }

    public Player getRandomMember() {
        List<Player> chrsOnMap = new ArrayList<>();
        for (Player chr : this.characters) {
            if (MonsterCarnival.isBattlefieldMap(chr.getMapId())) {
                chrsOnMap.add(chr);
            }
        }
        if (chrsOnMap.isEmpty()) {
            return null;
        }
        return chrsOnMap.get(new Random().nextInt(chrsOnMap.size()));
    }

    public int getAvailableCP() {
        return availCP;
    }

    public int getTotalCP() {
        return totalCP;
    }
}  