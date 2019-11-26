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
import java.util.Map.Entry;

import config.YamlConfig;
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
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleFamily;
import client.MapleFamilyEntry;
import client.keybind.MapleKeyBinding;
import client.MapleMount;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.game.GameConstants;
import constants.game.ScriptableNPCConstants;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.server.coordinator.world.MapleEventRecallCoordinator;
import net.server.coordinator.session.MapleSessionCoordinator;
import org.apache.mina.core.session.IoSession;
import server.life.MobSkill;
import scripting.event.EventInstanceManager;
import tools.packets.Wedding;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    private static Set<Integer> attemptingLoginAccounts = new HashSet<>();
    
    private boolean tryAcquireAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            if (attemptingLoginAccounts.contains(accId)) {
                return false;
            }
            
            attemptingLoginAccounts.add(accId);
            return true;
        }
    }
    
    private void releaseAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            attemptingLoginAccounts.remove(accId);
        }
    }
    
    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int cid = slea.readInt();
        final Server server = Server.getInstance();
        
        if (c.tryacquireClient()) { // thanks MedicOP for assisting on concurrency protection here
            try {
                World wserv = server.getWorld(c.getWorld());
                if(wserv == null) {
                    c.disconnect(true, false);
                    return;
                }

                Channel cserv = wserv.getChannel(c.getChannel());
                if(cserv == null) {
                    c.setChannel(1);
                    cserv = wserv.getChannel(c.getChannel());

                    if(cserv == null) {
                        c.disconnect(true, false);
                        return;
                    }
                }

                MapleCharacter player = wserv.getPlayerStorage().getCharacterById(cid);
                IoSession session = c.getSession();
                
                String remoteHwid;
                if (player == null) {
                    remoteHwid = MapleSessionCoordinator.getInstance().pickLoginSessionHwid(session);
                    if (remoteHwid == null) {
                        c.disconnect(true, false);
                        return;
                    }
                } else {
                    remoteHwid = player.getClient().getHWID();
                }
                
                int hwidLen = remoteHwid.length();
                session.setAttribute(MapleClient.CLIENT_HWID, remoteHwid);
                session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid.substring(hwidLen - 8, hwidLen));
                c.setHWID(remoteHwid);
                
                if (!server.validateCharacteridInTransition(c, cid)) {
                    c.disconnect(true, false);
                    return;
                }
                
                boolean newcomer = false;
                if (player == null) {
                    try {
                        player = MapleCharacter.loadCharFromDB(cid, c, true);
                        newcomer = true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    if (player == null) { //If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
                        c.disconnect(true, false);
                        return;
                    }
                }
                c.setPlayer(player);
                c.setAccID(player.getAccountID());
                
                boolean allowLogin = true;

                /*  is this check really necessary?
                if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.LOGIN_NOTLOGGEDIN) {
                    List<String> charNames = c.loadCharacterNames(c.getWorld());
                    if(!newcomer) {
                        charNames.remove(player.getName());
                    }

                    for (String charName : charNames) {
                        if(wserv.getPlayerStorage().getCharacterByName(charName) != null) {
                            allowLogin = false;
                            break;
                        }
                    }
                }
                */
                
                int accId = c.getAccID();
                if (tryAcquireAccount(accId)) { // Sync this to prevent wrong login state for double loggedin handling
                    try {
                        int state = c.getLoginState();
                        if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                            c.setPlayer(null);
                            c.setAccID(0);

                            if (state == MapleClient.LOGIN_LOGGEDIN) {
                                c.disconnect(true, false);
                            } else {
                                c.announce(MaplePacketCreator.getAfterLoginError(7));
                            }

                            return;
                        }
                        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
                    } finally {
                        releaseAccount(accId);
                    }
                } else {
                    c.setPlayer(null);
                    c.setAccID(0);
                    c.announce(MaplePacketCreator.getAfterLoginError(10));
                    return;
                }
                
                if (!newcomer) {
                    c.setLanguage(player.getClient().getLanguage());
                    c.setCharacterSlots((byte) player.getClient().getCharacterSlots());
                    player.newClient(c);
                }

                cserv.addPlayer(player);
                wserv.addPlayer(player);
                player.setEnteredChannelWorld();

                List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
                if (buffs != null) {
                    List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = getLocalStartTimes(buffs);
                    player.silentGiveBuffs(timedBuffs);
                }

                Map<MapleDisease, Pair<Long, MobSkill>> diseases = server.getPlayerBuffStorage().getDiseasesFromStorage(cid);
                if (diseases != null) {
                    player.silentApplyDiseases(diseases);
                }

                c.announce(MaplePacketCreator.getCharInfo(player));
                if (!player.isHidden()) {
                    if(player.isGM() && YamlConfig.config.server.USE_AUTOHIDE_GM) {
                        player.toggleHide(true);
                    }
                }
                player.sendKeymap();
                player.sendQuickmap();
                player.sendMacros();

                // pot bindings being passed through other characters on the account detected thanks to Croosade dev team
                MapleKeyBinding autohpPot = player.getKeymap().get(91);
                player.announce(MaplePacketCreator.sendAutoHpPot(autohpPot != null ? autohpPot.getAction() : 0));

                MapleKeyBinding autompPot = player.getKeymap().get(92);
                player.announce(MaplePacketCreator.sendAutoMpPot(autompPot != null ? autompPot.getAction() : 0));

                player.getMap().addPlayer(player);
                player.visitMap(player.getMap());

                BuddyList bl = player.getBuddylist();
                int buddyIds[] = bl.getBuddyIds();
                wserv.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
                for (CharacterIdChannelPair onlineBuddy : wserv.multiBuddyFind(player.getId(), buddyIds)) {
                    BuddylistEntry ble = bl.get(onlineBuddy.getCharacterId());
                    ble.setChannel(onlineBuddy.getChannel());
                    bl.put(ble);
                }
                c.announce(MaplePacketCreator.updateBuddylist(bl.getBuddies()));

                c.announce(MaplePacketCreator.loadFamily(player));
                if (player.getFamilyId() > 0) {
                    MapleFamily f = wserv.getFamily(player.getFamilyId());
                    if(f != null) {
                        MapleFamilyEntry familyEntry = f.getEntryByID(player.getId());
                        if(familyEntry != null) {
                            familyEntry.setCharacter(player);
                            player.setFamilyEntry(familyEntry);
                            
                            c.announce(MaplePacketCreator.getFamilyInfo(familyEntry));
                            familyEntry.announceToSenior(MaplePacketCreator.sendFamilyLoginNotice(player.getName(), true), true);
                        } else {
                            FilePrinter.printError(FilePrinter.FAMILY_ERROR, "Player " + player.getName() + "'s family doesn't have an entry for them. (" + f.getID() + ")");
                        }
                    } else {
                        FilePrinter.printError(FilePrinter.FAMILY_ERROR, "Player " + player.getName() + " has an invalid family ID. (" + player.getFamilyId() + ")");
                        c.announce(MaplePacketCreator.getFamilyInfo(null));
                    }
                } else {
                    c.announce(MaplePacketCreator.getFamilyInfo(null));
                }
                if (player.getGuildId() > 0) {
                    MapleGuild playerGuild = server.getGuild(player.getGuildId(), player.getWorld(), player);
                    if (playerGuild == null) {
                        player.deleteGuild(player.getGuildId());
                        player.getMGC().setGuildId(0);
                        player.getMGC().setGuildRank(5);
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
                                c.announce(MaplePacketCreator.updateAllianceInfo(newAlliance, c.getWorld()));
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
                    wserv.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
                    player.updatePartyMemberHP();
                }

                MapleInventory eqpInv = player.getInventory(MapleInventoryType.EQUIPPED);
                eqpInv.lockInventory();
                try {
                    for(Item it : eqpInv.list()) {
                        player.equippedItem((Equip) it);
                    }
                } finally {
                    eqpInv.unlockInventory();
                }

                c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));

                CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
                if (pendingBuddyRequest != null) {
                    c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
                }

                c.announce(MaplePacketCreator.updateGender(player));
                player.checkMessenger();
                c.announce(MaplePacketCreator.enableReport());
                player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
                player.checkBerserk(player.isHidden());

                if (newcomer) {
                    for(MaplePet pet : player.getPets()) {
                        if(pet != null) {
                            wserv.registerPetHunger(player, player.getPetIndex(pet));
                        }
                    }
                    
                    MapleMount mount = player.getMount();   // thanks Ari for noticing a scenario where Silver Mane quest couldn't be started
                    if (mount.getItemId() != 0) {
                        player.announce(MaplePacketCreator.updateMount(player.getId(), mount, false));
                    }

                    player.reloadQuestExpirations();

                    /*
                    if (!c.hasVotedAlready()){
                        player.announce(MaplePacketCreator.earnTitleMessage("You can vote now! Vote and earn a vote point!"));
                    }
                    */
                    if (player.isGM()){
                        Server.getInstance().broadcastGMMessage(c.getWorld(), MaplePacketCreator.earnTitleMessage((player.gmLevel() < 6 ? "GM " : "Admin ") + player.getName() + " has logged in"));
                    }

                    if(diseases != null) {
                        for(Entry<MapleDisease, Pair<Long, MobSkill>> e : diseases.entrySet()) {
                            final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<>(e.getKey(), Integer.valueOf(e.getValue().getRight().getX())));
                            c.announce(MaplePacketCreator.giveDebuff(debuff, e.getValue().getRight()));
                        }
                    }
                } else {
                    if(player.isRidingBattleship()) {
                        player.announceBattleshipHp();
                    }
                }
                
                player.buffExpireTask();
                player.diseaseExpireTask();
                player.skillCooldownTask();
                player.expirationTask();
                player.questExpirationTask();
                if (GameConstants.hasSPTable(player.getJob()) && player.getJob().getId() != 2001) {
                    player.createDragon();
                }

                player.commitExcludedItems();
                showDueyNotification(c, player);

                if (player.getMap().getHPDec() > 0) player.resetHpDecreaseTask();

                player.resetPlayerRates();
                if(YamlConfig.config.server.USE_ADD_RATES_BY_LEVEL == true) player.setPlayerRates();
                player.setWorldRates();
                player.updateCouponRates();

                player.receivePartyMemberHP();

                if(player.getPartnerId() > 0) {
                    int partnerId = player.getPartnerId();
                    final MapleCharacter partner = wserv.getPlayerStorage().getCharacterById(partnerId);

                    if(partner != null && !partner.isAwayFromWorld()) {
                        player.announce(Wedding.OnNotifyWeddingPartnerTransfer(partnerId, partner.getMapId()));
                        partner.announce(Wedding.OnNotifyWeddingPartnerTransfer(player.getId(), player.getMapId()));
                    }
                }

                if (newcomer) {
                    EventInstanceManager eim = MapleEventRecallCoordinator.getInstance().recallEventInstance(cid);
                    if (eim != null) {
                        eim.registerPlayer(player);
                    }
                }
                
                if (YamlConfig.config.server.USE_NPCS_SCRIPTABLE) {
                    c.announce(MaplePacketCreator.setNPCScriptable(ScriptableNPCConstants.SCRIPTABLE_NPCS));
                }
                
                if(newcomer) player.setLoginTime(System.currentTimeMillis());
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                c.releaseClient();
            }
        } else {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
        }
    }
    
    private static void showDueyNotification(MapleClient c, MapleCharacter player) {
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pss = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT Type FROM dueypackages WHERE ReceiverId = ? AND Checked = 1 ORDER BY Type DESC");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    Connection con2 = DatabaseConnection.getConnection();
                    pss = con2.prepareStatement("UPDATE dueypackages SET Checked = 0 WHERE ReceiverId = ?");
                    pss.setInt(1, player.getId());
                    pss.executeUpdate();
                    pss.close();
                    con2.close();
                    
                    c.announce(MaplePacketCreator.sendDueyParcelNotification(rs.getInt("Type") == 1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
        long curtime = currentServerTime();
        
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
