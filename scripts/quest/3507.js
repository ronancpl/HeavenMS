function end(mode, type, selection) {
        if(qm.isQuestCompleted(3523) || qm.isQuestCompleted(3524) || qm.isQuestCompleted(3525) || qm.isQuestCompleted(3526) || qm.isQuestCompleted(3527) || qm.isQuestCompleted(3529) || qm.isQuestCompleted(3539)) {
            qm.completeQuest();
            qm.sendOk("You are now filled with all of your memories again.. You are now allowed to go to #m270020000#.");
        } else {
            qm.sendOk("You have not yet checked with your first teacher about your memories?");
        }
    
        qm.dispose();
}