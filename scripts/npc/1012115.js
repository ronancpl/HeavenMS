function start() {
    var status = cm.getQuestStatus(20706);
    
    if (status == 0) {
        cm.sendNext("It looks like there's nothing suspicious in the area.");
    } else if (status == 1) {
        cm.forceCompleteQuest(20706);
        cm.sendNext("You have spotted the shadow! Better report to #p1103001#.");
    } else if (status == 2) {
        cm.sendNext("The shadow has already been spotted. Better report to #p1103001#.");
    }
    cm.dispose();
}