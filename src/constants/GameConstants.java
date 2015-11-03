package constants;

import client.MapleJob;
import constants.skills.Aran;

/*
 * @author kevintjuh93
 */
public class GameConstants {

    public static int getHiddenSkill(final int skill) {
        switch (skill) {
            case Aran.HIDDEN_FULL_DOUBLE:
            case Aran.HIDDEN_FULL_TRIPLE:
                return Aran.FULL_SWING;
            case Aran.HIDDEN_OVER_DOUBLE:
            case Aran.HIDDEN_OVER_TRIPLE:
                return Aran.OVER_SWING;
        }
        return skill;
    }
    
    public static int getSkillBook(final int job) {
        if (job >= 2210 && job <= 2218) {
             return job - 2209;
        }
        return 0;
    }
    
    
    public static boolean isAranSkills(final int skill) {
    	return Aran.FULL_SWING == skill || Aran.OVER_SWING == skill || Aran.COMBO_TEMPEST == skill || Aran.COMBO_PENRIL == skill || Aran.COMBO_DRAIN == skill 
    			|| Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill
    			|| Aran.COMBO_SMASH == skill || Aran.DOUBLE_SWING  == skill || Aran.TRIPLE_SWING == skill;
    }
    
    public static boolean isHiddenSkills(final int skill) {
    	return Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill;
    }
    
    public static boolean isAran(final int job) {
        return job == 2000 || (job >= 2100 && job <= 2112);
    }
    
    public static boolean isInJobTree(int skillId, int jobId) {
    	int skill = skillId / 10000;
    	if ((jobId - skill) + skill == jobId) {
    		return true;
    	}
    	return false;
    }
    
    public static boolean isPqSkill(final int skill) {
    	return skill >= 20001013 && skill <= 20000018 || skill  % 10000000 == 1020 || skill == 10000013 || skill  % 10000000 >= 1009 && skill % 10000000 <= 1011;  
    }
    
    public static boolean bannedBindSkills(final int skill) {
    	return isAranSkills(skill) || isPqSkill(skill);
    }

    public static boolean isGMSkills(final int skill) {
    	return skill >= 9001000 && skill <= 9101008 || skill >= 8001000 && skill <= 8001001; 
    }
    
    public static boolean isDojo(int mapid) {
        return mapid >= 925020100 && mapid <= 925023814;
    }
    
    public static boolean isPyramid(int mapid) {
    	return mapid >= 926010010 & mapid <= 930010000;
    }
    
    public static boolean isPQSkillMap(int mapid) {
    	return isDojo(mapid) || isPyramid(mapid);
    }
    
    public static boolean isFinisherSkill(int skillId) {
        return skillId > 1111002 && skillId < 1111007 || skillId == 11111002 || skillId == 11111003;
    }
    
	public static boolean hasSPTable(MapleJob job) {
        switch (job) {
			case EVAN:
            case EVAN1:
            case EVAN2:
            case EVAN3:
            case EVAN4:
            case EVAN5:
            case EVAN6:
            case EVAN7:
            case EVAN8:
            case EVAN9:
            case EVAN10:
                return true;
            default:
                return false;
        }
    }
}
