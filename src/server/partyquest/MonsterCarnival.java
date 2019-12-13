package server.partyquest;

import java.util.concurrent.ScheduledFuture;
import client.MapleCharacter;
import config.YamlConfig;
import constants.string.LanguageConstants;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;

/**
 * @author Drago (Dragohe4rt)
 */
public class MonsterCarnival {

    public static int D = 3;
    public static int C = 2;
    public static int B = 1;
    public static int A = 0;

    private MapleParty p1, p2;
    private MapleMap map;
    private ScheduledFuture<?> timer, effectTimer, respawnTask;
    private long startTime = 0;
    private int summonsR = 0, summonsB = 0, room = 0;
    private MapleCharacter leader1, leader2, team1, team2;
    private int redCP, blueCP, redTotalCP, blueTotalCP, redTimeupCP, blueTimeupCP;
    private boolean cpq1;

    public MonsterCarnival(MapleParty p1, MapleParty p2, int mapid, boolean cpq1, int room) {
        try {
            this.cpq1 = cpq1;
            this.room = room;
            this.p1 = p1;
            this.p2 = p2;
            Channel cs = Server.getInstance().getWorld(p2.getLeader().getWorld()).getChannel(p2.getLeader().getChannel());
            p1.setEnemy(p2);
            p2.setEnemy(p1);
            map = cs.getMapFactory().getDisposableMap(mapid);
            startTime = System.currentTimeMillis() + 10 * 60 * 1000;
            int redPortal = 0;
            int bluePortal = 0;
            if (map.isPurpleCPQMap()) {
                redPortal = 2;
                bluePortal = 1;
            }
            for (MaplePartyCharacter mpc : p1.getMembers()) {
                MapleCharacter mc = mpc.getPlayer();
                if (mc != null) {
                    mc.setMonsterCarnival(this);
                    mc.setTeam(0);
                    mc.setFestivalPoints(0);
                    mc.forceChangeMap(map, map.getPortal(redPortal));
                    mc.dropMessage(6, LanguageConstants.getMessage(mc, LanguageConstants.CPQEntry));
                    if (p1.getLeader().getId() == mc.getId()) {
                        leader1 = mc;
                    }
                    team1 = mc;
                }
            }
            for (MaplePartyCharacter mpc : p2.getMembers()) {
                MapleCharacter mc = mpc.getPlayer();
                if (mc != null) {
                    mc.setMonsterCarnival(this);
                    mc.setTeam(1);
                    mc.setFestivalPoints(0);
                    mc.forceChangeMap(map, map.getPortal(bluePortal));
                    mc.dropMessage(6, LanguageConstants.getMessage(mc, LanguageConstants.CPQEntry));
                    if (p2.getLeader().getId() == mc.getId()) {
                        leader2 = mc;
                    }
                    team2 = mc;
                }
            }
            if (team1 == null || team2 == null) {
                for (MaplePartyCharacter mpc : p1.getMembers()) {
                    MapleCharacter chr = mpc.getPlayer();
                    if (chr != null) {
                        chr.dropMessage(5, LanguageConstants.getMessage(chr, LanguageConstants.CPQError));
                    }
                }
                for (MaplePartyCharacter mpc : p2.getMembers()) {
                    MapleCharacter chr = mpc.getPlayer();
                    if (chr != null) {
                        chr.dropMessage(5, LanguageConstants.getMessage(chr, LanguageConstants.CPQError));
                    }
                }
                return;
            }
            
            // thanks Atoot, Vcoc for noting double CPQ functional being sent to players in CPQ start
            
            timer = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    timeUp();
                }
            }, map.getTimeDefault() * 1000); // thanks Atoot for noticing an irregular "event extended" issue here
            effectTimer = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    complete();
                }
            }, map.getTimeDefault() * 1000 - 10 * 1000);
            respawnTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    respawn();
                }
            }, YamlConfig.config.server.RESPAWN_INTERVAL);
            
            cs.initMonsterCarnival(cpq1, room);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void respawn() {
        map.respawn();
    }
    
    public void playerDisconnected(int charid) {
        int team = -1;
        for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
            if (mpc.getId() == charid) {
                team = 0;
            }
        }
        for (MaplePartyCharacter mpc : leader2.getParty().getMembers()) {
            if (mpc.getId() == charid) {
                team = 1;
            }
        }
        for (MapleCharacter chrMap : map.getAllPlayers()) {
            if (team == -1) {
                team = 1;
            }
            String teamS = "";
            switch (team) {
                case 0:
                    teamS = LanguageConstants.getMessage(chrMap, LanguageConstants.CPQRed);
                    break;
                case 1:
                    teamS = LanguageConstants.getMessage(chrMap, LanguageConstants.CPQBlue);
                    break;
            }
            chrMap.dropMessage(5, teamS + LanguageConstants.getMessage(chrMap, LanguageConstants.CPQPlayerExit));
        }
        earlyFinish();
    }

    private void earlyFinish() {
        dispose(true);
    }

    public void leftParty(int charid) {
        playerDisconnected(charid);
    }

    protected void dispose() {
        dispose(false);
    }

    public boolean canSummonR() {
        return summonsR < map.getMaxMobs();
    }
    
    public void summonR() {
        summonsR++;
    }

    public boolean canSummonB() {
        return summonsB < map.getMaxMobs();
    }
    
    public void summonB() {
        summonsB++;
    }
    
    public boolean canGuardianR() {
        int teamReactors = 0;
        for (MapleReactor react : map.getAllReactors()) {
            if (react.getName().substring(0, 1).contentEquals("0")) {
                teamReactors += 1;
            }
        }
        
        return teamReactors < map.getMaxReactors();
    }
    
    public boolean canGuardianB() {
        int teamReactors = 0;
        for (MapleReactor react : map.getAllReactors()) {
            if (react.getName().substring(0, 1).contentEquals("1")) {
                teamReactors += 1;
            }
        }
        
        return teamReactors < map.getMaxReactors();
    }

    protected void dispose(boolean warpout) {
        Channel cs = map.getChannelServer();
        MapleMap out;
        if (!cpq1) { // cpq2
            out = cs.getMapFactory().getMap(980030010);
        } else {
            out = cs.getMapFactory().getMap(980000010);
        }
        for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
            MapleCharacter mc = mpc.getPlayer();
            if (mc != null) {
                mc.resetCP();
                mc.setTeam(-1);
                mc.setMonsterCarnival(null);
                if (warpout) {
                    mc.changeMap(out, out.getPortal(0));
                }
            }
        }
        for (MaplePartyCharacter mpc : leader2.getParty().getMembers()) {
            MapleCharacter mc = mpc.getPlayer();
            if (mc != null) {
                mc.resetCP();
                mc.setTeam(-1);
                mc.setMonsterCarnival(null);
                if (warpout) {
                    mc.changeMap(out, out.getPortal(0));
                }
            }
        }
        if (this.timer != null) {
            this.timer.cancel(true);
            this.timer = null;
        }
        if (this.effectTimer != null) {
            this.effectTimer.cancel(true);
            this.effectTimer = null;
        }
        if (this.respawnTask != null) {
            this.respawnTask.cancel(true);
            this.respawnTask = null;
        }
        redTotalCP = 0;
        blueTotalCP = 0;
        leader1.getParty().setEnemy(null);
        leader2.getParty().setEnemy(null);
        map.dispose();
        map = null;
        
        cs.finishMonsterCarnival(cpq1, room);
    }

    public void exit() {
        dispose();
    }

    public ScheduledFuture<?> getTimer() {
        return this.timer;
    }

    private void finish(int winningTeam) {
        try {
            Channel cs = map.getChannelServer();
            if (winningTeam == 0) {
                for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
                    MapleCharacter mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.gainFestivalPoints(this.redTotalCP);
                        mc.setMonsterCarnival(null);
                        if (cpq1) {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 2), cs.getMapFactory().getMap(map.getId() + 2).getPortal(0));
                        } else {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 200), cs.getMapFactory().getMap(map.getId() + 200).getPortal(0));
                        }
                        mc.setTeam(-1);
                        mc.dispelDebuffs();
                    }
                }
                for (MaplePartyCharacter mpc : leader2.getParty().getMembers()) {
                    MapleCharacter mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.gainFestivalPoints(this.blueTotalCP);
                        mc.setMonsterCarnival(null);
                        if (cpq1) {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 3), cs.getMapFactory().getMap(map.getId() + 3).getPortal(0));
                        } else {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 300), cs.getMapFactory().getMap(map.getId() + 300).getPortal(0));
                        }
                        mc.setTeam(-1);
                        mc.dispelDebuffs();
                    }
                }
            } else if (winningTeam == 1) {
                for (MaplePartyCharacter mpc : leader2.getParty().getMembers()) {
                    MapleCharacter mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.gainFestivalPoints(this.blueTotalCP);
                        mc.setMonsterCarnival(null);
                        if (cpq1) {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 2), cs.getMapFactory().getMap(map.getId() + 2).getPortal(0));
                        } else {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 200), cs.getMapFactory().getMap(map.getId() + 200).getPortal(0));
                        }
                        mc.setTeam(-1);
                        mc.dispelDebuffs();
                    }
                }
                for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
                    MapleCharacter mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.gainFestivalPoints(this.redTotalCP);
                        mc.setMonsterCarnival(null);
                        if (cpq1) {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 3), cs.getMapFactory().getMap(map.getId() + 3).getPortal(0));
                        } else {
                            mc.changeMap(cs.getMapFactory().getMap(map.getId() + 300), cs.getMapFactory().getMap(map.getId() + 300).getPortal(0));
                        }
                        mc.setTeam(-1);
                        mc.dispelDebuffs();
                    }
                }
            }
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void timeUp() {
        int cp1 = this.redTimeupCP;
        int cp2 = this.blueTimeupCP;
        if (cp1 == cp2) {
            extendTime();
            return;
        }
        if (cp1 > cp2) {
            finish(0);
        } else {
            finish(1);
        }
    }

    public long getTimeLeft() {
        return (startTime - System.currentTimeMillis());
    }

    public int getTimeLeftSeconds() {
        return (int) (getTimeLeft() / 1000);
    }

    private void extendTime() {
        for (MapleCharacter chrMap : map.getAllPlayers()) {
            chrMap.dropMessage(5, LanguageConstants.getMessage(chrMap, LanguageConstants.CPQExtendTime));
        }
        startTime = System.currentTimeMillis() + 3 * 60 * 1000;
        
        map.broadcastMessage(MaplePacketCreator.getClock(3 * 60));
        
        timer = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                timeUp();
            }
        }, map.getTimeExpand() * 1000);
        effectTimer = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                complete();
            }

        }, map.getTimeExpand() * 1000 - 10 * 1000); // thanks Vcoc for noticing a time set issue here
    }

    public void complete() {
        int cp1 = this.redTotalCP;
        int cp2 = this.blueTotalCP;
        
        this.redTimeupCP = cp1;
        this.blueTimeupCP = cp2;
        
        if (cp1 == cp2) {
            return;
        }
        boolean redWin = cp1 > cp2;
        int chnl = leader1.getClient().getChannel();
        int chnl1 = leader2.getClient().getChannel();
        if (chnl != chnl1) {
            throw new RuntimeException("Os lideres estao em canais diferentes.");
        }
        
        map.killAllMonsters();
        for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
            MapleCharacter mc = mpc.getPlayer();
            if (mc != null) {
                if (redWin) {
                    mc.getClient().announce(MaplePacketCreator.showEffect("quest/carnival/win"));
                    mc.getClient().announce(MaplePacketCreator.playSound("MobCarnival/Win"));
                    mc.dispelDebuffs();
                } else {
                    mc.getClient().announce(MaplePacketCreator.showEffect("quest/carnival/lose"));
                    mc.getClient().announce(MaplePacketCreator.playSound("MobCarnival/Lose"));
                    mc.dispelDebuffs();
                }
            }
        }
        for (MaplePartyCharacter mpc : leader2.getParty().getMembers()) {
            MapleCharacter mc = mpc.getPlayer();
            if (mc != null) {
                if (!redWin) {
                    mc.getClient().announce(MaplePacketCreator.showEffect("quest/carnival/win"));
                    mc.getClient().announce(MaplePacketCreator.playSound("MobCarnival/Win"));
                    mc.dispelDebuffs();
                } else {
                    mc.getClient().announce(MaplePacketCreator.showEffect("quest/carnival/lose"));
                    mc.getClient().announce(MaplePacketCreator.playSound("MobCarnival/Lose"));
                    mc.dispelDebuffs();
                }
            }
        }
    }

    public MapleParty getRed() {
        return p1;
    }

    public void setRed(MapleParty p1) {
        this.p1 = p1;
    }

    public MapleParty getBlue() {
        return p2;
    }

    public void setBlue(MapleParty p2) {
        this.p2 = p2;
    }

    public MapleCharacter getLeader1() {
        return leader1;
    }

    public void setLeader1(MapleCharacter leader1) {
        this.leader1 = leader1;
    }

    public MapleCharacter getLeader2() {
        return leader2;
    }

    public void setLeader2(MapleCharacter leader2) {
        this.leader2 = leader2;
    }

    public MapleCharacter getEnemyLeader(int team) {
        switch (team) {
            case 0:
                return leader2;
            case 1:
                return leader1;
        }
        return null;
    }

    public int getBlueCP() {
        return blueCP;
    }

    public void setBlueCP(int blueCP) {
        this.blueCP = blueCP;
    }

    public int getBlueTotalCP() {
        return blueTotalCP;
    }

    public void setBlueTotalCP(int blueTotalCP) {
        this.blueTotalCP = blueTotalCP;
    }

    public int getRedCP() {
        return redCP;
    }

    public void setRedCP(int redCP) {
        this.redCP = redCP;
    }

    public int getRedTotalCP() {
        return redTotalCP;
    }

    public void setRedTotalCP(int redTotalCP) {
        this.redTotalCP = redTotalCP;
    }

    public int getTotalCP(int team) {
        if (team == 0) {
            return redTotalCP;
        } else if (team == 1) {
            return blueTotalCP;
        } else {
            throw new RuntimeException("Equipe desconhecida");
        }
    }

    public void setTotalCP(int totalCP, int team) {
        if (team == 0) {
            this.redTotalCP = totalCP;
        } else if (team == 1) {
            this.blueTotalCP = totalCP;
        }
    }

    public int getCP(int team) {
        if (team == 0) {
            return redCP;
        } else if (team == 1) {
            return blueCP;
        } else {
            throw new RuntimeException("Equipe desconhecida" + team);
        }
    }

    public void setCP(int CP, int team) {
        if (team == 0) {
            this.redCP = CP;
        } else if (team == 1) {
            this.blueCP = CP;
        }
    }
    
    public int getRoom() {
        return this.room;
    }
    
    public MapleMap getEventMap() {
        return this.map;
    }
}
