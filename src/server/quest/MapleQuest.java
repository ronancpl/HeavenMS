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
package server.quest;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleQuestStatus.Status;
import java.util.EnumMap;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.quest.actions.*;
import server.quest.requirements.*;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class MapleQuest {

    private static Map<Integer, MapleQuest> quests = new HashMap<>();
    protected short infoNumber, id;
    protected int timeLimit, timeLimit2;
    protected String infoex;
    protected Map<MapleQuestRequirementType, MapleQuestRequirement> startReqs = new EnumMap<>(MapleQuestRequirementType.class);
    protected Map<MapleQuestRequirementType, MapleQuestRequirement> completeReqs = new EnumMap<>(MapleQuestRequirementType.class);
    protected Map<MapleQuestActionType, MapleQuestAction> startActs = new EnumMap<>(MapleQuestActionType.class);
    protected Map<MapleQuestActionType, MapleQuestAction> completeActs = new EnumMap<>(MapleQuestActionType.class);
    protected List<Integer> relevantMobs = new LinkedList<>();
    private boolean autoStart;
    private boolean autoPreComplete, autoComplete;
    private boolean repeatable = false;
    private final static MapleDataProvider questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Quest.wz"));
	private static MapleData questInfo;
	private static MapleData questAct;
	private static MapleData questReq;
	
    private MapleQuest(int id) {
        this.id = (short) id;

		if(questInfo != null) {
			timeLimit = MapleDataTool.getInt("timeLimit", questInfo, 0);
			timeLimit2 = MapleDataTool.getInt("timeLimit2", questInfo, 0);
			autoStart = MapleDataTool.getInt("autoStart", questInfo, 0) == 1;
			autoPreComplete = MapleDataTool.getInt("autoPreComplete", questInfo, 0) == 1;
			autoComplete = MapleDataTool.getInt("autoComplete", questInfo, 0) == 1;
		}
		
        MapleData reqData = questReq.getChildByPath(String.valueOf(id));
        if (reqData == null) {//most likely infoEx
            return;
        }
        MapleData startReqData = reqData.getChildByPath("0");
        if (startReqData != null) {
            for (MapleData startReq : startReqData.getChildren()) {
                MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(startReq.getName());
                if (type.equals(MapleQuestRequirementType.INTERVAL)) {
                    repeatable = true;
                }
				
				if (type.equals(MapleQuestRequirementType.INFO_NUMBER)) {
                    infoNumber = (short) MapleDataTool.getInt(startReq, 0);
                }
				
                MapleQuestRequirement req = this.getRequirement(type, startReq);
				
				if(req == null)
					continue;
				
                if (type.equals(MapleQuestRequirementType.MOB)) {
                    for (MapleData mob : startReq.getChildren()) {
                        relevantMobs.add(MapleDataTool.getInt(mob.getChildByPath("id")));
                    }
                }
                startReqs.put(type, req);
            }
        }
        MapleData completeReqData = reqData.getChildByPath("1");
        if (completeReqData != null) {
            for (MapleData completeReq : completeReqData.getChildren()) {
				MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(completeReq.getName());
                MapleQuestRequirement req = this.getRequirement(type, completeReq);
				if(req == null)
					continue;
				
                if (type.equals(MapleQuestRequirementType.INFO_NUMBER)) {
                    infoNumber = (short) MapleDataTool.getInt(completeReq, 0);
                }
                if (type.equals(MapleQuestRequirementType.MOB)) {
                    for (MapleData mob : completeReq.getChildren()) {
                        relevantMobs.add(MapleDataTool.getInt(mob.getChildByPath("id")));
                    }
                }
                completeReqs.put(type, req);
            }
        }
        MapleData actData = questAct.getChildByPath(String.valueOf(id));
        if (actData == null) {
            return;
        }
        final MapleData startActData = actData.getChildByPath("0");
        if (startActData != null) {
            for (MapleData startAct : startActData.getChildren()) {
                MapleQuestActionType questActionType = MapleQuestActionType.getByWZName(startAct.getName());
				MapleQuestAction act = this.getAction(questActionType, startAct);
				
				if(act == null)
					continue;
				
                startActs.put(questActionType, act);
            }
        }
        MapleData completeActData = actData.getChildByPath("1");
        if (completeActData != null) {
            for (MapleData completeAct : completeActData.getChildren()) {
				MapleQuestActionType questActionType = MapleQuestActionType.getByWZName(completeAct.getName());
				MapleQuestAction act = this.getAction(questActionType, completeAct);
				
				if(act == null)
					continue;
				
                completeActs.put(questActionType, act);
            }
        }
    }
	
	public boolean isAutoComplete() {
		return autoPreComplete || autoComplete;
	}
	
	public boolean isAutoStart() {
		return autoStart;
	}

    public static MapleQuest getInstance(int id) {
        MapleQuest ret = quests.get(id);
        if (ret == null) {
			questInfo = questData.getData("QuestInfo.img");
			questReq = questData.getData("Check.img");
			questAct = questData.getData("Act.img");
			
            ret = new MapleQuest(id);
            quests.put(id, ret);
        }
        return ret;
    }

    public boolean canStart(MapleCharacter c, int npcid) {
        if (c.getQuest(this).getStatus() != Status.NOT_STARTED && !(c.getQuest(this).getStatus() == Status.COMPLETED && repeatable)) {
            return false;
        }
        for (MapleQuestRequirement r : startReqs.values()) {
            if (!r.check(c, npcid)) {
                return false;
            }
        }
        return true;
    }

    public boolean canComplete(MapleCharacter c, Integer npcid) {
        if (!c.getQuest(this).getStatus().equals(Status.STARTED)) {
            return false;
        }
        for (MapleQuestRequirement r : completeReqs.values()) {
            if (r == null || !r.check(c, npcid)) {
                return false;
            }
        }
        return true;
    }

    public void start(MapleCharacter c, int npc) {
        if (autoStart || canStart(c, npc)) {
            for (MapleQuestAction a : startActs.values()) {
				if (!a.check(c, null)) { // would null be good ?
					return;
				}
                a.run(c, null);
            }
            forceStart(c, npc);
        }
    }

    public void complete(MapleCharacter c, int npc) {
        complete(c, npc, null);
    }

    public void complete(MapleCharacter c, int npc, Integer selection) {
		if (autoPreComplete || canComplete(c, npc)) {
		   for (MapleQuestAction a : completeActs.values()) {
			  if (!a.check(c, selection)) {
			 	 return;
			  }
		    }
		    forceComplete(c, npc);
		    for (MapleQuestAction a : completeActs.values()) {
		        a.run(c, selection);
		    }
		}
    }

    public void reset(MapleCharacter c) {
        c.updateQuest(new MapleQuestStatus(this, MapleQuestStatus.Status.NOT_STARTED));
    }

    public void forfeit(MapleCharacter c) {
        if (!c.getQuest(this).getStatus().equals(Status.STARTED)) {
            return;
        }
        if (timeLimit > 0) {
            c.announce(MaplePacketCreator.removeQuestTimeLimit(id));
        }
        MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.NOT_STARTED);
        newStatus.setForfeited(c.getQuest(this).getForfeited() + 1);
        c.updateQuest(newStatus);
    }

    public boolean forceStart(MapleCharacter c, int npc) {
        MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.STARTED, npc);
        newStatus.setForfeited(c.getQuest(this).getForfeited());

        if (timeLimit > 0) {
            c.questTimeLimit(this, 30000);//timeLimit * 1000
        }
        if (timeLimit2 > 0) {//=\

        }
        c.updateQuest(newStatus);
        return true;
    }

    public boolean forceComplete(MapleCharacter c, int npc) {
        MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.COMPLETED, npc);
        newStatus.setForfeited(c.getQuest(this).getForfeited());
        newStatus.setCompletionTime(System.currentTimeMillis());
        c.updateQuest(newStatus);
        return true;
    }

    public short getId() {
        return id;
    }

    public List<Integer> getRelevantMobs() {
        return relevantMobs;
    }

    public int getItemAmountNeeded(int itemid) {
        MapleQuestRequirement req = completeReqs.get(MapleQuestRequirementType.ITEM);
		if(req == null)
			return 0;
		
		ItemRequirement ireq = (ItemRequirement) req;
		
		return ireq.getItemAmountNeeded(itemid);
    }

    public int getMobAmountNeeded(int mid) {
        MapleQuestRequirement req = completeReqs.get(MapleQuestRequirementType.MOB);
		if(req == null)
			return 0;
		
		MobRequirement mreq = (MobRequirement) req;
		
		return mreq.getRequiredMobCount(mid);
    }

    public short getInfoNumber() {
        return infoNumber;
    }

    public String getInfoEx() {
        MapleQuestRequirement req = startReqs.get(MapleQuestRequirementType.INFO_EX);
		String ret = "";
		if(req != null) {
			InfoExRequirement ireq = (InfoExRequirement) req;
			ret = ireq.getFirstInfo();
		} else { // Check complete requirements.
			req = completeReqs.get(MapleQuestRequirementType.INFO_EX);
			if(req != null) {
				InfoExRequirement ireq = (InfoExRequirement) req;
				ret = ireq.getFirstInfo();
			}
		}
		return ret;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
	
	public static void clearCache(int quest) {
		if(quests.containsKey(quest)){
			quests.remove(quest);
		}
	}
	
	public static void clearCache() {
		quests.clear();
	}
	
	private MapleQuestRequirement getRequirement(MapleQuestRequirementType type, MapleData data) {
		MapleQuestRequirement ret = null;
		switch(type) {
			case END_DATE:
				ret = new EndDateRequirement(this, data);
				break;
			case JOB:
				ret = new JobRequirement(this, data);
				break;
			case QUEST:
				ret = new QuestRequirement(this, data);
				break;
			case FIELD_ENTER:
				ret = new FieldEnterRequirement(this, data);
				break;
			case INFO_EX:
				ret = new InfoExRequirement(this, data);
				break;
			case INTERVAL:
				ret = new IntervalRequirement(this, data);
				break;
			case COMPLETED_QUEST:
				ret = new CompletedQuestRequirement(this, data);
				break;
			case ITEM:
				ret = new ItemRequirement(this, data);
				break;
			case MAX_LEVEL:
				ret = new MaxLevelRequirement(this, data);
				break;
			case MIN_LEVEL:
				ret = new MinLevelRequirement(this, data);
				break;
			case MIN_PET_TAMENESS:
				ret = new MinTamenessRequirement(this, data);
				break;
			case MOB:
				ret = new MobRequirement(this, data);
				break;
			case MONSTER_BOOK:
				ret = new MonsterBookCountRequirement(this, data);
				break;
			case NPC:
				ret = new NpcRequirement(this, data);
				break;
			case PET:
				ret = new PetRequirement(this, data);
				break;
			case SCRIPT:
			case NORMAL_AUTO_START:
			case START:
			case END:
			case INFO_NUMBER:
				break;
			default:
				//FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, "Unhandled Requirement Type: " + type.toString() + " QuestID: " + this.getId());
				break;
		}
		return ret;
	}
	
	private MapleQuestAction getAction(MapleQuestActionType type, MapleData data) {
		MapleQuestAction ret = null;
		switch(type) {
			case BUFF:
				ret = new BuffAction(this, data);
				break;
			case EXP:
				ret = new ExpAction(this, data);
				break;
			case FAME:
				ret = new FameAction(this, data);
				break;
			case ITEM:
				ret = new ItemAction(this, data);
				break;
			case MESO:
				ret = new MesoAction(this, data);
				break;
			case NEXTQUEST:
				ret = new NextQuestAction(this, data);
				break;
			case PETSKILL:
				ret = new PetSkillAction(this, data);
				break;
			case QUEST:
				ret = new QuestAction(this, data);
				break;
			case SKILL:
				ret = new SkillAction(this, data);
				break;
			default:
				//FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, "Unhandled Action Type: " + type.toString() + " QuestID: " + this.getId());
				break;
		}
		return ret;
	}
	
	public static void loadAllQuest() {
		questInfo = questData.getData("QuestInfo.img");
		questReq = questData.getData("Check.img");
		questAct = questData.getData("Act.img");
		
		try {
			for(MapleData quest : questInfo.getChildren()) {
				int questID = Integer.parseInt(quest.getName());
				
				quests.put(questID, new MapleQuest(questID));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
