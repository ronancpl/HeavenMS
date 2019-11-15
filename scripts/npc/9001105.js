var status;
 
function start() {
        status = -1;
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        if (cm.getPlayer().getMapId() == 922240200)  {
                                cm.sendOk("That's a shame, come back when your ready.");
                        }
                        
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if(status == 0) {
                        if (cm.getMapId() == 922240200) {
                                cm.sendSimple("Did you have something to say...? #b\b\r\n#L0#I want to rescue Gaga.#l\r\n");    //#L1#I want to go to the Space Mine.#l
                        } else if (cm.getMapId() >= 922240000 && cm.getMapId() <= 922240019) {
                                cm.sendYesNo("Don't worry if you fail. You'll have 3 chances. Do you still want to give up?"); 
                        } else if (cm.getMapId() >= 922240100 && cm.getMapId() <= 922240119) {
                                var text = "You went through so much trouble to rescue Gaga, but it looks like we're back to square one. ";				
                                var rgaga = cm.getPlayer().getEvents().get("rescueGaga");
                                if (rgaga.getCompleted() > 10) {
                                        text += "Please don't give up until Gaga is rescued. To show you my appreciation for what you've accomplished thus far, I've given you a Spaceship. It's rather worn out, but it should still be operational. Check your #bSkill Window#k.";
                                        rgaga.giveSkill(cm.getPlayer());
                                } else 
                                        text += "Let's go back now.";

                                cm.sendNext(text); 
                        }
                } else {
                        if (cm.getPlayer().getMapId() == 922240200) {
                                if (status == 1) {
                                        if(selection == 0) {
                                                selected = 1;
                                                cm.sendNext("Welcome! I heard what happened from Baby Moon Bunny I'm glad you came since I was Planning on requesting some help. Gaga is a friend of mine who has helped me before and often stops by to say hello. Unfortunately, he was kidnapped by aliens."); 
                                        } else {
                                                selected = 2;
                                                cm.sendYesNo("At the Space Mine, you can find special ores called #bKrypto Crystals#k that contains the mysterious power of space. #bKrypto Crystals#l are usually emerald in color, but will turn brown if hit with the Spaceship's #bSpace Beam#k. Remember, in order to thwart this alien conspracy, #b10 Brown Krypto Crystal's and 10 Emerald Krypto Crystal's are needed. But since even #b1 Krypto Crystal#k can be of help, brign me as many as possible. Oh, and one more thing! The Space Mines are protected by the Space Mateons. They are extemely strong due to the power of the #Krypto Crystals#k, so don't try to defeat them. Simply concentrate on quickly collecting the crystals."); 
                                        } 
                                } else if (status == 2) {
                                        if(selected == 1) {
                                                cm.sendYesNo("If we just leave Gaga with the aliens, something terrible will happen to him! I'll let you borrow a spaceship that the Moon Bunnies use for traveling so that you can rescue Gaga.#b Although he might appear a bit indecisive, slow, and immature at times#k, he's really a nice young man. Do you want to go rescue him now?");
                                        } else if(selected == 2) { 
                                                cm.sendOk("Not coded yet, f4."); 
                                                cm.dispose();
                                        }
                                } else if (status == 3) {
                                        var em = cm.getEventManager("RescueGaga");
                                        if (em == null) {
                                                cm.sendOk("This event is currently unavailable.");
                                        } else if (!em.startInstance(cm.getPlayer())) {
                                                cm.sendOk("There is currently someone in this map, come back later.");
                                        }
                                        
                                        cm.dispose();
                                }
                        } else if (cm.getPlayer().getMapId() >= 922240000 && cm.getPlayer().getMapId() <= 922240019) {
                                cm.warp(922240200, 0);
                                cm.dispose();
                        } else if (cm.getPlayer().getMapId() >= 922240100 && cm.getPlayer().getMapId() <= 922240119) {
                                cm.warp(922240200, 0);
                                cm.dispose();
                        }
                }
        }
}