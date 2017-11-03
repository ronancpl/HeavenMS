package server.partyquest.mcpq;

/*
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

/**
 * Object representing MCSkill in Skill.wz/MCSkill.img.
 *
 * @author s4nta
 */
public class MCSkill {
    private final int id, target, mobSkillID, level, spendCP;
    private String name, desc;

    public MCSkill(int id, int target, int mobSkillID, int level, int spendCP) {
        this.id = id;
        this.target = target;
        this.mobSkillID = mobSkillID;
        this.level = level;
        this.spendCP = spendCP;
    }

    public int getId() {
        return id;
    }

    public int getTarget() {
        return target;
    }

    public int getMobSkillID() {
        return mobSkillID;
    }

    public int getLevel() {
        return level;
    }

    public int getSpendCP() {
        return spendCP;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}