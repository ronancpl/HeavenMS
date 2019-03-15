package server.partyquest;

import client.MapleDisease;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import server.life.MobSkillFactory;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MobSkill;

/**
    *@author Drago/Dragohe4rt
*/
public class MapleCarnivalFactory {

    private final static MapleCarnivalFactory instance = new MapleCarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap<Integer, MCSkill>();
    private final Map<Integer, MCSkill> guardians = new HashMap<Integer, MCSkill>();
    private final MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));

    public MapleCarnivalFactory() {
        //whoosh
	initialize();
    }

    public static final MapleCarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (skills.size() != 0) {
            return;
        }
        for (MapleData z : dataRoot.getData("MCSkill.img")) {
            skills.put(Integer.parseInt(z.getName()), new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), MapleDataTool.getInt("target", z, 1) > 1));
        }
        for (MapleData z : dataRoot.getData("MCGuardian.img")) {
            guardians.put(Integer.parseInt(z.getName()), new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), true));
        }
    }

    public MCSkill getSkill(final int id) {
        return skills.get(id);
    }

    public MCSkill getGuardian(final int id) {
        return guardians.get(id);
    }

    public static class MCSkill {

        public int cpLoss, skillid, level;
        public boolean targetsAll;

        public MCSkill(int _cpLoss, int _skillid, int _level, boolean _targetsAll) {
            cpLoss = _cpLoss;
            skillid = _skillid;
            level = _level;
            targetsAll = _targetsAll;
        }

        public MobSkill getSkill() {
            return getMobSkill(skillid);
        }
        
        public static MobSkill getMobSkill(int skillid) {
            return MobSkillFactory.getMobSkill(skillid, 1); //level?
        }

        public MapleDisease getDisease() {
            if (skillid <= 0) {
                return MapleDisease.getRandom();
            }
            return MapleDisease.getBySkill(skillid);
        }
    }
}
