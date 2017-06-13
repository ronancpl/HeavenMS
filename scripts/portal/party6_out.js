function enter(pi) {
        if ((pi.getMap().getMonsters().size() == 0 || pi.getMap().getMonsterById(9300183) != null) && (pi.getMap().getReactorByName("") == null || pi.getMap().getReactorByName("").getState() == 1)) {
                if(pi.isEventLeader()) {
                        pi.getEventInstance().clearPQ();
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