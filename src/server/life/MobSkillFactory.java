/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

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
package server.life;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkillFactory {

    private static Map<String, MobSkill> mobSkills = new HashMap<String, MobSkill>();
    private final static MapleDataProvider dataSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
    private static MapleData skillRoot = dataSource.getData("MobSkill.img");
    private final static ReentrantReadWriteLock dataLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MOBSKILL_FACTORY);
    private final static ReadLock rL = dataLock.readLock();
    private final static WriteLock wL = dataLock.writeLock();

    public static MobSkill getMobSkill(final int skillId, final int level) {
        final String key = skillId + "" + level;
        rL.lock();
        try {
            MobSkill ret = mobSkills.get(key);
            if (ret != null) {
                return ret;
            }
        } finally {
            rL.unlock();
        }
        wL.lock();
        try {
            MobSkill ret;
            ret = mobSkills.get(key);
            if (ret == null) {
                MapleData skillData = skillRoot.getChildByPath(skillId + "/level/" + level);
                if (skillData != null) {
                    int mpCon = MapleDataTool.getInt(skillData.getChildByPath("mpCon"), 0);
                    List<Integer> toSummon = new ArrayList<Integer>();
                    for (int i = 0; i > -1; i++) {
                        if (skillData.getChildByPath(String.valueOf(i)) == null) {
                            break;
                        }
                        toSummon.add(Integer.valueOf(MapleDataTool.getInt(skillData.getChildByPath(String.valueOf(i)), 0)));
                    }
                    int effect = MapleDataTool.getInt("summonEffect", skillData, 0);
                    int hp = MapleDataTool.getInt("hp", skillData, 100);
                    int x = MapleDataTool.getInt("x", skillData, 1);
                    int y = MapleDataTool.getInt("y", skillData, 1);
                    long duration = MapleDataTool.getInt("time", skillData, 0) * 1000;
                    long cooltime = MapleDataTool.getInt("interval", skillData, 0) * 1000;
                    int iprop = MapleDataTool.getInt("prop", skillData, 100);
                    float prop = iprop / 100;
                    int limit = MapleDataTool.getInt("limit", skillData, 0);
                    MapleData ltd = skillData.getChildByPath("lt");
                    Point lt = null;
                    Point rb = null;
                    if (ltd != null) {
                        lt = (Point) ltd.getData();
                        rb = (Point) skillData.getChildByPath("rb").getData();
                    }
                    ret = new MobSkill(skillId, level);
                    ret.addSummons(toSummon);
                    ret.setCoolTime(cooltime);
                    ret.setDuration(duration);
                    ret.setHp(hp);
                    ret.setMpCon(mpCon);
                    ret.setSpawnEffect(effect);
                    ret.setX(x);
                    ret.setY(y);
                    ret.setProp(prop);
                    ret.setLimit(limit);
                    ret.setLtRb(lt, rb);
                }
                mobSkills.put(skillId + "" + level, ret);
            }
            return ret;
        } finally {
            wL.unlock();
        }
    }
}
