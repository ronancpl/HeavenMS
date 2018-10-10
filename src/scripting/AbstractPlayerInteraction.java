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
package scripting;

import java.awt.Point;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.server.Server;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import scripting.npc.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.partyquest.PartyQuest;
import server.partyquest.Pyramid;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleStat;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryProof;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import client.inventory.manipulator.MapleInventoryManipulator;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import server.life.MapleNPC;
import tools.Pair;

public class AbstractPlayerInteraction {

	public MapleClient c;

	public AbstractPlayerInteraction(MapleClient c) {
		this.c = c;
	}

	public MapleClient getClient() {
		return c;
	}

	public MapleCharacter getPlayer() {
		return c.getPlayer();
	}
        
        public MapleCharacter getChar() {
		return c.getPlayer();
	}
        
        public MapleMap getMap() {
                return c.getPlayer().getMap();
        }
        
        public static int getHourOfDay() {
                return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        }
        
        public int getMarketPortalId(int mapId) {
            return getMarketPortalId(getWarpMap(mapId));
        }
        
        private static int getMarketPortalId(MapleMap map) {
            return (map.findMarketPortal() != null) ? map.findMarketPortal().getId() : map.getRandomPlayerSpawnpoint().getId();
        }
        
	public void warp(int mapid) {
		getPlayer().changeMap(mapid);
	}

	public void warp(int map, int portal) {
		getPlayer().changeMap(map, portal);
	}

	public void warp(int map, String portal) {
		getPlayer().changeMap(map, portal);
	}

	public void warpMap(int map) {
		getPlayer().getMap().warpEveryone(map);
	}

        public void warpParty(int id) {
                warpParty(id, 0);
        }
        
        public void warpParty(int id, int portalId) {
                int mapid = getMapId();
                warpParty(id, portalId, mapid, mapid);
        }
        
        public void warpParty(int id, int fromMinId, int fromMaxId) {
                warpParty(id, 0, fromMinId, fromMaxId);
        }
        
	public void warpParty(int id, int portalId, int fromMinId, int fromMaxId) {
                for (MapleCharacter mc : getPartyMembers()) {
                        if(mc.getMapId() >= fromMinId && mc.getMapId() <= fromMaxId) {
                                mc.changeMap(id, portalId);
                        }
                }
	}

