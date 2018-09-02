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
package net;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import client.MapleClient;
import constants.ServerConstants;

import net.server.Server;
import net.server.coordinator.MapleSessionCoordinator;

import tools.FilePrinter;
import tools.MapleAESOFB;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import java.util.Arrays;

public class MapleServerHandler extends IoHandlerAdapter {
    private final static Set<Short> ignoredDebugRecvPackets = new HashSet<>(Arrays.asList((short) 167, (short) 197, (short) 89, (short) 91, (short) 41, (short) 188, (short) 107));
    
    private PacketProcessor processor;
    private int world = -1, channel = -1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private static AtomicLong sessionId = new AtomicLong(7777);
    
    public MapleServerHandler() {
        this.processor = PacketProcessor.getProcessor(-1, -1);
    }

    public MapleServerHandler(int world, int channel) {
        this.processor = PacketProcessor.getProcessor(world, channel);
        this.world = world;
        this.channel = channel;
    }
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        if (cause instanceof IOException || cause instanceof ClassCastException) {
            return;
        }
        MapleClient mc = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (mc != null && mc.getPlayer() != null) {
            FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, cause, "Exception caught by: " + mc.getPlayer());
        }
    }

    private boolean isLoginServerHandler() {
        return channel == -1 && world == -1;
    }
    
    @Override
    public void sessionOpened(IoSession session) {
        if (!Server.getInstance().isOnline()) {
            MapleSessionCoordinator.getInstance().closeSession(session, true);
            return;
        }
        
        if (!isLoginServerHandler()) {
            if (Server.getInstance().getChannel(world, channel) == null) {
                MapleSessionCoordinator.getInstance().closeSession(session, true);
                return;
            }
        } else {
            if (!MapleSessionCoordinator.getInstance().canStartLoginSession(session)) {
                return;
            }
            
            FilePrinter.print(FilePrinter.SESSION, "IoSession with " + session.getRemoteAddress() + " opened on " + sdf.format(Calendar.getInstance().getTime()), false);
        }

        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(ivRecv, ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setWorld(world);
        client.setChannel(channel);
        client.setSessionId(sessionId.getAndIncrement()); // Generates a reasonable session id.
        session.write(MaplePacketCreator.getHello(ServerConstants.VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if (isLoginServerHandler()) {
            MapleSessionCoordinator.getInstance().closeLoginSession(session);
        } else {
            MapleSessionCoordinator.getInstance().closeSession(session, null);
        }
        
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            try {
                boolean inCashShop = false;
                if (client.getPlayer() != null) {
                    inCashShop = client.getPlayer().getCashShop().isOpened();
                }
                client.disconnect(false, inCashShop);
            } catch (Throwable t) {
                FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, t);
            } finally {
                session.close(true);
                session.removeAttribute(MapleClient.CLIENT_KEY);      
                //client.empty();
            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        
        if(ServerConstants.USE_DEBUG_SHOW_RCVD_PACKET && !ignoredDebugRecvPackets.contains(packetId)) System.out.println("Received packet id " + packetId);
        final MaplePacketHandler packetHandler = processor.getHandler(packetId);
        if (packetHandler != null && packetHandler.validateState(client)) {
            try {
            	MapleLogger.logRecv(client, packetId, message);
                packetHandler.handlePacket(slea, client);
            } catch (final Throwable t) {
                FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (client.getPlayer() == null ? "" : "player ; " + client.getPlayer() + " on map ; " + client.getPlayer().getMapId() + " - ") + "account ; " + client.getAccountName() + "\r\n" + slea.toString());
            }
        }
    }
    
    @Override
    public void messageSent(IoSession session, Object message) {
    	byte[] content = (byte[]) message;
    	SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
    	slea.readShort();
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            client.getPing();
        }
        super.sessionIdle(session, status);
    }
}
