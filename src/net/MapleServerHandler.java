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
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

import net.server.Server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import tools.FilePrinter;
import tools.MapleAESOFB;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import constants.ServerConstants;

import java.util.concurrent.locks.Lock;
import tools.locks.MonitoredReentrantLock;
import java.util.concurrent.ScheduledFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.TimerManager;
import tools.locks.MonitoredLockType;

public class MapleServerHandler extends IoHandlerAdapter {

    private PacketProcessor processor;
    private int world = -1, channel = -1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private static AtomicLong sessionId = new AtomicLong(7777);
    
    private Lock idleLock = new MonitoredReentrantLock(MonitoredLockType.SRVHANDLER_IDLE, true);
    private Lock tempLock = new MonitoredReentrantLock(MonitoredLockType.SRVHANDLER_TEMP, true);
    private Map<MapleClient, Long> idleSessions = new HashMap<>(100);
    private Map<MapleClient, Long> tempIdleSessions = new HashMap<>();
    private ScheduledFuture<?> idleManager = null;
    
    public MapleServerHandler() {
        this.processor = PacketProcessor.getProcessor(-1, -1);
        
        idleManagerTask();
    }

    public MapleServerHandler(int world, int channel) {
        this.processor = PacketProcessor.getProcessor(world, channel);
        this.world = world;
        this.channel = channel;
        
        idleManagerTask();
    }
    
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    	System.out.println("disconnect by exception");
        cause.printStackTrace();
        
        if (cause instanceof IOException || cause instanceof ClassCastException) {
            return;
        }
        MapleClient mc = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (mc != null && mc.getPlayer() != null) {
            FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, cause, "Exception caught by: " + mc.getPlayer());
        }
    }

    @Override
    public void sessionOpened(IoSession session) {
        if (!Server.getInstance().isOnline()) {
            session.close(true);
            return;
        }
        if (channel > -1 && world > -1) {
            if (Server.getInstance().getChannel(world, channel) == null) {
                session.close(true);
                return;
            }
        } else {
            FilePrinter.print(FilePrinter.SESSION, "IoSession with " + session.getRemoteAddress() + " opened on " + sdf.format(Calendar.getInstance().getTime()), false);
        }

        byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setWorld(world);
        client.setChannel(channel);
        client.setSessionId(sessionId.getAndIncrement()); // Generates a reasonable session id.
        session.write(MaplePacketCreator.getHello(ServerConstants.VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
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
                session.close();
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
        
        if(ServerConstants.USE_DEBUG_SHOW_RCVD_PACKET) System.out.println("Received packet id " + packetId);
        final MaplePacketHandler packetHandler = processor.getHandler(packetId);
        if (packetHandler != null && packetHandler.validateState(client)) {
            try {
            	MapleLogger.logRecv(client, packetId, message);
                packetHandler.handlePacket(slea, client);
            } catch (final Throwable t) {
                FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (client.getPlayer() == null ? "" : "player ; " + client.getPlayer() + " on map ; " + client.getPlayer().getMapId() + " - ") + "account ; " + client.getAccountName() + "\r\n" + slea.toString());
                //client.announce(MaplePacketCreator.enableActions());//bugs sometimes
            }
        }
    }
    
    @Override
    public void messageSent(IoSession session, Object message) {
    	byte[] content = (byte[]) message;
    	SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
    	slea.readShort(); //packetId
    }
    
    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            registerIdleSession(client);
        }
        super.sessionIdle(session, status);
    }
    
    private void registerIdleSession(MapleClient c) {
        if(idleLock.tryLock()) {
            idleSessions.put(c, System.currentTimeMillis());
            c.announce(MaplePacketCreator.getPing());
            
            idleLock.unlock();
        } else {
            tempLock.lock();
            try {
                tempIdleSessions.put(c, System.currentTimeMillis());
                c.announce(MaplePacketCreator.getPing());
            } finally {
                tempLock.unlock();
            }
        }
    }
    
    private void manageIdleSessions() {
        long timeNow = System.currentTimeMillis();
        long timeThen = timeNow - 15000;
        
        idleLock.lock();
        try {
            for(Entry<MapleClient, Long> mc : idleSessions.entrySet()) {
                if(timeNow - mc.getValue() >= 15000) {
                    mc.getKey().testPing(timeThen);
                }
            }
            
            idleSessions.clear();
            
            if(!tempIdleSessions.isEmpty()) {
                tempLock.lock();
                try {
                    for(Entry<MapleClient, Long> mc : tempIdleSessions.entrySet()) {
                        idleSessions.put(mc.getKey(), mc.getValue());
                    }
                    
                    tempIdleSessions.clear();
                } finally {
                    tempLock.unlock();
                }
            }
        } finally {
            idleLock.unlock();
        }
    }
    
    private void idleManagerTask() {
        this.idleManager = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                manageIdleSessions();
            }
        }, 10000);
    }
    
    private void cancelIdleManagerTask() {
        this.idleManager.cancel(false);
        this.idleManager = null;
    }
}
