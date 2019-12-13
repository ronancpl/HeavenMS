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
package net.server.coordinator.session;

import net.server.coordinator.login.LoginStorage;
import client.MapleCharacter;
import client.MapleClient;
import config.YamlConfig;

import net.server.Server;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import org.apache.mina.core.session.IoSession;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Ronan
 */
public class MapleSessionCoordinator {
    
    private final static MapleSessionCoordinator instance = new MapleSessionCoordinator();
    
    public static MapleSessionCoordinator getInstance() {
        return instance;
    }
    
    public enum AntiMulticlientResult {
        SUCCESS,
        REMOTE_LOGGEDIN,
        REMOTE_REACHED_LIMIT,
        REMOTE_PROCESSING,
        REMOTE_NO_MATCH,
        MANY_ACCOUNT_ATTEMPTS,
        COORDINATOR_ERROR
    }
    
    private final LoginStorage loginStorage = new LoginStorage();
    private final Map<Integer, MapleClient> onlineClients = new HashMap<>();
    private final Set<String> onlineRemoteHwids = new HashSet<>();
    private final Map<String, Set<IoSession>> loginRemoteHosts = new HashMap<>();
    private final Set<String> pooledRemoteHosts = new HashSet<>();
    
    private final ConcurrentHashMap<String, String> cachedHostHwids = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cachedHostTimeout = new ConcurrentHashMap<>();
    private final List<ReentrantLock> poolLock = new ArrayList<>(100);
    
    private MapleSessionCoordinator() {
        for(int i = 0; i < 100; i++) {
            poolLock.add(MonitoredReentrantLockFactory.createLock(MonitoredLockType.SERVER_LOGIN_COORD));
        }
    }
    
    private static long hwidExpirationUpdate(int relevance) {
        int degree = 1, i = relevance, subdegree;
        while ((subdegree = 5 * degree) <= i) {
            i -= subdegree;
            degree++;
        }
        
        degree--;
        int baseTime, subdegreeTime;
        if (degree > 2) {
            subdegreeTime = 10;
        } else {
            subdegreeTime = 1 + (3 * degree);
        }
        
        switch(degree) {
            case 0:
                baseTime = 2;       // 2 hours
                break;
                
            case 1:
                baseTime = 24;      // 1 day
                break;
                
            case 2:
                baseTime = 168;     // 7 days
                break;
                
            default:
                baseTime = 1680;    // 70 days
        }
        
        return 3600000 * (baseTime + subdegreeTime);
    }
    
    private static void updateAccessAccount(Connection con, String remoteHwid, int accountId, int loginRelevance) throws SQLException {
        java.sql.Timestamp nextTimestamp = new java.sql.Timestamp(Server.getInstance().getCurrentTime() + hwidExpirationUpdate(loginRelevance));
        if(loginRelevance < Byte.MAX_VALUE) {
            loginRelevance++;
        }
        
        try (PreparedStatement ps = con.prepareStatement("UPDATE hwidaccounts SET relevance = ?, expiresat = ? WHERE accountid = ? AND hwid LIKE ?")) {
            ps.setInt(1, loginRelevance);
            ps.setTimestamp(2, nextTimestamp);
            ps.setInt(3, accountId);
            ps.setString(4, remoteHwid);
            
            ps.executeUpdate();
        }
    }
    
