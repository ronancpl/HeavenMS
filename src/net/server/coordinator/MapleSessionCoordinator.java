/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package net.server.coordinator;

import client.MapleClient;
import constants.ServerConstants;

import net.server.Server;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import org.apache.mina.core.session.IoSession;
import tools.DatabaseConnection;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                
                if (hwidCount < ServerConstants.MAX_ALLOWED_ACCOUNT_HWID) {
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
                
                if (hwidCount < ServerConstants.MAX_ALLOWED_ACCOUNT_HWID) {
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
        return poolLock.get(remoteHost.hashCode() % 100);
    }
    
    private static String getRemoteIp(IoSession session) {
        return ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
    }
    
    public boolean canStartLoginSession(IoSession session) {
        if (!ServerConstants.DETERRED_MULTICLIENT) return true;
        
        String remoteHost = getRemoteIp(session);
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
        String remoteIp = getRemoteIp(session);
        Set<IoSession> lrh = loginRemoteHosts.get(remoteIp);
        if (lrh != null) {
            lrh.remove(session);
            if (lrh.isEmpty()) {
                loginRemoteHosts.remove(remoteIp);
                
                String nibbleHwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID);
                if (nibbleHwid != null) {
                    onlineRemoteHwids.remove(nibbleHwid);
                }
            }
        }
    }
    
    public AntiMulticlientResult attemptLoginSession(IoSession session, String nibbleHwid, int accountId, boolean routineCheck) {
        if (!ServerConstants.DETERRED_MULTICLIENT) return AntiMulticlientResult.SUCCESS;
        
        String remoteHost = getRemoteIp(session);
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
        if (!ServerConstants.DETERRED_MULTICLIENT) return AntiMulticlientResult.SUCCESS;
        
        String remoteHost = getRemoteIp(session);
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
            String nibbleHwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID);
            if (nibbleHwid != null) {
                onlineRemoteHwids.remove(nibbleHwid);
                
                if (remoteHwid.endsWith(nibbleHwid)) {
                    if (!onlineRemoteHwids.contains(remoteHwid)) {
                        // assumption: after a SUCCESSFUL login attempt, the incoming client WILL receive a new IoSession from the game server
                        
                        // updated session CLIENT_HWID attribute will be set when the player log in the game
                        onlineRemoteHwids.add(remoteHwid);

                        cachedHostHwids.put(remoteHost, remoteHwid);
                        cachedHostTimeout.put(remoteHost, Server.getInstance().getCurrentTime() + 604800000);   // 1 week-time entry
                        
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
    
    public void closeSession(IoSession session, Boolean immediately) {
        String hwid = (String) session.removeAttribute(MapleClient.CLIENT_HWID);
        onlineRemoteHwids.remove(hwid);
        
        hwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID); // making sure to clean up calls to this function on login phase
        onlineRemoteHwids.remove(hwid);
        
        if (immediately != null) {
            session.close(immediately);
        }
    }
    
    public String getGameSessionHwid(IoSession session) {
        String remoteHost = getRemoteIp(session);
        return cachedHostHwids.get(remoteHost);
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
}
