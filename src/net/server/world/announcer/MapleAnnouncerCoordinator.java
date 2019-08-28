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
import net.server.world.announcer.MapleAnnouncerEntryPool.SessionPacket;

/**
 *
 * @author Ronan
 */
public class MapleAnnouncerCoordinator {
    
    private static final MapleAnnouncerCoordinator instance = new MapleAnnouncerCoordinator();
    
    public static MapleAnnouncerCoordinator getInstance() {   // world-agnostic Announcer coordinator
        return instance;
    }
    
    private MapleAnnouncerEntryPool pool = new MapleAnnouncerEntryPool();
    private ConcurrentLinkedQueue<SessionPacket> queue = new ConcurrentLinkedQueue<>();
    private Thread t;
    
    public void append(IoSession io, byte[] packet) {
        queue.offer(pool.getSessionPacket(io, packet));
    }
    
    public void init() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        SessionPacket p = queue.poll();
                        if (p != null) {
                            IoSession session = p.getSession();
                            byte[] packet = p.getPacket();

                            session.write(packet);
                            pool.returnSessionPacket(p);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        t = new Thread(r);
        t.start();
    }
    
    public void shutdown() {
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
        queue.clear();
        pool.shutdown();
    }
    
}