    private static void registerAccessAccount(Connection con, String remoteHwid, int accountId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO hwidaccounts (accountid, hwid, expiresat) VALUES (?, ?, ?)")) {
            ps.setInt(1, accountId);
            ps.setString(2, remoteHwid);
            ps.setTimestamp(3, new java.sql.Timestamp(Server.getInstance().getCurrentTime() + hwidExpirationUpdate(0)));
            
            ps.executeUpdate();
        }
    }
    
    private static boolean associateHwidAccountIfAbsent(String remoteHwid, int accountId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            int hwidCount = 0;
            
            try (PreparedStatement ps = con.prepareStatement("SELECT SQL_CACHE hwid FROM hwidaccounts WHERE accountid = ?")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String rsHwid = rs.getString("hwid");
                        if (rsHwid.contentEquals(remoteHwid)) {
                            return false;
                        }
                        
                        hwidCount++;
                    }
                }

                if (hwidCount < YamlConfig.config.server.MAX_ALLOWED_ACCOUNT_HWID) {
                    registerAccessAccount(con, remoteHwid, accountId);
                    return true;
                }
            } finally {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private static boolean attemptAccessAccount(String nibbleHwid, int accountId, boolean routineCheck) {
        try {
            Connection con = DatabaseConnection.getConnection();
            int hwidCount = 0;

            try (PreparedStatement ps = con.prepareStatement("SELECT SQL_CACHE * FROM hwidaccounts WHERE accountid = ?")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String rsHwid = rs.getString("hwid");
                        if (rsHwid.endsWith(nibbleHwid)) {
                            if (!routineCheck) {
                                // better update HWID relevance as soon as the login is authenticated

                                int loginRelevance = rs.getInt("relevance");
                                updateAccessAccount(con, rsHwid, accountId, loginRelevance);
                            }

                            return true;
                        }

                        hwidCount++;
                    }
                }

                if (hwidCount < YamlConfig.config.server.MAX_ALLOWED_ACCOUNT_HWID) {
                    return true;
                }
            } finally {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private Lock getCoodinatorLock(String remoteHost) {
        return poolLock.get(Math.abs(remoteHost.hashCode()) % 100);
    }

    public static String getSessionRemoteAddress(IoSession session) {
        return (String) session.getAttribute(MapleClient.CLIENT_REMOTE_ADDRESS);
    }
    
    public static String getSessionRemoteHost(IoSession session) {
        String nibbleHwid = (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);
        
        if (nibbleHwid != null) {
            return getSessionRemoteAddress(session) + "-" + nibbleHwid;
        } else {
            return getSessionRemoteAddress(session);
        }
    }

    private static MapleClient getSessionClient(IoSession session) {
        return (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
    }

    public void updateOnlineSession(IoSession session) {
        MapleClient client = getSessionClient(session);

        if (client != null) {
            int accountId = client.getAccID();
            MapleClient ingameClient = onlineClients.get(accountId);
            if (ingameClient != null) {     // thanks MedicOP for finding out a loss of loggedin account uniqueness when using the CMS "Unstuck" feature
                ingameClient.forceDisconnect();
            }

            onlineClients.put(accountId, client);
        }
    }

    public boolean canStartLoginSession(IoSession session) {
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) return true;

        String remoteHost = getSessionRemoteHost(session);
        Lock lock = getCoodinatorLock(remoteHost);

        try {
            int tries = 0;
            while (true) {
                if (lock.tryLock()) {
                    try {
                        if (pooledRemoteHosts.contains(remoteHost)) {
                            return false;
                        }

                        pooledRemoteHosts.add(remoteHost);
                    } finally {
                        lock.unlock();
                    }

                    break;
                } else {
                    if(tries == 2) {
                        return true;
                    }
                    tries++;

                    Thread.sleep(1777);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        try {
            String knownHwid = cachedHostHwids.get(remoteHost);
            if (knownHwid != null) {
                if (onlineRemoteHwids.contains(knownHwid)) {
                    return false;
                }
            }

            if (loginRemoteHosts.containsKey(remoteHost)) {
                return false;
            }

            Set<IoSession> lrh = new HashSet<>(2);
            lrh.add(session);
            loginRemoteHosts.put(remoteHost, lrh);

            return true;
        } finally {
            lock.lock();
            try {
                pooledRemoteHosts.remove(remoteHost);
            } finally {
                lock.unlock();
            }
        }
    }

    public void closeLoginSession(IoSession session) {
        String nibbleHwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID);
        String remoteHost = getSessionRemoteHost(session);
        
        Set<IoSession> lrh = loginRemoteHosts.get(remoteHost);
        if (lrh != null) {
            lrh.remove(session);
            if (lrh.isEmpty()) {
                loginRemoteHosts.remove(remoteHost);
            }
        }
        
        if (nibbleHwid != null) {
            onlineRemoteHwids.remove(nibbleHwid);

            MapleClient client = getSessionClient(session);
            if (client != null) {
                MapleClient loggedClient = onlineClients.get(client.getAccID());

                // do not remove an online game session here, only login session
                if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                    onlineClients.remove(client.getAccID());
                }
            }
        }
    }

    public AntiMulticlientResult attemptLoginSession(IoSession session, String nibbleHwid, int accountId, boolean routineCheck) {
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) {
            session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, nibbleHwid);
            return AntiMulticlientResult.SUCCESS;
        }

        String remoteHost = getSessionRemoteHost(session);
        Lock lock = getCoodinatorLock(remoteHost);

        try {
            int tries = 0;
            while (true) {
                if (lock.tryLock()) {
                    try {
                        if (pooledRemoteHosts.contains(remoteHost)) {
                            return AntiMulticlientResult.REMOTE_PROCESSING;
                        }

                        pooledRemoteHosts.add(remoteHost);
                    } finally {
                        lock.unlock();
                    }

                    break;
                } else {
                    if(tries == 2) {
                        return AntiMulticlientResult.COORDINATOR_ERROR;
                    }
                    tries++;

                    Thread.sleep(1777);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AntiMulticlientResult.COORDINATOR_ERROR;
        }

        try {
            if (!loginStorage.registerLogin(accountId)) {
                return AntiMulticlientResult.MANY_ACCOUNT_ATTEMPTS;
            }

            if (!routineCheck) {
                if (onlineRemoteHwids.contains(nibbleHwid)) {
                    return AntiMulticlientResult.REMOTE_LOGGEDIN;
                }

                if (!attemptAccessAccount(nibbleHwid, accountId, routineCheck)) {
                    return AntiMulticlientResult.REMOTE_REACHED_LIMIT;
                }

                session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, nibbleHwid);
                onlineRemoteHwids.add(nibbleHwid);
            } else {
                if (!attemptAccessAccount(nibbleHwid, accountId, routineCheck)) {
                    return AntiMulticlientResult.REMOTE_REACHED_LIMIT;
                }
            }

            return AntiMulticlientResult.SUCCESS;
        } finally {
            lock.lock();
            try {
                pooledRemoteHosts.remove(remoteHost);
            } finally {
                lock.unlock();
            }
        }
    }

    public AntiMulticlientResult attemptGameSession(IoSession session, int accountId, String remoteHwid) {
        String remoteHost = getSessionRemoteHost(session);
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) {
            associateRemoteHostHwid(remoteHost, remoteHwid);
            associateRemoteHostHwid(getSessionRemoteAddress(session), remoteHwid);  // no HWID information on the loggedin newcomer session...
            return AntiMulticlientResult.SUCCESS;
        }
        
        Lock lock = getCoodinatorLock(remoteHost);
        try {
            int tries = 0;
            while (true) {
                if (lock.tryLock()) {
                    try {
                        if (pooledRemoteHosts.contains(remoteHost)) {
                            return AntiMulticlientResult.REMOTE_PROCESSING;
                        }
                        
                        pooledRemoteHosts.add(remoteHost);
                    } finally {
                        lock.unlock();
                    }
                    
                    break;
                } else {
                    if(tries == 2) {
                        return AntiMulticlientResult.COORDINATOR_ERROR;
                    }
                    tries++;
                    
                    Thread.sleep(1777);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AntiMulticlientResult.COORDINATOR_ERROR;
        }
        
        try {
            String nibbleHwid = (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);   // thanks Paxum for noticing account stuck after PIC failure
            if (nibbleHwid != null) {
                onlineRemoteHwids.remove(nibbleHwid);
                
                if (remoteHwid.endsWith(nibbleHwid)) {
                    if (!onlineRemoteHwids.contains(remoteHwid)) {
                        // assumption: after a SUCCESSFUL login attempt, the incoming client WILL receive a new IoSession from the game server
                        
                        // updated session CLIENT_HWID attribute will be set when the player log in the game
                        onlineRemoteHwids.add(remoteHwid);
                        associateRemoteHostHwid(remoteHost, remoteHwid);
                        associateRemoteHostHwid(getSessionRemoteAddress(session), remoteHwid);
                        associateHwidAccountIfAbsent(remoteHwid, accountId);

                        return AntiMulticlientResult.SUCCESS;
                    } else {
                        return AntiMulticlientResult.REMOTE_LOGGEDIN;
                    }
                } else {
                    return AntiMulticlientResult.REMOTE_NO_MATCH;
                }
            } else {
                return AntiMulticlientResult.REMOTE_NO_MATCH;
            }
        } finally {
            lock.lock();
            try {
                pooledRemoteHosts.remove(remoteHost);
            } finally {
                lock.unlock();
            }
        }
    }
    
    private static MapleClient fetchInTransitionSessionClient(IoSession session) {
        String remoteHwid = MapleSessionCoordinator.getInstance().getGameSessionHwid(session);
        
        if (remoteHwid != null) {   // maybe this session was currently in-transition?
            int hwidLen = remoteHwid.length();
            if (hwidLen <= 8) {
                session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid);
            } else {
                session.setAttribute(MapleClient.CLIENT_HWID, remoteHwid);
                session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid.substring(hwidLen - 8, hwidLen));
            }
            
            MapleClient client = new MapleClient(null, null, session);
            Integer cid = Server.getInstance().freeCharacteridInTransition(client);
            if (cid != null) {
                try {
                    client.setAccID(MapleCharacter.loadCharFromDB(cid, client, false).getAccountID());
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
            
            session.setAttribute(MapleClient.CLIENT_KEY, client);
            return client;
        }
        
        return null;
    }
    
    public void closeSession(IoSession session, Boolean immediately) {
        MapleClient client = getSessionClient(session);
        if (client == null) {
            client = fetchInTransitionSessionClient(session);
        }
        
        String hwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID); // making sure to clean up calls to this function on login phase
        onlineRemoteHwids.remove(hwid);
        
        hwid = (String) session.removeAttribute(MapleClient.CLIENT_HWID);
        onlineRemoteHwids.remove(hwid);
        
        if (client != null) {
            if (hwid != null) { // is a game session
                onlineClients.remove(client.getAccID());
            } else {
                MapleClient loggedClient = onlineClients.get(client.getAccID());
                
                // do not remove an online game session here, only login session
                if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                    onlineClients.remove(client.getAccID());
                }
            }
        }
        
        if (immediately != null) {
            session.close(immediately);
        }
        
        // session.removeAttribute(MapleClient.CLIENT_REMOTE_ADDRESS); No real need for removing String property on closed sessions
    }
    
    public String pickLoginSessionHwid(IoSession session) {
        String remoteHost = getSessionRemoteAddress(session);
        return cachedHostHwids.remove(remoteHost);    // thanks BHB, resinate for noticing players from same network not being able to login
    }
    
    public String getGameSessionHwid(IoSession session) {
        String remoteHost = getSessionRemoteHost(session);
        return cachedHostHwids.get(remoteHost);
    }
    
    private void associateRemoteHostHwid(String remoteHost, String remoteHwid) {
        cachedHostHwids.put(remoteHost, remoteHwid);
        cachedHostTimeout.put(remoteHost, Server.getInstance().getCurrentTime() + 604800000);   // 1 week-time entry
    }
    
    public void runUpdateHwidHistory() {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM hwidaccounts WHERE expiresat < CURRENT_TIMESTAMP")) {
                ps.execute();
            } finally {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        long timeNow = Server.getInstance().getCurrentTime();
        List<String> toRemove = new LinkedList<>();
        for (Entry<String, Long> cht : cachedHostTimeout.entrySet()) {
            if (cht.getValue() < timeNow) {
                toRemove.add(cht.getKey());
            }
        }
        
        if (!toRemove.isEmpty()) {
            for (String s : toRemove) {
                cachedHostHwids.remove(s);
                cachedHostTimeout.remove(s);
            }
        }
    }
    
    public void runUpdateLoginHistory() {
        loginStorage.updateLoginHistory();
    }
    
    public void printSessionTrace() {
        if (!onlineClients.isEmpty()) {
            List<Entry<Integer, MapleClient>> elist = new ArrayList<>(onlineClients.entrySet());
            Collections.sort(elist, new Comparator<Entry<Integer, MapleClient>>() {
                @Override
                public int compare(Entry<Integer, MapleClient> e1, Entry<Integer, MapleClient> e2) {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
            
            System.out.println("Current online clients: ");
            for (Entry<Integer, MapleClient> e : elist) {
                System.out.println("  " + e.getKey());
            }
        }
        
        if (!onlineRemoteHwids.isEmpty()) {
            List<String> slist = new ArrayList<>(onlineRemoteHwids);
            Collections.sort(slist);
            
            System.out.println("Current online HWIDs: ");
            for (String s : slist) {
                System.out.println("  " + s);
            }
        }
        
        if (!loginRemoteHosts.isEmpty()) {
            List<Entry<String, Set<IoSession>>> elist = new ArrayList<>(loginRemoteHosts.entrySet());
            
            Collections.sort(elist, new Comparator<Entry<String, Set<IoSession>>>() {
                @Override
                public int compare(Entry<String, Set<IoSession>> e1, Entry<String, Set<IoSession>> e2) {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
            
            System.out.println("Current login sessions: ");
            for (Entry<String, Set<IoSession>> e : elist) {
                System.out.println("  " + e.getKey() + ", size: " + e.getValue().size());
            }
        }
    }
    
    public void printSessionTrace(MapleClient c) {
        String str = "Opened server sessions:\r\n\r\n";
        
        if (!onlineClients.isEmpty()) {
            List<Entry<Integer, MapleClient>> elist = new ArrayList<>(onlineClients.entrySet());
            Collections.sort(elist, new Comparator<Entry<Integer, MapleClient>>() {
                @Override
                public int compare(Entry<Integer, MapleClient> e1, Entry<Integer, MapleClient> e2) {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
            
            str += ("Current online clients:\r\n");
            for (Entry<Integer, MapleClient> e : elist) {
                str += ("  " + e.getKey() + "\r\n");
            }
        }
        
        if (!onlineRemoteHwids.isEmpty()) {
            List<String> slist = new ArrayList<>(onlineRemoteHwids);
            Collections.sort(slist);
            
            str += ("Current online HWIDs:\r\n");
            for (String s : slist) {
                str += ("  " + s + "\r\n");
            }
        }
        
        if (!loginRemoteHosts.isEmpty()) {
            List<Entry<String, Set<IoSession>>> elist = new ArrayList<>(loginRemoteHosts.entrySet());
            
            Collections.sort(elist, new Comparator<Entry<String, Set<IoSession>>>() {
                @Override
                public int compare(Entry<String, Set<IoSession>> e1, Entry<String, Set<IoSession>> e2) {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
            
            str += ("Current login sessions:\r\n");
            for (Entry<String, Set<IoSession>> e : elist) {
                str += ("  " + e.getKey() + ", IP: " + e.getValue() + "\r\n");
            }
        }
        
        c.getAbstractPlayerInteraction().npcTalk(2140000, str);
    }
}
