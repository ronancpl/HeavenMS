package server.partyquest;

import java.util.concurrent.ScheduledFuture;
import client.MapleCharacter;
import constants.LinguaConstants;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.TimerManager;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 * @author Drago/Dragohe4rt
 */

public class MonsterCarnival {

    public static int D = 3;
    public static int C = 2;
    public static int B = 1;
    public static int A = 0;

    private MapleParty p1, p2;
    private MapleMap map;
    private ScheduledFuture<?> timer, effectTimer;
    private long startTime = 0;
    private int summons = 8, summonss = 8;
    private MapleCharacter leader1, leader2, Grupo1, Grupo2;
    private int redCP, blueCP, redTotalCP, blueTotalCP;
    private boolean cpq1;

    public MonsterCarnival(MapleParty p1, MapleParty p2, int mapid, boolean cpq1) {
        try {
            this.cpq1 = cpq1;
            this.p1 = p1;
            this.p2 = p2;
            Channel cs = Server.getInstance().getWorld(p2.getLeader().getWorld()).getChannel(p2.getLeader().getChannel());
            p1.setEnemy(p2);
            p2.setEnemy(p1);
            map = cs.getMapFactory().resetMap(mapid);
            int redPortal = 0;
            int bluePortal = 0;
            if (map.isPurpleCPQMap()) {
                redPortal = 2;
                bluePortal = 1;
            }
            for (MaplePartyCharacter mpc : p1.getMembers()) {
                MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                if (mc != null) {
                    mc.setMonsterCarnival(this);
                    mc.setTeam(0);
                    mc.setFestivalPoints(0);
                    mc.changeMap(map, map.getPortal(redPortal));
                    mc.dropMessage(6, LinguaConstants.Linguas(mc).CPQEntrada);
                    if (p1.getLeader().getId() == mc.getId()) {
                        leader1 = mc;
                    }
                    Grupo1 = mc;
                }
            }
            for (MaplePartyCharacter mpc : p2.getMembers()) {
                MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                if (mc != null) {
                    mc.setMonsterCarnival(this);
                    mc.setTeam(1);
                    mc.setFestivalPoints(0);
                    mc.changeMap(map, map.getPortal(bluePortal));
                    mc.dropMessage(6, LinguaConstants.Linguas(mc).CPQEntrada);
                    if (p2.getLeader().getId() == mc.getId()) {
                        leader2 = mc;
                    }
                    Grupo2 = mc;
                }
            }
            if (Grupo1 == null || Grupo2 == null) {
                for (MaplePartyCharacter mpc : p2.getMembers()) {
                    mpc.getPlayer().dropMessage(5, LinguaConstants.Linguas(mpc.getPlayer()).CPQErro);
                }
                for (MaplePartyCharacter mpc : p2.getMembers()) {
                    mpc.getPlayer().dropMessage(5, LinguaConstants.Linguas(mpc.getPlayer()).CPQErro);
                }
                return;
            }
            Grupo1.getClient().announce(MaplePacketCreator.startMonsterCarnival(Grupo1, 0, 1));
            Grupo2.getClient().announce(MaplePacketCreator.startMonsterCarnival(Grupo2, 1, 0));
            startTime = System.currentTimeMillis() + 60 * 10000;
            timer = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    timeUp();
                }
            }, 10 * 60 * 1000);
            effectTimer = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    complete();
                }
            }, 10 * 60 * 1000 - 10 * 1000);
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    map.addClock(60 * 10);
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    teamS = LinguaConstants.Linguas(chrMap).CPQVermelho;
                    break;
                case 1:
                    teamS = LinguaConstants.Linguas(chrMap).CPQAzul;
                    break;
            }
            chrMap.dropMessage(5, teamS + LinguaConstants.Linguas(chrMap).CPQPlayerExit);
        }
        earlyFinish();
    }

    public void earlyFinish() {
        dispose(true);
    }

    public void leftParty(int charid) {
        playerDisconnected(charid);
    }

    protected void dispose() {
        dispose(false);
    }

    public void summon() {
        this.summons--;
    }

    public boolean canSummon() {
        return this.summons > 0;
    }

    public void summons() {
        this.summonss--;
    }

    public boolean canSummons() {
        return this.summonss > 0;
    }

    protected void dispose(boolean warpout) {
        Channel cs = Server.getInstance().getWorld(p1.getLeader().getWorld()).getChannel(p1.getLeader().getChannel());
        MapleMap out;
        if (!cpq1) { // cpq2
            out = cs.getMapFactory().getMap(980030000);
        } else {
            out = cs.getMapFactory().getMap(980000000);
        }
        for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
            MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
            MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
        redTotalCP = 0;
        blueTotalCP = 0;
        leader1.getParty().setEnemy(null);
        leader2.getParty().setEnemy(null);

    }

    public void exit() {
        dispose();
    }

    public ScheduledFuture<?> getTimer() {
        return this.timer;
    }

    public void finish(int winningTeam) {
        try {
            Channel cs = Server.getInstance().getWorld(p1.getLeader().getWorld()).getChannel(p1.getLeader().getChannel());
            if (winningTeam == 0) {
                for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
                    MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
                    MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
                    MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
                    MapleCharacter mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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

    public void timeUp() {
        int cp1 = this.redTotalCP;
        int cp2 = this.blueTotalCP;
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

    public void extendTime() {
        for (MapleCharacter chrMap : map.getAllPlayers()) {
            chrMap.dropMessage(5, LinguaConstants.Linguas(chrMap).CPQTempoExtendido);
        }
        startTime = System.currentTimeMillis() + 3 * 1000;
        map.addClock(3 * 60);
        timer = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                timeUp();
            }
        }, 3 * 60 * 1000);
        effectTimer = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                complete();
            }
        }, 3 * 60 * 1000 - 10);
    }

    public void complete() {
        int cp1 = this.redTotalCP;
        int cp2 = this.blueTotalCP;
        if (cp1 == cp2) {
            return;
        }
        boolean redWin = cp1 > cp2;
        int chnl = leader1.getClient().getChannel();
        int chnl1 = leader2.getClient().getChannel();
        if (chnl != chnl1) {
            throw new RuntimeException("Os líderes estão em canais diferentes.");
        }

        Channel cs = Server.getInstance().getWorld(p1.getLeader().getWorld()).getChannel(p1.getLeader().getChannel());
        map.killAllMonsters();
        for (MaplePartyCharacter mpc : leader1.getParty().getMembers()) {
            MapleCharacter mc;
            mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
            MapleCharacter mc;
            mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
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
}
