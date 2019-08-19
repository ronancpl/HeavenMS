package client;

public enum MapleFamilyEntitlement {
    FAMILY_REUINION(1, 300, "Family Reunion", "[Target] Me\\n[Effect] Teleport directly to the Family member of your choice."),
    SUMMON_FAMILY(1, 500, "Summon Family", "[Target] 1 Family member\\n[Effect] Summon a Family member of choice to the map you're in."),
    SELF_DROP_1_5(1, 700, "My Drop Rate 1.5x (15 min)", "[Target] Me\\n[Time] 15 min.\\n[Effect] Monster drop rate will be increased #c1.5x#.\\n*  If the Drop Rate event is in progress, this will be nullified."),
    SELF_EXP_1_5(1, 800, "My EXP 1.5x (15 min)", "[Target] Me\\n[Time] 15 min.\\n[Effect] EXP earned from hunting will be increased #c1.5x#.\\n* If the EXP event is in progress, this will be nullified."),
    FAMILY_BONDING(1, 1000, "Family Bonding (30 min)", "[Target] At least 6 Family members online that are below me in the Pedigree\\n[Time] 30 min.\\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \\n* If the EXP event is in progress, this will be nullified."),
    SELF_DROP_2(1, 1200, "My Drop Rate 2x (15 min)", "[Target] Me\\n[Time] 15 min.\\n[Effect] Monster drop rate will be increased #c2x#.\\n* If the Drop Rate event is in progress, this will be nullified."),
    SELF_EXP_2(1, 1500, "My EXP 2x (15 min)", "[Target] Me\\n[Time] 15 min.\\n[Effect] EXP earned from hunting will be increased #c2x#.\\n* If the EXP event is in progress, this will be nullified."),
    SELF_DROP_2_30MIN(1, 2000, "My Drop Rate 2x (30 min)", "[Target] Me\\n[Time] 30 min.\\n[Effect] Monster drop rate will be increased #c2x#.\\n* If the Drop Rate event is in progress, this will be nullified."),
    SELF_EXP_2_30MIN(1, 2500, "My EXP 2x (30 min)", "[Target] Me\\n[Time] 30 min.\\n[Effect] EXP earned from hunting will be increased #c2x#. \\n* If the EXP event is in progress, this will be nullified."),
    PARTY_DROP_2_30MIN(1, 4000, "My Party Drop Rate 2x (30 min)", "[Target] My party\\n[Time] 30 min.\\n[Effect] Monster drop rate will be increased #c2x#.\\n* If the Drop Rate event is in progress, this will be nullified."),
    PARTY_EXP_2_30MIN(1, 5000, "My Party EXP 2x (30 min)", "[Target] My party\\n[Time] 30 min.\\n[Effect] EXP earned from hunting will be increased #c2x#.\\n* If the EXP event is in progress, this will be nullified.");
    
    private final int usageLimit, repCost;
    private final String name, description;
    
    private MapleFamilyEntitlement(int usageLimit, int repCost, String name, String description) {
        this.usageLimit = usageLimit;
        this.repCost = repCost;
        this.name = name;
        this.description = description;
    }
    
    public int getUsageLimit() {
        return usageLimit;
    }
    
    public int getRepCost() {
        return repCost;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
