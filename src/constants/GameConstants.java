package constants;

import client.MapleJob;
import constants.skills.Aran;
import server.maps.MapleMap;
import server.maps.FieldLimit;

/*
 * @author kevintjuh93
 * @author Ronan
 */
public class GameConstants {
    public static final int[] OWL_DATA = new int[]{1082002, 2070005, 2070006, 1022047, 1102041, 2044705, 2340000, 2040017, 1092030, 2040804};
    
    // Ronan's rates upgrade system
    private static final int[] DROP_RATE_GAIN = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    private static final int[] MESO_RATE_GAIN = {1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105};
    private static final int[]  EXP_RATE_GAIN = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610};    //fibonacci :3
    
    public static int getPlayerBonusDropRate(int slot) {
        return(DROP_RATE_GAIN[slot]);
    }
    
    public static int getPlayerBonusMesoRate(int slot) {
        return(MESO_RATE_GAIN[slot]);
    }
    
    public static int getPlayerBonusExpRate(int slot) {
        return(EXP_RATE_GAIN[slot]);
    }
    
    // MapleStory default keyset
    private static final int[] DEFAULT_KEY = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41, 39};
    private static final int[] DEFAULT_TYPE = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
    private static final int[] DEFAULT_ACTION = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
    
    // MapleSolaxiaV2 custom keyset
    private static final int[] CUSTOM_KEY = {2, 3, 4, 5, 31, 56, 59, 32, 42, 6, 17, 29, 30, 41, 50, 60, 61, 62, 63, 64, 65, 16, 7, 9, 13, 8};
    private static final int[] CUSTOM_TYPE = {4, 4, 4, 4, 5, 5, 6, 5, 5, 4, 4, 4, 5, 4, 4, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4, 4};
    private static final int[] CUSTOM_ACTION = {1, 0, 3, 2, 53, 54, 100, 52, 51, 19, 5, 9, 50, 7, 22, 101, 102, 103, 104, 105, 106, 8, 17, 26, 20, 4};
    
    public static int[] getCustomKey(boolean customKeyset) {
        return(customKeyset ? CUSTOM_KEY : DEFAULT_KEY);
    }
    
    public static int[] getCustomType(boolean customKeyset) {
        return(customKeyset ? CUSTOM_TYPE : DEFAULT_TYPE);
    }
    
    public static int[] getCustomAction(boolean customKeyset) {
        return(customKeyset ? CUSTOM_ACTION : DEFAULT_ACTION);
    }
    
    private static final int[] mobHpVal = {0, 15, 20, 25, 35, 50, 65, 80, 95, 110, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350,
        375, 405, 435, 465, 495, 525, 580, 650, 720, 790, 900, 990, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800,
        1900, 2000, 2100, 2200, 2300, 2400, 2520, 2640, 2760, 2880, 3000, 3200, 3400, 3600, 3800, 4000, 4300, 4600, 4900, 5200,
        5500, 5900, 6300, 6700, 7100, 7500, 8000, 8500, 9000, 9500, 10000, 11000, 12000, 13000, 14000, 15000, 17000, 19000, 21000, 23000,
        25000, 27000, 29000, 31000, 33000, 35000, 37000, 39000, 41000, 43000, 45000, 47000, 49000, 51000, 53000, 55000, 57000, 59000, 61000, 63000,
        65000, 67000, 69000, 71000, 73000, 75000, 77000, 79000, 81000, 83000, 85000, 89000, 91000, 93000, 95000, 97000, 99000, 101000, 103000,
        105000, 107000, 109000, 111000, 113000, 115000, 118000, 120000, 125000, 130000, 135000, 140000, 145000, 150000, 155000, 160000, 165000, 170000, 175000, 180000,
        185000, 190000, 195000, 200000, 205000, 210000, 215000, 220000, 225000, 230000, 235000, 240000, 250000, 260000, 270000, 280000, 290000, 300000, 310000, 320000,
        330000, 340000, 350000, 360000, 370000, 380000, 390000, 400000, 410000, 420000, 430000, 440000, 450000, 460000, 470000, 480000, 490000, 500000, 510000, 520000,
        530000, 550000, 570000, 590000, 610000, 630000, 650000, 670000, 690000, 710000, 730000, 750000, 770000, 790000, 810000, 830000, 850000, 870000, 890000, 910000};
    
    public static int getJobMaxLevel(MapleJob job) {
        if(job.getId() % 1000 == 0) {   // beginner
            return 10;
            
        } else if(job.getId() % 100 == 0) {   // 1st job
            return 30;
            
        } else {
            int jobBranch = job.getId() % 10;
            
            switch(jobBranch) {
                case 0:
                    return 70;   // 2nd job
                    
                case 1:
                    return 120;   // 3rd job
                    
                default:
                    return (job.getId() / 1000 == 1) ? 120 : 200;   // 4th job: cygnus is 120, rest is 200
            }
        }
    }
    
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
    	return Aran.FULL_SWING == skill || Aran.OVER_SWING == skill || Aran.COMBO_TEMPEST == skill || Aran.COMBO_FENRIR == skill || Aran.COMBO_DRAIN == skill 
    			|| Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill
    			|| Aran.COMBO_SMASH == skill || Aran.DOUBLE_SWING  == skill || Aran.TRIPLE_SWING == skill;
    }
    
    public static boolean isHiddenSkills(final int skill) {
    	return Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill;
    }
    
    public static boolean isAran(final int job) {
        return job == 2000 || (job >= 2100 && job <= 2112);
    }
    
    private static boolean isInBranchJobTree(int skillJobId, int jobId, int branchType) {
        int branch = (int)(Math.pow(10, branchType));
        
        int skillBranch = (int)(skillJobId / branch) * branch;
        int jobBranch = (int)(jobId / branch) * branch;
        
        return skillBranch == jobBranch;
    }
    
    private static boolean hasDivergedBranchJobTree(int skillJobId, int jobId, int branchType) {
        int branch = (int)(Math.pow(10, branchType));
        
        int skillBranch = (int)(skillJobId / branch);
        int jobBranch = (int)(jobId / branch);
        
        return skillBranch != jobBranch && skillBranch % 10 != 0;
    }
    
    public static boolean isInJobTree(int skillId, int jobId) {
        int skillJob = skillId / 10000;
        
        if(!isInBranchJobTree(skillJob, jobId, 0)) {
            for(int i = 1; i <= 3; i++) {
                if(hasDivergedBranchJobTree(skillJob, jobId, i)) return false;
                if(isInBranchJobTree(skillJob, jobId, i)) return (skillJob <= jobId);
            }
        } else {
            return (skillJob <= jobId);
        }
        
        return false;
    }
    
    public static boolean isPqSkill(final int skill) {
    	return (skill >= 20000014 && skill <= 20000018) || skill == 10000013 || skill == 20001013 || (skill % 10000000 >= 1009 && skill % 10000000 <= 1011) || skill % 10000000 == 1020;
    }
    
    public static boolean bannedBindSkills(final int skill) {
    	return isAranSkills(skill) || isPqSkill(skill);
    }

    public static boolean isGMSkills(final int skill) {
    	return skill >= 9001000 && skill <= 9101008 || skill >= 8001000 && skill <= 8001001; 
    }
    
    public static boolean isFreeMarketRoom(int mapid) {
        return mapid > 910000000 && mapid < 910000023;
    }
    
    public static boolean isMerchantLocked(MapleMap map) {
        if(FieldLimit.CANNOTMIGRATE.check(map.getFieldLimit())) {   // maps that cannot access cash shop cannot access merchants too (except FM rooms).
            return true;
        }
        
        switch(map.getId()) {
            case 910000000:
                return true;
        }
        
        return false;
    }
    
    public static boolean isBossRush(int mapid) {
        return mapid >= 970030100 && mapid <= 970042711;
    }
    
    public static boolean isDojo(int mapid) {
        return mapid >= 925020000 && mapid < 925040000;
    }
    
    public static boolean isPyramid(int mapid) {
    	return mapid >= 926010010 & mapid <= 930010000;
    }
    
    public static boolean isPqSkillMap(int mapid) {
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
        
    public static int getMonsterHP(final int level) {
        if (level < 0 || level >= mobHpVal.length) {
            return Integer.MAX_VALUE;
        }
        return mobHpVal[level];
    }
}
