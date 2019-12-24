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
package net.server.coordinator.matchchecker;

import client.MapleCharacter;
import net.server.PlayerStorage;
import net.server.Server;
import net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import net.server.world.World;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Ronan
 */
public class MapleMatchCheckerCoordinator {
    
    private final Map<Integer, MapleMatchCheckingElement> matchEntries = new HashMap<>();
    
    private final Set<Integer> pooledCids = new HashSet<>();
    private final Semaphore semaphorePool = new Semaphore(7);
    
    private class MapleMatchCheckingEntry {
        private boolean accepted;
        private int cid;
        
        private MapleMatchCheckingEntry(int cid) {
            this.cid = cid;
            this.accepted = false;
        }
        
        private boolean setAccept() {
            if (!this.accepted) {
                this.accepted = true;
                return true;
            } else {
                return false;
            }
        }
        
        private boolean getAccept() {
            return this.accepted;
        }
    }
    
    private class MapleMatchCheckingElement {
        private int leaderCid;
        private int world;
        
        private MatchCheckerType matchType;
        private AbstractMatchCheckerListener listener;
        
        private Map<Integer, MapleMatchCheckingEntry> confirmingMembers = new HashMap<>();
        private int confirmCount;
        private boolean active = true;
        
        private String message;
        
        private MapleMatchCheckingElement(MatchCheckerType matchType, int leaderCid, int world, AbstractMatchCheckerListener leaderListener, Set<Integer> matchPlayers, String message) {
            this.leaderCid = leaderCid;
            this.world = world;
            this.listener = leaderListener;
            this.confirmCount = 0;
            this.message = message;
            this.matchType = matchType;
            
            for (Integer cid : matchPlayers) {
                MapleMatchCheckingEntry mmcEntry = new MapleMatchCheckingEntry(cid);
                confirmingMembers.put(cid, mmcEntry);
            }
        }
        
