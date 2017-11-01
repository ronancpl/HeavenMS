/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.partyquest.mcpq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataTool;

/**
 * Representation of the data tree inside of the Map.wz file.
 *
 * @author s4nta
 */
public class MCWZData {

    public List<MCMobGenPos> mobGenPosList = new ArrayList<>();
    public List<MCGuardianGenPos> guardianGenPosList = new ArrayList<>();
    public Map<Integer, MCSummonMob> summons = new HashMap<>();
    public Map<Integer, Integer> skills = new HashMap<>();
    public Map<Integer, Integer> guardians = new HashMap<>();

    public String effectWin, effectLose, soundWin, soundLose;
    public int rewardMapWin, rewardMapLose;

    public int mobGenMax, guardianGenMax;
    public boolean mapDivided;
    public int deathCP;
    public int reactorRed, reactorBlue;

    public MCWZData(MapleData src) {
        parse(src);
    }

    public void parse(MapleData src) {
        populateMobGenPos(src.getChildByPath("mobGenPos"));
        populateSummonMobs(src.getChildByPath("mob"));
        effectWin = MapleDataTool.getString("effectWin", src);
        effectLose = MapleDataTool.getString("effectLose", src);
        soundWin = MapleDataTool.getString("soundWin", src);
        soundLose = MapleDataTool.getString("soundLose", src);

        rewardMapWin = MapleDataTool.getInt("rewardMapWin", src);
        rewardMapLose = MapleDataTool.getInt("rewardMapLose", src);

        populateSkills(src.getChildByPath("skill"));
        populateGuardianGenPos(src.getChildByPath("guardianGenPos"));
        populateGuardians(src.getChildByPath("guardian"));

        mobGenMax = MapleDataTool.getInt("mobGenMax", src, 20); // HACK: 20 default
        guardianGenMax = MapleDataTool.getInt("guardianGenMax", src, 20); // HACK: 20 default

        mapDivided = MapleDataTool.getInt("mapDivided", src) > 0;

        deathCP = MapleDataTool.getInt("deathCP", src);
        reactorRed = MapleDataTool.getInt("reactorRed", src);
        reactorBlue = MapleDataTool.getInt("reactorBlue", src);
    }

    private void populateMobGenPos(MapleData src) {
        for (MapleData n : src) {
            MCMobGenPos nn = new MCMobGenPos(MapleDataTool.getInt("x", n, 0),
            MapleDataTool.getInt("y", n, 0),
            MapleDataTool.getInt("fh", n, 0),
            MapleDataTool.getInt("cy", n, 0),
            MapleDataTool.getInt("team", n, -1));
            mobGenPosList.add(nn);
        }
    }

    private void populateSummonMobs(MapleData src) {
        for (MapleData n : src) {
            int id = Integer.parseInt(n.getName());
            MCSummonMob mcs = new MCSummonMob(
            MapleDataTool.getInt("id", n, 0),
            MapleDataTool.getInt("spendCP", n, 0),
            MapleDataTool.getInt("mobTime", n, 0)
            );

            this.summons.put(id, mcs);
        }
    }

    private void populateSkills(MapleData src) {
        for (MapleData n : src) {
            int key = Integer.parseInt(n.getName());
            int val = MapleDataTool.getInt(n);

            skills.put(key, val);
        }
    }

    private void populateGuardianGenPos(MapleData src) {
        for (MapleData n : src) {
            MCGuardianGenPos nn = new MCGuardianGenPos(MapleDataTool.getInt("x", n, 0),
            MapleDataTool.getInt("y", n, 0),
            MapleDataTool.getInt("f", n, 0),
            MapleDataTool.getInt("team", n, -1));
            guardianGenPosList.add(nn);
        }
    }

    private void populateGuardians(MapleData src) {
        for (MapleData n : src) {
            int key = Integer.parseInt(n.getName());
            int val = MapleDataTool.getInt(n);

            guardians.put(key, val);
        }
    }

    public class MCMobGenPos {

        public final int x, y, fh, cy, team;

        private MCMobGenPos(int x, int y, int fh, int cy, int team) {
            this.x = x;
            this.y = y;
            this.fh = fh;
            this.cy = cy;
            this.team = team;
        }
    }

    public class MCGuardianGenPos {

        public final int x, y, f, team;

        private MCGuardianGenPos(int x, int y, int f, int team) {
            this.x = x;
            this.y = y;
            this.f = f;
            this.team = team;
        }
    }

    public class MCSummonMob {

        public final int id, spendCP, mobTime;

        private MCSummonMob(int id, int spendCP, int mobTime) {
            this.id = id;
            this.spendCP = spendCP;
            this.mobTime = mobTime;
        }
    }
}  