/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.map;

import client.MapleCharacter.DelayedQuestUpdate;
import client.MapleClient;
import client.MapleQuestStatus;
import scripting.AbstractPlayerInteraction;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

public class MapScriptMethods extends AbstractPlayerInteraction {
   
	private String rewardstring = " title has been rewarded. Please see NPC Dalair to receive your Medal.";
    
	public MapScriptMethods(MapleClient c) {
        super(c);
    }
    
    public void displayCygnusIntro() {
        switch (c.getPlayer().getMapId()) {
            case 913040100:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene0"));
                break;
            case 913040101:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene1"));
                break;
            case 913040102:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene2"));
                break;
            case 913040103:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene3"));
                break;
            case 913040104:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene4"));
                break;
            case 913040105:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene5"));
                break;
            case 913040106:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene6"));
                break;
        }
    }
    
    public void displayAranIntro() {
        switch (c.getPlayer().getMapId()) {
            case 914090010:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene0"));
                break;
            case 914090011:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene1" + c.getPlayer().getGender()));
                break;
            case 914090012:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene2" + c.getPlayer().getGender()));
                break;
            case 914090013:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene3"));
                break;
            case 914090100:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/HandedPoleArm" + c.getPlayer().getGender()));
                break;
        }
    }

    public void startExplorerExperience() {
        if (c.getPlayer().getMapId() == 1020100) //Swordman
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/swordman/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020200) //Magician
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/magician/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020300) //Archer
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/archer/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020400) //Rogue
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/rogue/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020500) //Pirate
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/pirate/Scene" + c.getPlayer().getGender()));
        }
    }

    public void goAdventure() {
        lockUI();
        c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/goAdventure/Scene" + c.getPlayer().getGender()));
    }

    public void goLith() {
        lockUI();
        c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/goLith/Scene" + c.getPlayer().getGender()));
    }

    public void explorerQuest(short questid, String questName) {
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (!isQuestStarted(questid)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus qs = getPlayer().getQuest(quest);
        if (!qs.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(qs.getMedalProgress());
        String infoex = qs.getInfoEx(0);
        getPlayer().announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
        StringBuilder smp = new StringBuilder();
        StringBuilder etm = new StringBuilder();
        if (status.equals(infoex)) {
            etm.append("Earned the ").append(questName).append(" title!");
            smp.append("You have earned the <").append(questName).append(">").append(rewardstring);
            getPlayer().announce(MaplePacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            getPlayer().announce(MaplePacketCreator.earnTitleMessage(status + "/" + infoex + " regions explored."));
            etm.append("Trying for the ").append(questName).append(" title.");
            smp.append("You made progress on the ").append(questName).append(" title. ").append(status).append("/").append(infoex);
        }
        getPlayer().announce(MaplePacketCreator.earnTitleMessage(etm.toString()));
        showInfoText(smp.toString());
    }

    public void touchTheSky() { //29004
        MapleQuest quest = MapleQuest.getInstance(29004);
        if (!isQuestStarted(29004)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus qs = getPlayer().getQuest(quest);
        if (!qs.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(qs.getMedalProgress());
        getPlayer().announceUpdateQuest(DelayedQuestUpdate.UPDATE, qs, true);
        getPlayer().announce(MaplePacketCreator.earnTitleMessage(status + "/5 Completed"));
        getPlayer().announce(MaplePacketCreator.earnTitleMessage("The One Who's Touched the Sky title in progress."));
        if (Integer.toString(qs.getMedalProgress()).equals(qs.getInfoEx(0))) {
            showInfoText("The One Who's Touched the Sky" + rewardstring);
            getPlayer().announce(MaplePacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            showInfoText("The One Who's Touched the Sky title in progress. " + status + "/5 Completed");
        }
    }
}
