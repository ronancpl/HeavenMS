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

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import static client.BuddyList.BuddyOperation.ADDED;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.AbstractMaplePacketHandler;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class BuddylistModifyHandler extends AbstractMaplePacketHandler {
    private static class CharacterIdNameBuddyCapacity extends CharacterNameAndId {
        private int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, int buddyCapacity) {
            super(id, name);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }

    private void nextPendingRequest(MapleClient c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        CharacterIdNameBuddyCapacity ret;
        try (PreparedStatement ps = con.prepareStatement("SELECT id, name, buddyCapacity FROM characters WHERE name LIKE ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                ret = null;
                if (rs.next()) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("buddyCapacity"));
                }
            }
        }
        con.close();
        return ret;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int mode = slea.readByte();
        MapleCharacter player = c.getPlayer();
        BuddyList buddylist = player.getBuddylist();
        if (mode == 1) { // add
            String addName = slea.readMapleAsciiString();
            String group = slea.readMapleAsciiString();
            if (group.length() > 16 || addName.length() < 4 || addName.length() > 13) {
                return; //hax.
            }
            BuddylistEntry ble = buddylist.get(addName);
            if (ble != null && !ble.isVisible() && group.equals(ble.getGroup())) {
                c.announce(MaplePacketCreator.serverNotice(1, "You already have \"" + ble.getName() + "\" on your Buddylist"));
            } else if (buddylist.isFull() && ble == null) {
                c.announce(MaplePacketCreator.serverNotice(1, "Your buddylist is already full"));
            } else if (ble == null) {
                try {
                    World world = c.getWorldServer();
                    CharacterIdNameBuddyCapacity charWithId;
                    int channel;
                    MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
                    if (otherChar != null) {
                        channel = c.getChannel();
                        charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), otherChar.getBuddylist().getCapacity());
                    } else {
                        channel = world.find(addName);
                        charWithId = getCharacterIdAndNameFromDatabase(addName);
                    }
                    if (charWithId != null) {
                        BuddyAddResult buddyAddResult = null;
                        if (channel != -1) {
                           buddyAddResult = world.requestBuddyAdd(addName, c.getChannel(), player.getId(), player.getName());
                        } else {
                            Connection con = DatabaseConnection.getConnection();
                            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
                            ps.setInt(1, charWithId.getId());
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) {
                                throw new RuntimeException("Result set expected");
                            } else if (rs.getInt("buddyCount") >= charWithId.getBuddyCapacity()) {
                                buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
                            }
                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
                            ps.setInt(1, charWithId.getId());
                            ps.setInt(2, player.getId());
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
                            }
                            rs.close();
                            ps.close();
                            con.close();
                        }
                        if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                            c.announce(MaplePacketCreator.serverNotice(1, "\"" + addName + "\"'s Buddylist is full"));
                        } else {
                            int displayChannel;
                            displayChannel = -1;
                            int otherCid = charWithId.getId();
                            if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
                                displayChannel = channel;
                                notifyRemoteChannel(c, channel, otherCid, ADDED);
                            } else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
                                Connection con = DatabaseConnection.getConnection();
                                try (PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 1)")) {
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, player.getId());
                                    ps.executeUpdate();
                                }
                                con.close();
                            }
                            buddylist.put(new BuddylistEntry(charWithId.getName(), group, otherCid, displayChannel, true));
                            c.announce(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
                        }
                    } else {
                        c.announce(MaplePacketCreator.serverNotice(1, "A character called \"" + addName + "\" does not exist"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                ble.changeGroup(group);
                c.announce(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
            }
        } else if (mode == 2) { // accept buddy
            int otherCid = slea.readInt();
            if (!buddylist.isFull()) {
                try {
                    int channel = c.getWorldServer().find(otherCid);//worldInterface.find(otherCid);
                    String otherName = null;
                    MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(otherCid);
                    if (otherChar == null) {
                        Connection con = DatabaseConnection.getConnection();
                        try (PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")) {
                            ps.setInt(1, otherCid);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    otherName = rs.getString("name");
                                }
                            }
                        }
                        con.close();
                    } else {
                        otherName = otherChar.getName();
                    }
                    if (otherName != null) {
                        buddylist.put(new BuddylistEntry(otherName, "Default Group", otherCid, channel, true));
                        c.announce(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
                        notifyRemoteChannel(c, channel, otherCid, ADDED);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            nextPendingRequest(c);
        } else if (mode == 3) { // delete
            int otherCid = slea.readInt();
            player.deleteBuddy(otherCid);
        }
    }

    private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel != -1) {
            c.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }
}
