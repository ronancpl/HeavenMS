var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.sendNext("Whoa, whoa! Are you really declining my offer? Well, you'll be able to #blevel-up quicker #kwith our help, so let me know if you change your mind. Even if you've declined a Quest, you can receive the Quest again if you just come and talk to me.");
        qm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            qm.sendNext("Welcome to Ereve! And you are? Oh, you're #b#h ##k! \r\nGood to meet you. I've been waiting. You've come to become a Cygnus Knight, right? My name is Kimu, and I'm currently guiding Noblesses like you at the request of Empress Cygnus.");
        } else if (status == 1) {
            qm.sendNextPrev("If you want to officially become a part of Cygnus Knights, you must first meet the Empress. She's at the center of this island, accompained by Shinsoo. My brothers and I would like to share with you a few things that are considered #bBasic Knowledge#k in Maple World before you go. Would that be okay?");
        } else if (status == 2) {
            qm.sendOk("Oh, let me warn you that this is a Quest. You may have noticed that NPCs around Maple World occasionally ask you for various favors. A favor of that sort is called a #bQuest#k. You will receive reward items or EXP upon completing Quests, so I strongly suggest you diligently fulfill the favors of Maple NPCs.");
        } else if (status == 3) {
            qm.sendAcceptDecline("Would you like to meet #bKizan#k, who can tell you about hunting? You can find Kizan by following the arrow to the left.");
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.guideHint(2);
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        qm.dispose();
    } else {
        if (status == 0) {
            qm.sendNext("Are you the Noblesse my brother Kimu sent? Nice to meet you! I'm Kizan. I'll give you the reward Kimu asked me to give you. Remember, you can check your Inventory by pressing the #bI key#k. Red potions help you recover HP, and blue ones help recover MP. It's a good idea to learn how to use them beforehand so you'll be ready with them when you're in danger. \r\n\r\n#fUI/UIWindow.img/Quest/reward# \r\n\r\n#v2000020# #z2000020# \r\n#v2000021# #z2000021# \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#15 exp");
        } else if (status == 1) {
            if(qm.canHold(2000022) && qm.canHold(2000023)){
                if(!qm.isQuestCompleted(21010)) {		
                    qm.gainItem(2000020, 5);
                    qm.gainItem(2000021, 5);
                    qm.gainExp(15);
                }
                qm.guideHint(3);
                qm.forceCompleteQuest();
            } else {
                qm.dropMessage(1, "Your inventory is full");
            }

            qm.dispose();        
        }
    }
}