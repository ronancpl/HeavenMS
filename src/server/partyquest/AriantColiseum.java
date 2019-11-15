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
package server.partyquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import client.MapleCharacter;
import constants.game.GameConstants;
import server.TimerManager;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan
 */
public class AriantColiseum {
    
    private MapleExpedition exped;
    private MapleMap map;
    
    private Map<MapleCharacter, Integer> score;
    private Map<MapleCharacter, Integer> rewardTier;
    private boolean scoreDirty = false;
    
    private ScheduledFuture<?> ariantUpdate;
    private ScheduledFuture<?> ariantFinish;
    private ScheduledFuture<?> ariantScoreboard;
    
    private int lostShards = 0;
    
    private boolean eventClear = false;
    
    public AriantColiseum(MapleMap eventMap, MapleExpedition expedition) {
        exped = expedition;
        exped.finishRegistration();
        
        map = eventMap;
        map.resetFully();
        
        int pqTimer = 10 * 60 * 1000;
        int pqTimerBoard = (9 * 60 * 1000) + 50 * 1000;
        
        List<MapleCharacter> players = exped.getActiveMembers();
        score = new HashMap<>();
        rewardTier = new HashMap<>();
        for (MapleCharacter mc : players) {
            mc.changeMap(map, 0);
            mc.setAriantColiseum(this);
            mc.updateAriantScore();
            rewardTier.put(mc, 0);
        }
        
        for (MapleCharacter mc : players) {
            mc.announce(MaplePacketCreator.updateAriantPQRanking(score));
        }
        
        setAriantScoreBoard(TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                showArenaResults();
            }
        }, pqTimerBoard));
        
        setArenaFinish(TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                enterKingsRoom();
            }
        }, pqTimer));
        
        setArenaUpdate(TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                broadcastAriantScoreUpdate();
            }
        }, 500, 500));
    }
    
    private void setArenaUpdate(ScheduledFuture<?> ariantUpdate) {
        this.ariantUpdate = ariantUpdate;
    }
    
    private void setArenaFinish(ScheduledFuture<?> arenaFinish) {
        this.ariantFinish = arenaFinish;
    }
    
    private void setAriantScoreBoard(ScheduledFuture<?> ariantScore) {
        this.ariantScoreboard = ariantScore;
    }
    
    private void cancelArenaUpdate() {
        if (ariantUpdate != null) {
            ariantUpdate.cancel(true);
            ariantUpdate = null;
        }
    }
    
    private void cancelArenaFinish() {
        if (ariantFinish != null) {
            ariantFinish.cancel(true);
            ariantFinish = null;
        }
    }
    
    private void cancelAriantScoreBoard() {
        if (ariantScoreboard != null) {
            ariantScoreboard.cancel(true);
            ariantScoreboard = null;
        }
    }
    
    private void cancelAriantSchedules() {
        cancelArenaUpdate();
        cancelArenaFinish();
        cancelAriantScoreBoard();
    }
    
    public int getAriantScore(MapleCharacter chr) {
        Integer chrScore = score.get(chr);
        return chrScore != null ? chrScore : 0;
    }
    
    public void clearAriantScore(MapleCharacter chr) {
        score.remove(chr);
    }
    
    public void updateAriantScore(MapleCharacter chr, int points) {
        if (map != null) {
            score.put(chr, points);
            scoreDirty = true;
        }
    }
    
    private void broadcastAriantScoreUpdate() {
        if (scoreDirty) {
            for (MapleCharacter chr : score.keySet()) {
                chr.announce(MaplePacketCreator.updateAriantPQRanking(score));
            }
            scoreDirty = false;
        }
    }
    
    public int getAriantRewardTier(MapleCharacter chr) {
        Integer reward = rewardTier.get(chr);
        return reward != null ? reward : 0;
    }
    
    public void clearAriantRewardTier(MapleCharacter chr) {
        rewardTier.remove(chr);
    }
    
    public void addLostShards(int quantity) {
        lostShards += quantity;
    }
    
    public void leaveArena(MapleCharacter chr) {
        if (!(eventClear && GameConstants.isAriantColiseumArena(chr.getMapId()))) {
            leaveArenaInternal(chr);
        }
    }
    
    private synchronized void leaveArenaInternal(MapleCharacter chr) {
        if (exped != null) {
            if (exped.removeMember(chr)) {
                int minSize = eventClear ? 1 : 2;
                if (exped.getActiveMembers().size() < minSize) {
                    dispose();
                }
                chr.setAriantColiseum(null);
                
                int shards = chr.countItem(4031868);
                chr.getAbstractPlayerInteraction().removeAll(4031868);
                chr.updateAriantScore(shards);
            }
        }
    }
    
    public void playerDisconnected(MapleCharacter chr) {
        leaveArenaInternal(chr);
    }
    
    private void showArenaResults() {
        eventClear = true;
        
        if (map != null) {
            map.broadcastMessage(MaplePacketCreator.showAriantScoreBoard());
            map.killAllMonsters();
            
            distributeAriantPoints();
        }
    }
    
    private static boolean isUnfairMatch(Integer winnerScore, Integer secondScore, Integer lostShardsScore, List<Integer> runnerupsScore) {
        if (winnerScore <= 0) {
            return false;
        }
        
        double runnerupsScoreCount = 0;
        for (Integer i : runnerupsScore) {
            runnerupsScoreCount += i;
        }
        
        runnerupsScoreCount += lostShardsScore;
        secondScore += lostShardsScore;
        
        double matchRes = runnerupsScoreCount / winnerScore;
        double runnerupRes = ((double) secondScore) / winnerScore;
        
        return matchRes < 0.81770726891980117713114871015349 && (runnerupsScoreCount < 7 || runnerupRes < 0.5929);
    }
    
    public void distributeAriantPoints() {
        Integer firstTop = -1, secondTop = -1;
        MapleCharacter winner = null;
        List<Integer> runnerups = new ArrayList<>();
        
        for (Entry<MapleCharacter, Integer> e : score.entrySet()) {
            Integer s = e.getValue();
            if (s > firstTop) {
                secondTop = firstTop;
                firstTop = s;
                winner = e.getKey();
            } else if (s > secondTop) {
                secondTop = s;
            }
            
            runnerups.add(s);
            rewardTier.put(e.getKey(), (int) Math.floor(s / 10));
        }
        
        runnerups.remove(firstTop);
        if (isUnfairMatch(firstTop, secondTop, map.getDroppedItemsCountById(4031868) + lostShards, runnerups)) {
            rewardTier.put(winner, 1);
        }
    }
    
    private MapleExpeditionType getExpeditionType() {
        MapleExpeditionType type;
        if (map.getId() == 980010101) {
            type = MapleExpeditionType.ARIANT;
        } else if (map.getId() == 980010201) {
            type = MapleExpeditionType.ARIANT1;
        } else {
            type = MapleExpeditionType.ARIANT2;
        }
        
        return type;
    }
    
    private void enterKingsRoom() {
        exped.removeChannelExpedition(map.getChannelServer());
        cancelAriantSchedules();
        
        for (MapleCharacter chr : map.getAllPlayers()) {
            chr.changeMap(980010010, 0);
        }
    }
    
    private synchronized void dispose() {
        if (exped != null) {
            exped.dispose(false);
            
            for (MapleCharacter chr : exped.getActiveMembers()) {
                chr.setAriantColiseum(null);
                chr.changeMap(980010000, 0);
            }
            
            map.getWorldServer().registerTimedMapObject(new Runnable() {
                @Override
                public void run() {
                    score.clear();
                    exped = null;
                    map = null;
                }
            }, 5 * 60 * 1000);
        }
    }
}
