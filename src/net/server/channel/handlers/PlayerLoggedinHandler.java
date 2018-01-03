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
package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.PlayerBuffValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamily;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ServerConstants;
import java.util.Collections;
import java.util.Comparator;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int cid = slea.readInt();
        final Server server = Server.getInstance();
        MapleCharacter player = c.getWorldServer().getPlayerStorage().getCharacterById(cid);
        boolean newcomer = false;
        if (player == null) {
            try {
                player = MapleCharacter.loadCharFromDB(cid, c, true);
                newcomer = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            player.newClient(c);
        }
        if (player == null) { //If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
            c.disconnect(true, false);
            return;
        }
        
        c.setPlayer(player);
        c.setAccID(player.getAccountID());
        
        int state = c.getLoginState();
        boolean allowLogin = true;
        Channel cserv = c.getChannelServer();

        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            for (String charName : c.loadCharacterNames(c.getWorld())) {
                for (Channel ch : c.getWorldServer().getChannels()) {
                    if (ch.isConnected(charName)) {
                        allowLogin = false;
                    }
                }
                break;
            }
        }
        if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
            c.setPlayer(null);
            c.announce(MaplePacketCreator.getAfterLoginError(7));
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);

        cserv.addPlayer(player);
        
        List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
        if (buffs != null) {
            List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = getLocalStartTimes(buffs);
            player.silentGiveBuffs(timedBuffs);
        }
        
        c.announce(MaplePacketCreator.getCharInfo(player));
        if (!player.isHidden()) {
            player.toggleHide(true);
        }
        player.sendKeymap();
        player.sendMacros();
        
        if(player.getKeymap().get(91) != null)
            player.announce(MaplePacketCreator.sendAutoHpPot(player.getKeymap().get(91).getAction()));
        if(player.getKeymap().get(92) != null)
            player.announce(MaplePacketCreator.sendAutoMpPot(player.getKeymap().get(92).getAction()));
        
        player.getMap().addPlayer(player);
        World world = server.getWorld(c.getWorld());
        world.getPlayerStorage().addPlayer(player);
        
        player.setAwayFromWorld(false);
            
        int buddyIds[] = player.getBuddylist().getBuddyIds();
        world.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
        for (CharacterIdChannelPair onlineBuddy : server.getWorld(c.getWorld()).multiBuddyFind(player.getId(), buddyIds)) {
            BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
            ble.setChannel(onlineBuddy.getChannel());
            player.getBuddylist().put(ble);
        }
        c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        c.announce(MaplePacketCreator.loadFamily(player));
        if (player.getFamilyId() > 0) {
            MapleFamily f = world.getFamily(player.getFamilyId());
            if (f == null) {
                f = new MapleFamily(player.getId());
                world.addFamily(player.getFamilyId(), f);
            }
            player.setFamily(f);
            c.announce(MaplePacketCreator.getFamilyInfo(f.getMember(player.getId())));
        }
        if (player.getGuildId() > 0) {
            MapleGuild playerGuild = server.getGuild(player.getGuildId(), player.getWorld(), player);
            if (playerGuild == null) {
                player.deleteGuild(player.getGuildId());
                player.getMGC().setGuildId(0);
            } else {
                playerGuild.getMGC(player.getId()).setCharacter(player);
                player.setMGC(playerGuild.getMGC(player.getId()));
                server.setGuildMemberOnline(player, true, c.getChannel());
                c.announce(MaplePacketCreator.showGuildInfo(player));
                int allianceId = player.getGuild().getAllianceId();
                if (allianceId > 0) {
                    MapleAlliance newAlliance = server.getAlliance(allianceId);
                    if (newAlliance == null) {
                        newAlliance = MapleAlliance.loadAlliance(allianceId);
                        if (newAlliance != null) {
                            server.addAlliance(allianceId, newAlliance);
                        } else {
                            player.getGuild().setAllianceId(0);
                        }
                    }
                    if (newAlliance != null) {
                        c.announce(MaplePacketCreator.updateAllianceInfo(newAlliance, c));
                        c.announce(MaplePacketCreator.allianceNotice(newAlliance.getId(), newAlliance.getNotice()));
                        
                        if (newcomer) {
                            server.allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, true), player.getId(), -1);
                        }
                    }
                }
            }
        }

        player.showNote();
        if (player.getParty() != null) {
            MaplePartyCharacter pchar = player.getMPC();
            
            //Use this in case of enabling party HPbar HUD when logging in, however "you created a party" will appear on chat.
            //c.announce(MaplePacketCreator.partyCreated(pchar));
            
            pchar.setChannel(c.getChannel());
            pchar.setMapId(player.getMapId());
            pchar.setOnline(true);
            world.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
            player.updatePartyMemberHP();
        }
        
        if (player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
            player.equipPendantOfSpirit();
        }
        c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
        
        if(newcomer) {
            for(MaplePet pet : player.getPets()) {
                if(pet != null)
                    world.registerPetHunger(player, player.getPetIndex(pet));
            }
            
            player.reloadQuestExpirations();
        }
        
        c.announce(MaplePacketCreator.updateGender(player));
        player.checkMessenger();
        c.announce(MaplePacketCreator.enableReport());
        player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
        player.checkBerserk(player.isHidden());
        player.buffExpireTask();
        player.diseaseExpireTask();
        player.skillCooldownTask();
        player.expirationTask();
        player.questExpirationTask();
        if (GameConstants.hasSPTable(player.getJob()) && player.getJob().getId() != 2001) {
                player.createDragon();
        }
        
        player.commitExcludedItems();
        
        if (newcomer){
            /*
            if (!c.hasVotedAlready()){
            	player.announce(MaplePacketCreator.earnTitleMessage("You can vote now! Vote and earn a vote point!"));
            }
                    */
            if (player.isGM()){
            	Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.earnTitleMessage((player.gmLevel() < 6 ? "GM " : "Admin ") + player.getName() + " has logged in"));
            }
            
        }
        
        showDueyNotification(c, player);
        
        if (player.getMap().getHPDec() > 0) player.resetHpDecreaseTask();
        
        player.resetPlayerRates();
        if(ServerConstants.USE_ADD_RATES_BY_LEVEL == true) player.setPlayerRates();
        player.setWorldRates();
        player.updateCouponRates();
        
        player.receivePartyMemberHP();
    }
    
    private static void showDueyNotification(MapleClient c, MapleCharacter player) {
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pss = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    Connection con2 = DatabaseConnection.getConnection();
                    pss = con2.prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
                    pss.setInt(1, player.getId());
                    pss.executeUpdate();
                    pss.close();
                    con2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                c.announce(MaplePacketCreator.sendDueyNotification(false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pss != null) {
                    pss.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static List<Pair<Long, PlayerBuffValueHolder>> getLocalStartTimes(List<PlayerBuffValueHolder> lpbvl) {
        List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = new ArrayList<>();
        long curtime = System.currentTimeMillis();
        
        for(PlayerBuffValueHolder pb : lpbvl) {
            timedBuffs.add(new Pair<>(curtime - pb.usedTime, pb));
        }
        
        Collections.sort(timedBuffs, new Comparator<Pair<Long, PlayerBuffValueHolder>>() {
            @Override
            public int compare(Pair<Long, PlayerBuffValueHolder> p1, Pair<Long, PlayerBuffValueHolder> p2) {
                return p1.getLeft().compareTo(p2.getLeft());
            }
        });
        
        return timedBuffs;
    }
}
