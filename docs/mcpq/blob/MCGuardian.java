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

package server.partyquest.mcpq;

/**
 * Object representing MCGuardian in Skill.wz/MCGuardian.img.
 *
 * @author s4nta
 */

public class MCGuardian {
    private final int type, spendCP, mobSkillID, level;
    private String name, desc;

    public MCGuardian(int type, int spendCP, int mobSkillID, int level) {
        this.type = type;
        this.mobSkillID = mobSkillID;
        this.level = level;
        this.spendCP = spendCP;
    }

    public int getType() {
        return type;
    }

    public int getSpendCP() {
        return spendCP;
    }

    public int getMobSkillID() {
        return mobSkillID;
    }

    public int getLevel() {
        return level;
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
