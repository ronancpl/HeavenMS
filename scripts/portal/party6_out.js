function enter(pi) {
        if ((pi.getMap().getMonsters().size() == 0 || pi.getMap().getMonsterById(9300183) != null) && (pi.getMap().getReactorByName("") == null || pi.getMap().getReactorByName("").getState() == 1)) {
                if(pi.isLeader()) {
                        pi.clearPQ(930000800);
                        return true;
                }
                else {
                        pi.playerMessage(5, "Wait for the leader to pass through the portal.");
                        return false;
                }


        } else {
                pi.playerMessage(5, "Please eliminate the Poison Golem.");
                return false;
        }
}