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

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.coordinator.world.MapleInviteCoordinator;
import net.server.coordinator.world.MapleInviteCoordinator.InviteResult;
import net.server.coordinator.world.MapleInviteCoordinator.InviteType;
import net.server.coordinator.world.MapleInviteCoordinator.MapleInviteResult;
import net.server.world.MapleMessenger;
import net.server.world.MapleMessengerCharacter;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MessengerHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.tryacquireClient()) {
            try {
                String input;
                byte mode = slea.readByte();
                MapleCharacter player = c.getPlayer();
                World world = c.getWorldServer();
                MapleMessenger messenger = player.getMessenger();
                switch (mode) {
                    case 0x00:
                        int messengerid = slea.readInt();
                        if (messenger == null) {
                            if (messengerid == 0) {
                                MapleInviteCoordinator.removeInvite(InviteType.MESSENGER, player.getId());

                                MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, 0);
                                messenger = world.createMessenger(messengerplayer);
                                player.setMessenger(messenger);
                                player.setMessengerPosition(0);
                            } else {
                                messenger = world.getMessenger(messengerid);
                                if (messenger != null) {
                                    MapleInviteResult inviteRes = MapleInviteCoordinator.answerInvite(InviteType.MESSENGER, player.getId(), messengerid, true);
                                    InviteResult res = inviteRes.result;
                                    if (res == InviteResult.ACCEPTED) {
                                        int position = messenger.getLowestPosition();
                                        MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, position);
                                        if (messenger.getMembers().size() < 3) {
                                            player.setMessenger(messenger);
                                            player.setMessengerPosition(position);
                                            world.joinMessenger(messenger.getId(), messengerplayer, player.getName(), messengerplayer.getChannel());
                                        }
                                    } else {
                                        player.message("Could not verify your Maple Messenger accept since the invitation rescinded.");
                                    }
                                }
                            }
                        } else {
                            MapleInviteCoordinator.answerInvite(InviteType.MESSENGER, player.getId(), messengerid, false);
                        }
                        break;
                    case 0x02:
                        player.closePlayerMessenger();
                        break;
                    case 0x03:
                        if (messenger == null) {
                            c.announce(MaplePacketCreator.messengerChat(player.getName() + " : This Maple Messenger is currently unavailable. Please quit this chat."));
                        } else if (messenger.getMembers().size() < 3) {
                            input = slea.readMapleAsciiString();
                            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
                            if (target != null) {
                                if (target.getMessenger() == null) {
                                    if (MapleInviteCoordinator.createInvite(InviteType.MESSENGER, c.getPlayer(), messenger.getId(), target.getId())) {
                                        target.getClient().announce(MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));
                                        c.announce(MaplePacketCreator.messengerNote(input, 4, 1));
                                    } else {
                                        c.announce(MaplePacketCreator.messengerChat(player.getName() + " : " + input + " is already managing a Maple Messenger invitation"));
                                    }
                                } else {
                                    c.announce(MaplePacketCreator.messengerChat(player.getName() + " : " + input + " is already using Maple Messenger"));
                                }
                            } else {
                                if (world.find(input) > -1) {
                                    world.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel());
                                } else {
                                    c.announce(MaplePacketCreator.messengerNote(input, 4, 0));
                                }
                            }
                        } else {
                            c.announce(MaplePacketCreator.messengerChat(player.getName() + " : You cannot have more than 3 people in the Maple Messenger"));
                        }
                        break;
                    case 0x05:
                        String targeted = slea.readMapleAsciiString();
                        world.declineChat(targeted, player);
                        break;
                    case 0x06:
                        if (messenger != null) {
                            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, player.getMessengerPosition());
                            input = slea.readMapleAsciiString();
                            world.messengerChat(messenger, input, messengerplayer.getName());
                        }
                        break;
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
