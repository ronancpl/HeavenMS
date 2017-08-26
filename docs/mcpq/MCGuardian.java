/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