        private boolean acceptEntry(int cid) {
            MapleMatchCheckingEntry mmcEntry = confirmingMembers.get(cid);
            if (mmcEntry != null) {
                if (mmcEntry.setAccept()) {
                    this.confirmCount++;
                    
                    if (this.confirmCount == this.confirmingMembers.size()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        private boolean isMatchActive() {
            return active;
        }
        
        private void setMatchActive(boolean a) {
            active = a;
        }
        
        private Set<Integer> getMatchPlayers() {
            return confirmingMembers.keySet();
        }
        
        private Set<Integer> getAcceptedMatchPlayers() {
            Set<Integer> s = new HashSet<>();
            
            for (Entry<Integer, MapleMatchCheckingEntry> e : confirmingMembers.entrySet()) {
                if (e.getValue().getAccept()) {
                    s.add(e.getKey());
                }
            }
            
            return s;
        }
        
        private Set<MapleCharacter> getMatchCharacters() {
            Set<MapleCharacter> players = new HashSet<>();
            
            World wserv = Server.getInstance().getWorld(world);
            if (wserv != null) {
                PlayerStorage ps = wserv.getPlayerStorage();
                
                for (Integer cid : getMatchPlayers()) {
                    MapleCharacter chr = ps.getCharacterById(cid);
                    if (chr != null) {
                        players.add(chr);
                    }
                }
            }
            
            return players;
        }
        
        private void dispatchMatchCreated() {
            Set<MapleCharacter> nonLeaderMatchPlayers = getMatchCharacters();
            MapleCharacter leader = null;
            
            for (MapleCharacter chr : nonLeaderMatchPlayers) {
                if (chr.getId() == leaderCid) {
                    leader = chr;
                    break;
                }
            }
            
            nonLeaderMatchPlayers.remove(leader);
            listener.onMatchCreated(leader, nonLeaderMatchPlayers, message);
        }
        
        private void dispatchMatchResult(boolean accept) {
            if (accept) {
                listener.onMatchAccepted(leaderCid, getMatchCharacters(), message);
            } else {
                listener.onMatchDeclined(leaderCid, getMatchCharacters(), message);
            }
        }
        
        private void dispatchMatchDismissed() {
            listener.onMatchDismissed(leaderCid, getMatchCharacters(), message);
        }
    }
    
    private void unpoolMatchPlayer(Integer cid) {
        unpoolMatchPlayers(Collections.singleton(cid));
    }
    
    private void unpoolMatchPlayers(Set<Integer> matchPlayers) {
        for (Integer cid : matchPlayers) {
            pooledCids.remove(cid);
        }
    }
    
    private boolean poolMatchPlayer(Integer cid) {
        return poolMatchPlayers(Collections.singleton(cid));
    }
    
    private boolean poolMatchPlayers(Set<Integer> matchPlayers) {
        Set<Integer> pooledPlayers = new HashSet<>();
        
        for (Integer cid : matchPlayers) {
            if (!pooledCids.add(cid)) {
                unpoolMatchPlayers(pooledPlayers);
                return false;
            } else {
                pooledPlayers.add(cid);
            }
        }
        
        return true;
    }
    
    private boolean isMatchingAvailable(Set<Integer> matchPlayers) {
        for (Integer cid : matchPlayers) {
            if (matchEntries.containsKey(cid)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void reenablePlayerMatching(Set<Integer> matchPlayers) {
        for (Integer cid : matchPlayers) {
            MapleMatchCheckingElement mmce = matchEntries.get(cid);
            
            if (mmce != null) {
                synchronized (mmce) {
                    if (!mmce.isMatchActive()) {
                        matchEntries.remove(cid);
                    }
                }
            }
        }
    }
    
    public int getMatchConfirmationLeaderid(int cid) {
        MapleMatchCheckingElement mmce = matchEntries.get(cid);
        if (mmce != null) {
            return mmce.leaderCid;
        } else {
            return -1;
        }
    }
    
    public MatchCheckerType getMatchConfirmationType(int cid) {
        MapleMatchCheckingElement mmce = matchEntries.get(cid);
        if (mmce != null) {
            return mmce.matchType;
        } else {
            return null;
        }
    }
    
    public boolean isMatchConfirmationActive(int cid) {
        MapleMatchCheckingElement mmce = matchEntries.get(cid);
        if (mmce != null) {
            return mmce.active;
        } else {
            return false;
        }
    }
    
    private MapleMatchCheckingElement createMatchConfirmationInternal(MatchCheckerType matchType, int world, int leaderCid, AbstractMatchCheckerListener leaderListener, Set<Integer> players, String message) {
        MapleMatchCheckingElement mmce = new MapleMatchCheckingElement(matchType, leaderCid, world, leaderListener, players, message);
        
        for (Integer cid : players) {
            matchEntries.put(cid, mmce);
        }
        
        acceptMatchElement(mmce, leaderCid);
        return mmce;
    }
    
    public boolean createMatchConfirmation(MatchCheckerType matchType, int world, int leaderCid, Set<Integer> players, String message) {
        MapleMatchCheckingElement mmce = null;
        try {
            semaphorePool.acquire();
            try {
                if (poolMatchPlayers(players)) {
                    try {
                        if (isMatchingAvailable(players)) {
                            AbstractMatchCheckerListener leaderListener = matchType.getListener();
                            mmce = createMatchConfirmationInternal(matchType, world, leaderCid, leaderListener, players, message);
                        } else {
                            reenablePlayerMatching(players);
                        }
                    } finally {
                        unpoolMatchPlayers(players);
                    }
                }
            } finally {
                semaphorePool.release();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
        if (mmce != null) {
            mmce.dispatchMatchCreated();
            return true;
        } else {
            return false;
        }
    }
    
    private void disposeMatchElement(MapleMatchCheckingElement mmce) {
        Set<Integer> matchPlayers = mmce.getMatchPlayers();     // thanks Ai for noticing players getting match-stuck on certain cases
        while (!poolMatchPlayers(matchPlayers)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {}
        }
        
        try {
            for (Integer cid : matchPlayers) {
                matchEntries.remove(cid);
            }
        } finally {
            unpoolMatchPlayers(matchPlayers);
        }
    }
    
    private boolean acceptMatchElement(MapleMatchCheckingElement mmce, int cid) {
        if (mmce.acceptEntry(cid)) {
            unpoolMatchPlayer(cid);
            disposeMatchElement(mmce);
            
            return true;
        } else {
            return false;
        }
    }
    
    private void denyMatchElement(MapleMatchCheckingElement mmce, int cid) {
        unpoolMatchPlayer(cid);
        disposeMatchElement(mmce);
    }
    
    private void dismissMatchElement(MapleMatchCheckingElement mmce, int cid) {
        mmce.setMatchActive(false);
        
        unpoolMatchPlayer(cid);
        disposeMatchElement(mmce);
    }
    
    public boolean answerMatchConfirmation(int cid, boolean accept) {
        MapleMatchCheckingElement mmce = null;
        try {
            semaphorePool.acquire();
            try {
                while (matchEntries.containsKey(cid)) {
                    if (poolMatchPlayer(cid)) {
                        try {
                            mmce = matchEntries.get(cid);
                            
                            if (mmce != null) {
                                synchronized (mmce) {
                                    if (!mmce.isMatchActive()) {    // thanks Alex (Alex-0000) for noticing that exploiters could stall on match checking
                                        matchEntries.remove(cid);
                                        mmce = null;
                                    } else {
                                        if (accept) {
                                            if (!acceptMatchElement(mmce, cid)) {
                                                mmce = null;
                                            }
                                            
                                            break;  // thanks Rohenn for noticing loop scenario here
                                        } else {
                                            denyMatchElement(mmce, cid);
                                            matchEntries.remove(cid);
                                        }
                                    }
                                }
                            }
                        } finally {
                            unpoolMatchPlayer(cid);
                        }
                    }
                }
            } finally {
                semaphorePool.release();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
        if (mmce != null) {
            mmce.dispatchMatchResult(accept);
        }
        
        return false;
    }
    
    public boolean dismissMatchConfirmation(int cid) {
        MapleMatchCheckingElement mmce = null;
        try {
            semaphorePool.acquire();
            try {
                while (matchEntries.containsKey(cid)) {
                    if (poolMatchPlayer(cid)) {
                        try {
                            mmce = matchEntries.get(cid);

                            if (mmce != null) {
                                synchronized (mmce) {
                                    if (!mmce.isMatchActive()) {
                                        mmce = null;
                                    } else {
                                        dismissMatchElement(mmce, cid);
                                    }
                                }
                            }
                        } finally {
                            unpoolMatchPlayer(cid);
                        }
                    }
                }
            } finally {
                semaphorePool.release();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
        if (mmce != null) {
            mmce.dispatchMatchDismissed();
            return true;
        } else {
            return false;
        }
    }
        
}