	public List<MapleCharacter> getPartyMembers() {
		if (getPlayer().getParty() == null) {
			return null;
		}
		List<MapleCharacter> chars = new LinkedList<>();
		for (Channel channel : Server.getInstance().getChannelsFromWorld(getPlayer().getWorld())) {
			for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
				if (chr != null) {
					chars.add(chr);
				}
			}
		}
		return chars;
	}

	public MapleMap getWarpMap(int map) {
		return getPlayer().getWarpMap(map);
	}

	public MapleMap getMap(int map) {
		return getWarpMap(map);
	}
        
        public int countAllMonstersOnMap(int map) {
                return getMap(map).countMonsters();
        }
        
        public int countMonster() {
            return getPlayer().getMap().countMonsters();
        }
        
        public void resetMapObjects(int mapid) {
                getWarpMap(mapid).resetMapObjects();
        }

	public EventManager getEventManager(String event) {
		return getClient().getEventManager(event);
	}
        
        public EventInstanceManager getEventInstance() {
		return getPlayer().getEventInstance();
	}
        
        public MapleInventory getInventory(int type) {
                return getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
        }
        
        public MapleInventory getInventory(MapleInventoryType type) {
                return getPlayer().getInventory(type);
        }
        
	public boolean hasItem(int itemid) {
		return haveItem(itemid, 1);
	}

	public boolean hasItem(int itemid, int quantity) {
		return haveItem(itemid, quantity);
	}

	public boolean haveItem(int itemid) {
		return haveItem(itemid, 1);
	}

	public boolean haveItem(int itemid, int quantity) {
		return getPlayer().getItemQuantity(itemid, false) >= quantity;
	}
        
        public int getItemQuantity(int itemid) {
                return getPlayer().getItemQuantity(itemid, false);
        }

        public boolean haveItemWithId(int itemid) {
                return haveItemWithId(itemid, false);
        }
        
        public boolean haveItemWithId(int itemid, boolean checkEquipped) {
                return getPlayer().haveItemWithId(itemid, checkEquipped);
        }
        
	public boolean canHold(int itemid) {
                return canHold(itemid, 1);
        }
        
        public boolean canHold(int itemid, int quantity) {
                return getPlayer().canHold(itemid, quantity);
        }
        
        private static List<Integer> convertToIntegerArray(List<Double> list) {
                List<Integer> intList = new LinkedList<>();
                for(Double d: list) intList.add(d.intValue());

                return intList;
        }
        
        public boolean canHoldAll(List<Double> itemids, List<Double> quantity) {
                return canHoldAll(convertToIntegerArray(itemids), convertToIntegerArray(quantity), true);
        }
        
        private boolean canHoldAll(List<Integer> itemids, List<Integer> quantity, boolean isInteger) {
            int size = Math.min(itemids.size(), quantity.size());
            
            List<Pair<Item, MapleInventoryType>> addedItems = new LinkedList<>();
            for(int i = 0; i < size; i++) {
                Item it = new Item(itemids.get(i), (short) 0, quantity.get(i).shortValue());
                addedItems.add(new Pair<>(it, ItemConstants.getInventoryType(itemids.get(i))));
            }
            
            return MapleInventory.checkSpots(c.getPlayer(), addedItems, false);
        }
        
        public boolean canHold(int itemid, int quantity, int removeItemid, int removeQuantity) {
            return canHoldAllAfterRemoving(Collections.singletonList(itemid), Collections.singletonList(quantity), Collections.singletonList(removeItemid), Collections.singletonList(removeQuantity));
        }
        
        private static List<Pair<Item, MapleInventoryType>> prepareProofInventoryItems(List<Pair<Integer, Integer>> items) {
            List<Pair<Item, MapleInventoryType>> addedItems = new LinkedList<>();
            for(Pair<Integer, Integer> p : items) {
                Item it = new Item(p.getLeft(), (short) 0, p.getRight().shortValue());
                addedItems.add(new Pair<>(it, MapleInventoryType.CANHOLD));
            }
            
            return addedItems;
        }
        
        private static List<List<Pair<Integer, Integer>>> prepareInventoryItemList(List<Integer> itemids, List<Integer> quantity) {
            int size = Math.min(itemids.size(), quantity.size());
            
            List<List<Pair<Integer, Integer>>> invList = new ArrayList<>(6);
            for(int i = MapleInventoryType.UNDEFINED.getType(); i < MapleInventoryType.CASH.getType(); i++) {
                invList.add(new LinkedList<Pair<Integer, Integer>>());
            }
            
            for(int i = 0; i < size; i++) {
                int itemid = itemids.get(i);
                invList.get(ItemConstants.getInventoryType(itemid).getType()).add(new Pair<>(itemid, quantity.get(i)));
            }
            
            return invList;
        }
        
        public boolean canHoldAllAfterRemoving(List<Integer> toAddItemids, List<Integer> toAddQuantity, List<Integer> toRemoveItemids, List<Integer> toRemoveQuantity) {
            List<List<Pair<Integer, Integer>>> toAddItemList = prepareInventoryItemList(toAddItemids, toAddQuantity);
            List<List<Pair<Integer, Integer>>> toRemoveItemList = prepareInventoryItemList(toRemoveItemids, toRemoveQuantity);
            
            MapleInventoryProof prfInv = (MapleInventoryProof) this.getInventory(MapleInventoryType.CANHOLD);
            prfInv.lockInventory();
            try {
                for(int i = MapleInventoryType.EQUIP.getType(); i < MapleInventoryType.CASH.getType(); i++) {
                    List<Pair<Integer, Integer>> toAdd = toAddItemList.get(i);
                    
                    if(!toAdd.isEmpty()) {
                        List<Pair<Integer, Integer>> toRemove = toRemoveItemList.get(i);
                        
                        MapleInventory inv = this.getInventory(i);
                        prfInv.cloneContents(inv);
                        
                        for(Pair<Integer, Integer> p : toRemove) {
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CANHOLD, p.getLeft(), p.getRight(), false, false);
                        }
                        
                        List<Pair<Item, MapleInventoryType>> addItems = prepareProofInventoryItems(toAdd);
                        
                        boolean canHold = MapleInventory.checkSpots(c.getPlayer(), addItems, true);
                        if(!canHold) {
                            return false;
                        }
                    }
                }
            } finally {
                prfInv.flushContents();
                prfInv.unlockInventory();
            }
            
            return true;
        }
     
        //---- \/ \/ \/ \/ \/ \/ \/  NOT TESTED  \/ \/ \/ \/ \/ \/ \/ \/ \/ ----
        
        public final MapleQuestStatus getQuestRecord(final int id) {
            return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
        }

        public final MapleQuestStatus getQuestNoRecord(final int id) {
            return c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(id));
        }
        
        //---- /\ /\ /\ /\ /\ /\ /\  NOT TESTED  /\ /\ /\ /\ /\ /\ /\ /\ /\ ----

	public void openNpc(int npcid) {
		openNpc(npcid, null);
	}

	public void openNpc(int npcid, String script) {
                if(c.getCM() != null) return;
            
		c.removeClickedNPC();
		NPCScriptManager.getInstance().dispose(c);
		NPCScriptManager.getInstance().start(c, npcid, script, null);
	}

        public void updateQuest(int questid, int data) {
            MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(questid));
            updateQuest(questid, status.getAnyProgressKey(), data);
        }
        
        public void updateQuest(int questid, String data) {
            MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(questid));
            updateQuest(questid, status.getAnyProgressKey(), data);
        }
        
        public void updateQuest(int questid, int pid, int data) {
            updateQuest(questid, pid, String.valueOf(data));
        }
        
	public void updateQuest(int questid, int pid, String data) {
		MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(questid));
		status.setStatus(MapleQuestStatus.Status.STARTED);
		status.setProgress(pid, data);//override old if exists
		c.getPlayer().updateQuest(status);
	}

	public int getQuestStatus(int id) {
		return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus().getId();
	}
        
        private MapleQuestStatus.Status getQuestStat(int id) {
                return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus();
        }
        
	public boolean isQuestCompleted(int quest) {
		try {
			return getQuestStat(quest) == MapleQuestStatus.Status.COMPLETED;
		} catch (NullPointerException e) {
                        e.printStackTrace();
			return false;
		}
	}

        public boolean isQuestActive(int quest) {
            return isQuestStarted(quest);
        }
        
	public boolean isQuestStarted(int quest) {
		try {
			return getQuestStat(quest) == MapleQuestStatus.Status.STARTED;
		} catch (NullPointerException e) {
                        e.printStackTrace();
			return false;
		}
	}
        
        public void setQuestProgress(int qid, int progress) {
                MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(qid));
                status.setProgress(status.getAnyProgressKey(), String.valueOf(progress));
        }
        
        public void setQuestProgress(int qid, int pid, int progress) {
                MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(qid));
                status.setProgress(pid, String.valueOf(progress));
	}
        
        public void setStringQuestProgress(int qid, int pid, String progress) {
                MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(qid));
                status.setProgress(pid, progress);
        }
        
        public int getQuestProgress(int qid) {
                MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(qid));
                String progress = status.getProgress(status.getAnyProgressKey());
            
                if(progress.isEmpty()) return 0;
                return Integer.parseInt(progress);
        }
        
        public int getQuestProgress(int qid, int pid) {
                if(getPlayer().getQuest(MapleQuest.getInstance(qid)).getProgress(pid).isEmpty()) return 0;
		return Integer.parseInt(getPlayer().getQuest(MapleQuest.getInstance(qid)).getProgress(pid));
	}
        
        public String getStringQuestProgress(int qid, int pid) {
                if(getPlayer().getQuest(MapleQuest.getInstance(qid)).getProgress(pid).isEmpty()) return "";
                return getPlayer().getQuest(MapleQuest.getInstance(qid)).getProgress(pid);
        }
        
        public void resetAllQuestProgress(int qid) {
                getPlayer().getQuest(MapleQuest.getInstance(qid)).resetAllProgress();
                getClient().announce(MaplePacketCreator.updateQuest(getPlayer().getQuest(MapleQuest.getInstance(qid)), false));
        }
        
        public void resetQuestProgress(int qid, int pid) {
                getPlayer().getQuest(MapleQuest.getInstance(qid)).resetProgress(pid);
                getClient().announce(MaplePacketCreator.updateQuest(getPlayer().getQuest(MapleQuest.getInstance(qid)), false));
        }
        
        public Item evolvePet(byte slot, int afterId) {
            MaplePet evolved = null;
            MaplePet target;
            
            long period = (long) 90 * 24 * 60 * 60 * 1000;    //refreshes expiration date: 90 days
            
            target = getPlayer().getPet(slot);
            if(target == null) {
                getPlayer().message("Pet could not be evolved...");
                return(null);
            }
            
            Item tmp = gainItem(afterId, (short) 1, false, true, period, target);
            getPlayer().unequipPet(target, true, false);
            
            /*
            evolved = MaplePet.loadFromDb(tmp.getItemId(), tmp.getPosition(), tmp.getPetId());
            
            evolved = tmp.getPet();
            if(evolved == null) {
                getPlayer().message("Pet structure non-existent for " + tmp.getItemId() + "...");
                return(null);
            }
            else if(tmp.getPetId() == -1) {
                getPlayer().message("Pet id -1");
                return(null);
            }
            
            getPlayer().addPet(evolved);
            
            getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), evolved, false, false), true);
            c.announce(MaplePacketCreator.petStatUpdate(c.getPlayer()));
            c.announce(MaplePacketCreator.enableActions());
            chr.getClient().getWorldServer().registerPetHunger(chr, chr.getPetIndex(evolved));
            */
            
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, target.getPosition(), (short) 1, false);
            
            return evolved;
        }
        
	public void gainItem(int id, short quantity) {
		gainItem(id, quantity, false, true);
	}

	public void gainItem(int id, short quantity, boolean show) {//this will fk randomStats equip :P
		gainItem(id, quantity, false, show);
	}

	public void gainItem(int id, boolean show) {
		gainItem(id, (short) 1, false, show);
	}

	public void gainItem(int id) {
		gainItem(id, (short) 1, false, true);
	}   

	public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage) {
		return gainItem(id, quantity, randomStats, showMessage, -1);
	}

        public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage, long expires) {
            return gainItem(id, quantity, randomStats, showMessage, expires, null);
        }
        
        public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage, long expires, MaplePet from) {
		Item item = null;
                MaplePet evolved;
                int petId = -1;
                
                if (quantity >= 0) {
                        if (ItemConstants.isPet(id)) {
                                petId = MaplePet.createPet(id);

                                if(from != null) {
                                    evolved = MaplePet.loadFromDb(id, (short) 0, petId);

                                    Point pos = getPlayer().getPosition();
                                    pos.y -= 12;
                                    evolved.setPos(pos);
                                    evolved.setFh(getPlayer().getMap().getFootholds().findBelow(evolved.getPos()).getId());
                                    evolved.setStance(0);
                                    evolved.setSummoned(true);

                                    evolved.setName(from.getName().compareTo(MapleItemInformationProvider.getInstance().getName(from.getItemId())) != 0 ? from.getName() : MapleItemInformationProvider.getInstance().getName(id));
                                    evolved.setCloseness(from.getCloseness());
                                    evolved.setFullness(from.getFullness());
                                    evolved.setLevel(from.getLevel());
                                    evolved.setExpiration(System.currentTimeMillis() + expires);
                                    evolved.saveToDb();
                                }

                                //MapleInventoryManipulator.addById(c, id, (short) 1, null, petId, expires == -1 ? -1 : System.currentTimeMillis() + expires);
                        }
                    
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

			if (ItemConstants.getInventoryType(id).equals(MapleInventoryType.EQUIP)) {
				item = ii.getEquipById(id);
                                
                                if(item != null) {
                                    Equip it = (Equip)item;
                                    if(ItemConstants.isAccessory(item.getItemId()) && it.getUpgradeSlots() <= 0) it.setUpgradeSlots(3);
                                
                                    if(ServerConstants.USE_ENHANCED_CRAFTING == true && c.getPlayer().getCS() == true) {
                                        Equip eqp = (Equip)item;
                                        if(!(c.getPlayer().isGM() && ServerConstants.USE_PERFECT_GM_SCROLL)) {
                                            eqp.setUpgradeSlots((byte)(eqp.getUpgradeSlots() + 1));
                                        }
                                        item = MapleItemInformationProvider.getInstance().scrollEquipWithId(item, 2049100, true, 2049100, c.getPlayer().isGM());
                                    }
                                }
			} else {
				item = new Item(id, (short) 0, quantity, petId);
			}

			if(expires >= 0)
				item.setExpiration(System.currentTimeMillis() + expires);
                        
                        item.setPetId(petId);

			if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
				c.getPlayer().dropMessage(1, "Your inventory is full. Please remove an item from your " + ItemConstants.getInventoryType(id).name() + " inventory.");
				return null;
			}
			if (ItemConstants.getInventoryType(id) == MapleInventoryType.EQUIP) {
				if (randomStats) {
					MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) item), false, petId);
				} else {
					MapleInventoryManipulator.addFromDrop(c, (Equip) item, false, petId);
				}
			} else {
				MapleInventoryManipulator.addFromDrop(c, item, false, petId);
			}
		} else {
			MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(id), id, -quantity, true, false);
		}
		if (showMessage) {
			c.announce(MaplePacketCreator.getShowItemGain(id, quantity, true));
		}

		return item;
	}
        
        public void gainFame(int delta) {
                getPlayer().gainFame(delta);
        }

	public void changeMusic(String songName) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
	}

	public void playerMessage(int type, String message) {
		c.announce(MaplePacketCreator.serverNotice(type, message));
	}

	public void message(String message) {
		getPlayer().message(message);
	}

	public void mapMessage(int type, String message) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
	}

	public void mapEffect(String path) {
		c.announce(MaplePacketCreator.mapEffect(path));
	}

	public void mapSound(String path) {
		c.announce(MaplePacketCreator.mapSound(path));
	}

	public void displayAranIntro() {
		String intro = "";
		switch (c.getPlayer().getMapId()) {
		case 914090010:
			intro = "Effect/Direction1.img/aranTutorial/Scene0";
			break;
		case 914090011:
			intro = "Effect/Direction1.img/aranTutorial/Scene1" + (c.getPlayer().getGender() == 0 ? "0" : "1");
			break;
		case 914090012:
			intro = "Effect/Direction1.img/aranTutorial/Scene2" + (c.getPlayer().getGender() == 0 ? "0" : "1");
			break;
		case 914090013:
			intro = "Effect/Direction1.img/aranTutorial/Scene3";
			break;
		case 914090100:
			intro = "Effect/Direction1.img/aranTutorial/HandedPoleArm" + (c.getPlayer().getGender() == 0 ? "0" : "1");
			break;
		case 914090200:
			intro = "Effect/Direction1.img/aranTutorial/Maha";
			break;
		}
		showIntro(intro);
	}



	public void showIntro(String path) {
		c.announce(MaplePacketCreator.showIntro(path));
	}

	public void showInfo(String path) {
		c.announce(MaplePacketCreator.showInfo(path));
		c.announce(MaplePacketCreator.enableActions());
	}

	public void guildMessage(int type, String message) {
		if (getGuild() != null) {
			getGuild().guildMessage(MaplePacketCreator.serverNotice(type, message));
		}
	}

	public MapleGuild getGuild() {
		try {
			return Server.getInstance().getGuild(getPlayer().getGuildId(), getPlayer().getWorld(), null);
		} catch (Exception e) {
                        e.printStackTrace();
		}
		return null;
	}

	public MapleParty getParty() {
		return getPlayer().getParty();
	}
        
        public boolean isLeader() {
                return isPartyLeader();
        }
        
        public boolean isGuildLeader() {
                return getPlayer().isGuildLeader();
        }

        public boolean isPartyLeader() {
		if(getParty() == null)
			return false;
		
                return getParty().getLeaderId() == getPlayer().getId();
	}
        
        public boolean isEventLeader() {
		return getEventInstance() != null && getPlayer().getId() == getEventInstance().getLeaderId();
	}
        
        public void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			MapleClient cl = chr.getClient();
			if (quantity >= 0) {
				MapleInventoryManipulator.addById(cl, id, quantity);
			} else {
				MapleInventoryManipulator.removeById(cl, ItemConstants.getInventoryType(id), id, -quantity, true, false);
			}
			cl.announce(MaplePacketCreator.getShowItemGain(id, quantity, true));
		}
	}

	public void removeHPQItems() {
		int[] items = {4001095, 4001096, 4001097, 4001098, 4001099, 4001100, 4001101};
		for (int i = 0; i < items.length; i ++) {
			removePartyItems(items[i]);
		}
	}

	public void removePartyItems(int id) {
		if (getParty() == null) {
			removeAll(id);
			return;
		}
		for (MaplePartyCharacter chr : getParty().getMembers()) {
			if (chr != null && chr.isOnline() && chr.getPlayer().getClient() != null){
				removeAll(id, chr.getPlayer().getClient());
			}
		}
	}
        
        public void giveCharacterExp(int amount, MapleCharacter chr) {
                chr.gainExp((amount * chr.getExpRate()), true, true);
        }

	public void givePartyExp(int amount, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			giveCharacterExp(amount, chr);
		}
	}
        
	public void givePartyExp(String PQ) {
		givePartyExp(PQ, true);
	}


	public void givePartyExp(String PQ, boolean instance) {
		//1 player  =  +0% bonus (100)
		//2 players =  +0% bonus (100)
		//3 players =  +0% bonus (100)
		//4 players = +10% bonus (110)
		//5 players = +20% bonus (120)
		//6 players = +30% bonus (130)
		MapleParty party = getPlayer().getParty();
		int size = party.getMembers().size();

		if(instance) {
			for(MaplePartyCharacter member: party.getMembers()) {
				if(member == null || !member.isOnline() || member.getPlayer().getEventInstance() == null){
					size--;
				}
			}
		}

		int bonus = size < 4 ? 100 : 70 + (size * 10);
		for (MaplePartyCharacter member : party.getMembers()) {
			if(member == null || !member.isOnline()){
				continue;
			}
			MapleCharacter player = member.getPlayer();
			if(instance && player.getEventInstance() == null){
				continue; // They aren't in the instance, don't give EXP.
			}
			int base = PartyQuest.getExp(PQ, player.getLevel());
			int exp = base * bonus / 100;
			player.gainExp(exp, true, true);
			if(ServerConstants.PQ_BONUS_EXP_RATE > 0 && System.currentTimeMillis() <= ServerConstants.EVENT_END_TIMESTAMP) {
				player.gainExp((int) (exp * ServerConstants.PQ_BONUS_EXP_RATE), true, true);
			}
		}
	}

	public void removeFromParty(int id, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			MapleInventoryType type = ItemConstants.getInventoryType(id);
			MapleInventory iv = chr.getInventory(type);
			int possesed = iv.countById(id);
			if (possesed > 0) {
				MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(id), id, possesed, true, false);
				chr.announce(MaplePacketCreator.getShowItemGain(id, (short) -possesed, true));
			}
		}
	}

	public void removeAll(int id) {
		removeAll(id, c);
	}

	public void removeAll(int id, MapleClient cl) {
		MapleInventoryType invType = ItemConstants.getInventoryType(id);
		int possessed = cl.getPlayer().getInventory(invType).countById(id);
		if (possessed > 0) {
			MapleInventoryManipulator.removeById(cl, ItemConstants.getInventoryType(id), id, possessed, true, false);
			cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
		}
		
		if(invType == MapleInventoryType.EQUIP) {
			if(cl.getPlayer().getInventory(MapleInventoryType.EQUIPPED).countById(id) > 0) {
				MapleInventoryManipulator.removeById(cl, MapleInventoryType.EQUIPPED, id, 1, true, false);
				cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -1, true));
			}
		}
	}

	public int getMapId() {
		return c.getPlayer().getMap().getId();
	}

	public int getPlayerCount(int mapid) {
		return c.getChannelServer().getMapFactory().getMap(mapid).getCharacters().size();
	}

	public void showInstruction(String msg, int width, int height) {
		c.announce(MaplePacketCreator.sendHint(msg, width, height));
		c.announce(MaplePacketCreator.enableActions());
	}

	public void disableMinimap() {
		c.announce(MaplePacketCreator.disableMinimap());
	}

        public boolean isAllReactorState(final int reactorId, final int state) {
                return c.getPlayer().getMap().isAllReactorState(reactorId, state);
        }
        
	public void resetMap(int mapid) {
		getMap(mapid).resetReactors();
		getMap(mapid).killAllMonsters();
		for (MapleMapObject i : getMap(mapid).getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
			getMap(mapid).removeMapObject(i);
			getMap(mapid).broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, c.getPlayer().getId()));
		}
	}

	public void useItem(int id) {
		MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(c.getPlayer());
		c.announce(MaplePacketCreator.getItemMessage(id));//Useful shet :3
	}

	public void cancelItem(final int id) {
		getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id), false, -1);
	}  

	public void teachSkill(int skillid, byte level, byte masterLevel, long expiration) {
		getPlayer().changeSkillLevel(SkillFactory.getSkill(skillid), level, masterLevel, expiration);
	}

	public void removeEquipFromSlot(short slot) {
		Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), false, false);
	}

	public void gainAndEquip(int itemid, short slot) {
		final Item old = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		if (old != null) {
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIPPED, slot, old.getQuantity(), false, false);
		}
		final Item newItem = MapleItemInformationProvider.getInstance().getEquipById(itemid);
		newItem.setPosition(slot);
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addItemFromDB(newItem);
		c.announce(MaplePacketCreator.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, newItem))));
	}
        
        public static void spawnNpc(int npcId, Point pos, MapleMap map) {
                MapleNPC npc = MapleLifeFactory.getNPC(npcId);
                if (npc != null) {
                        npc.setPosition(pos);
                        npc.setCy(pos.y);
                        npc.setRx0(pos.x + 50);
                        npc.setRx1(pos.x - 50);
                        npc.setFh(map.getFootholds().findBelow(pos).getId());
                        map.addMapObject(npc);
                        map.broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                }
        }
        
	public void spawnMonster(int id, int x, int y) {
		MapleMonster monster = MapleLifeFactory.getMonster(id);
		monster.setPosition(new Point(x, y));
		getPlayer().getMap().spawnMonster(monster);
	}
        
	public static MapleMonster getMonsterLifeFactory(int mid) {
		return MapleLifeFactory.getMonster(mid);
	}
        
        public static MobSkill getMobSkill(int skill, int level) {
		return MobSkillFactory.getMobSkill(skill, level);
	}

	public void spawnGuide() {
		c.announce(MaplePacketCreator.spawnGuide(true));
	}

	public void removeGuide() {
		c.announce(MaplePacketCreator.spawnGuide(false));
	}

	public void displayGuide(int num) {
		c.announce(MaplePacketCreator.showInfo("UI/tutorial.img/" + num));
	}

	public void goDojoUp() {
		c.announce(MaplePacketCreator.dojoWarpUp());
	}
        
        public void resetDojoEnergy() {
                c.getPlayer().setDojoEnergy(0);
        }
        
        public void resetPartyDojoEnergy() {
                for(MapleCharacter pchr: c.getPlayer().getPartyMembersOnSameMap()) {
                        pchr.setDojoEnergy(0);
                }
        }

	public void enableActions() {
		c.announce(MaplePacketCreator.enableActions());
	}

	public void showEffect(String effect){
		c.announce(MaplePacketCreator.showEffect(effect));
	}

	public void dojoEnergy() {
		c.announce(MaplePacketCreator.getEnergy("energy", getPlayer().getDojoEnergy()));
	}

	public void talkGuide(String message) {
		c.announce(MaplePacketCreator.talkGuide(message));
	}

	public void guideHint(int hint) {
		c.announce(MaplePacketCreator.guideHint(hint));
	}

	public void updateAreaInfo(Short area, String info) {
		c.getPlayer().updateAreaInfo(area, info);
		c.announce(MaplePacketCreator.enableActions());//idk, nexon does the same :P
	}

	public boolean containsAreaInfo(short area, String info) {
		return c.getPlayer().containsAreaInfo(area, info);
	}

	public void earnTitle(String msg) {
		c.announce(MaplePacketCreator.earnTitleMessage(msg));
	}

	public void showInfoText(String msg) {
		c.announce(MaplePacketCreator.showInfoText(msg));
	}

	public void openUI(byte ui) {
		c.announce(MaplePacketCreator.openUI(ui));
	}

	public void lockUI() {
		c.announce(MaplePacketCreator.disableUI(true));
		c.announce(MaplePacketCreator.lockUI(true));
	}

	public void unlockUI() {
		c.announce(MaplePacketCreator.disableUI(false));
		c.announce(MaplePacketCreator.lockUI(false));
	}

	public void playSound(String sound) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
	}

	public void environmentChange(String env, int mode) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, mode));
	}
        
        public String numberWithCommas(int number) {
                return GameConstants.numberWithCommas(number);
        }

	public Pyramid getPyramid() {
		return (Pyramid) getPlayer().getPartyQuest();
	}

	public void createExpedition(MapleExpeditionType type) {
		MapleExpedition exped = new MapleExpedition(getPlayer(), type);
		getPlayer().getClient().getChannelServer().getExpeditions().add(exped);
	}

	public void endExpedition(MapleExpedition exped) {
		exped.dispose(true);
		getPlayer().getClient().getChannelServer().getExpeditions().remove(exped);
	}

	public MapleExpedition getExpedition(MapleExpeditionType type) {
		for (MapleExpedition exped : getPlayer().getClient().getChannelServer().getExpeditions()) {
			if (exped.getType().equals(type)) {
				return exped;
			}
		}
		return null;
	}
        
        public long getJailTimeLeft() {
                return getPlayer().getJailExpirationTimeLeft();
        }
        
        public List<MaplePet> getDriedPets() {
                List<MaplePet> list = new LinkedList<>();
            
                long curTime = System.currentTimeMillis();
                for(Item it : getPlayer().getInventory(MapleInventoryType.CASH).list()) {
                        if(ItemConstants.isPet(it.getItemId()) && it.getExpiration() < curTime) {
                                MaplePet pet = it.getPet();
                                if (pet != null) {
                                        list.add(pet);
                                }
                        }
                }
                
                return list;
        }
        
        public boolean startDungeonInstance(int dungeonid) {
                return c.getChannelServer().addMiniDungeon(dungeonid);
        }
        
        public boolean canGetFirstJob(int jobType) {
                if (ServerConstants.USE_AUTOASSIGN_STARTERS_AP) {
                        return true;
                }
                
                MapleCharacter chr = this.getPlayer();
                
                switch(jobType) {
                    case 1:
                        return chr.getStr() >= 35;
                        
                    case 2:
                        return chr.getInt() >= 20;
                        
                    case 3:
                    case 4:
                        return chr.getDex() >= 25;
                        
                    case 5:
                        return chr.getDex() >= 20;
                        
                    default:
                        return true;
                }
        }
        
        public static String getFirstJobStatRequirement(int jobType) {
                switch(jobType) {
                    case 1:
                        return "STR " + 35;
                        
                    case 2:
                        return "INT " + 20;
                        
                    case 3:
                    case 4:
                        return "DEX " + 25;
                        
                    case 5:
                        return "DEX " + 20;
                }
                
                return null;
        }
}
