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
package net.server.coordinator.login;

import config.YamlConfig;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleCharacter;
import client.MapleClient;
import net.server.world.World;
import net.server.Server;
import tools.Pair;

/**
 *
 * @author Ronan
 */
public class MapleLoginBypassCoordinator {
    
    private final static MapleLoginBypassCoordinator instance = new MapleLoginBypassCoordinator();
    
    public static MapleLoginBypassCoordinator getInstance() {
        return instance;
    }
    
    private final ConcurrentHashMap<Pair<String, Integer>, Pair<Boolean, Long>> loginBypass = new ConcurrentHashMap<>();   // optimized PIN & PIC check
    
    public boolean canLoginBypass(String nibbleHwid, int accId, boolean pic) {
        try {
            Pair<String, Integer> entry = new Pair<>(nibbleHwid, accId);
            Boolean p = loginBypass.get(entry).getLeft();
            
            return !pic || p;
        } catch (NullPointerException npe) {
            return false;
        }
    }
    
    public void registerLoginBypassEntry(String nibbleHwid, int accId, boolean pic) {
        long expireTime = (pic ? YamlConfig.config.server.BYPASS_PIC_EXPIRATION : YamlConfig.config.server.BYPASS_PIN_EXPIRATION);
        if (expireTime > 0) {
            Pair<String, Integer> entry = new Pair<>(nibbleHwid, accId);
            expireTime = Server.getInstance().getCurrentTime() + expireTime * 60 * 1000;
            try {
                pic |= loginBypass.get(entry).getLeft();
                expireTime = Math.max(loginBypass.get(entry).getRight(), expireTime);
            } catch (NullPointerException npe) {}
            
            loginBypass.put(entry, new Pair<>(pic, expireTime));
        }
    }
    
    public void unregisterLoginBypassEntry(String nibbleHwid, int accId) {
        Pair<String, Integer> entry = new Pair<>(nibbleHwid, accId);
        loginBypass.remove(entry);
    }
    
    public void runUpdateLoginBypass() {
        if (!loginBypass.isEmpty()) {
            List<Pair<String, Integer>> toRemove = new LinkedList<>();
            Set<Integer> onlineAccounts = new HashSet<>();
            long timeNow = Server.getInstance().getCurrentTime();
            
            for (World w : Server.getInstance().getWorlds()) {
                for (MapleCharacter chr : w.getPlayerStorage().getAllCharacters()) {
                    MapleClient c = chr.getClient();
                    if (c != null) {
                        onlineAccounts.add(c.getAccID());
                    }
                }
            }
            
            for (Entry<Pair<String, Integer>, Pair<Boolean, Long>> e : loginBypass.entrySet()) {
                if (onlineAccounts.contains(e.getKey().getRight())) {
                    long expireTime = timeNow + 2 * 60 * 1000;
                    if (expireTime > e.getValue().getRight()) {
                        loginBypass.replace(e.getKey(), new Pair<>(e.getValue().getLeft(), expireTime));
                    }
                } else if (e.getValue().getRight() < timeNow) {
                    toRemove.add(e.getKey());
                }
            }
            
            if (!toRemove.isEmpty()) {
                for (Pair<String, Integer> p : toRemove) {
                    loginBypass.remove(p);
                }
            }
        }
    }
    
}
