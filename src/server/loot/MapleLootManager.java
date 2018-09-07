/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package server.loot;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;

import server.MapleItemInformationProvider;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.quest.MapleQuest;

/**
 *
 * @author Ronan
 */
public class MapleLootManager {
    
    private static boolean isRelevantDrop(MonsterDropEntry dropEntry, List<MapleCharacter> partyMembers, List<MapleLootInventory> partyInv) {
        MapleQuest quest = MapleQuest.getInstance(dropEntry.questid);
        int qItemAmount = quest != null ? quest.getItemAmountNeeded(dropEntry.itemId) : 0;
        
        boolean restricted = MapleItemInformationProvider.getInstance().isLootRestricted(dropEntry.itemId);
        for (int i = 0; i < partyMembers.size(); i++) {
            MapleLootInventory chrInv = partyInv.get(i);
            
            if (dropEntry.questid > 0) {
                if (partyMembers.get(i).getQuestStatus(dropEntry.questid) != 1) {
                    continue;
                }
                
                int qItemStatus = chrInv.hasItem(dropEntry.itemId, qItemAmount);
                if (qItemStatus == 2) {
                    continue;
                } else if (restricted && qItemStatus == 1) {
                    continue;
                }
            } else if (restricted && chrInv.hasItem(dropEntry.itemId, 1) > 0) {
                continue;
            }
            
            return true;
        }
        
        return false;
    }
    
    public static List<MonsterDropEntry> retrieveRelevantDrops(int monsterId, List<MapleCharacter> partyMembers) {
        List<MonsterDropEntry> loots = MapleMonsterInformationProvider.getInstance().retrieveEffectiveDrop(monsterId);
        if(loots.isEmpty()) return loots;
        
        List<MapleLootInventory> partyInv = new LinkedList<>();
        for(MapleCharacter chr : partyMembers) {
            MapleLootInventory lootInv = new MapleLootInventory(chr);
            partyInv.add(lootInv);
        }
        
        List<MonsterDropEntry> effectiveLoot = new LinkedList<>();
        for(MonsterDropEntry mde : loots) {
            if(isRelevantDrop(mde, partyMembers, partyInv)) {
                effectiveLoot.add(mde);
            }
        }
        
        return effectiveLoot;
    }
    
}
