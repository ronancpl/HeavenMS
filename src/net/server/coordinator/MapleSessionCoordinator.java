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
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        MANY_ACCOUNT_ATTEMPTS,
        COORDINATOR_ERROR
    }
    
    private final LoginStorage loginStorage = new LoginStorage();
    private final Set<String> onlineRemoteHosts = new HashSet<>();
    private final Map<String, Set<IoSession>> loginRemoteHosts = new HashMap<>();
    private final Set<String> pooledRemoteHosts = new HashSet<>();
    private final List<ReentrantLock> poolLock = new ArrayList<>(100);
    
    private MapleSessionCoordinator() {
        for(int i = 0; i < 100; i++) {
            poolLock.add(MonitoredReentrantLockFactory.createLock(MonitoredLockType.SERVER_LOGIN_COORD));
        }
    }
    
    private static long ipExpirationUpdate(int relevance) {
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
    
    private static void updateAccessAccount(Connection con, String remoteHost, int accountId, int loginRelevance) throws SQLException {
        java.sql.Timestamp nextTimestamp = new java.sql.Timestamp(Server.getInstance().getCurrentTime() + ipExpirationUpdate(loginRelevance));
        if(loginRelevance < Byte.MAX_VALUE) {
            loginRelevance++;
        }
        
        try (PreparedStatement ps = con.prepareStatement("UPDATE ipaccounts SET relevance = ?, expiresat = ? WHERE accountid = ? AND ip LIKE ?")) {
            ps.setInt(1, loginRelevance);
            ps.setTimestamp(2, nextTimestamp);
            ps.setInt(3, accountId);
            ps.setString(4, remoteHost);
            
            ps.executeUpdate();
        }
    }
    
    private static void registerAccessAccount(Connection con, String remoteHost, int accountId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO ipaccounts (accountid, ip, expiresat) VALUES (?, ?, ?)")) {
            ps.setInt(1, accountId);
            ps.setString(2, remoteHost);
            ps.setTimestamp(3, new java.sql.Timestamp(Server.getInstance().getCurrentTime() + ipExpirationUpdate(0)));
            
            ps.executeUpdate();
        }
    }
    
    private static boolean attemptAccessAccount(String remoteHost, int accountId, boolean routineCheck) {
        try {
            Connection con = DatabaseConnection.getConnection();
            int ipCount = 0;
            
            try (PreparedStatement ps = con.prepareStatement("SELECT SQL_CACHE * FROM ipaccounts WHERE accountid = ?")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (remoteHost.contentEquals(rs.getString("ip"))) {
                            if (!routineCheck) {
                                int loginRelevance = rs.getInt("relevance");
                                updateAccessAccount(con, remoteHost, accountId, loginRelevance);
                            }
                            
                            return true;
                        }
                        
                        ipCount++;
                    }
                }
                
                if (ipCount < ServerConstants.MAX_ALLOWED_ACCOUNT_IP) {
                    if (!routineCheck) {
                        registerAccessAccount(con, remoteHost, accountId);
                    }
                    
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
            if (onlineRemoteHosts.contains(remoteHost) || loginRemoteHosts.containsKey(remoteHost)) {
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
            }
        }
    }
    
    public AntiMulticlientResult attemptSessionLogin(IoSession session, int accountId, boolean routineCheck) {
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
                if (onlineRemoteHosts.contains(remoteHost)) {
                    return AntiMulticlientResult.REMOTE_LOGGEDIN;
                }

                if (!attemptAccessAccount(remoteHost, accountId, routineCheck)) {
                    return AntiMulticlientResult.REMOTE_REACHED_LIMIT;
                }
                
                onlineRemoteHosts.add(remoteHost);
            } else {
                if (!attemptAccessAccount(remoteHost, accountId, routineCheck)) {
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
    
    public void closeSession(IoSession session, Boolean immediately) {
        onlineRemoteHosts.remove(getRemoteIp(session));
        if (immediately != null) session.close(immediately);
    }
    
    public void runUpdateIpHistory() {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ipaccounts WHERE expiresat < CURRENT_TIMESTAMP")) {
                ps.execute();
            } finally {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void runUpdateLoginHistory() {
        loginStorage.updateLoginHistory();
    }
}
