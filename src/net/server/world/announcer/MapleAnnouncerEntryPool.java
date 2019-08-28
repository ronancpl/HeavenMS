/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package net.server.world.announcer;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Ronan
 */
public class MapleAnnouncerEntryPool {
    
    private ConcurrentLinkedQueue<SessionPacket> instancedPairs = new ConcurrentLinkedQueue<>();
    private final static int initialCount = 20000;     // initial length of the instanced pool
    
    public MapleAnnouncerEntryPool() {
        for (int i = 0; i < initialCount; i++) {
            instancedPairs.offer(new SessionPacket());
        }
    }
    
    public class SessionPacket {
        
        private IoSession session;
        private byte[] packet;
        
        public IoSession getSession() {
            return session;
        }
        
        public byte[] getPacket() {
            return packet;
        }
        
    }
    
    public SessionPacket getSessionPacket(IoSession session, byte[] packet) {
        SessionPacket sp = instancedPairs.poll();
        if (sp == null) {
            sp = new SessionPacket();
        }
        
        sp.session = session;
        sp.packet = packet;
        return sp;
    }
    
    public void returnSessionPacket(SessionPacket sp) {
        instancedPairs.offer(sp);
    }
    
    public void shutdown() {
        instancedPairs.clear();
    }
    
}
