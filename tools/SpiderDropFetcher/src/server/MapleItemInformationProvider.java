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
package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

/**
 *
 * @author Matze
 *
 */
public class MapleItemInformationProvider {
    private final static MapleItemInformationProvider instance = new MapleItemInformationProvider();
    
    public static MapleItemInformationProvider getInstance() {
        return instance;
    }
    
    protected MapleDataProvider itemData;
    protected MapleDataProvider equipData;
    protected MapleDataProvider stringData;
    protected MapleDataProvider etcData;
    protected MapleData cashStringData;
    protected MapleData consumeStringData;
    protected MapleData eqpStringData;
    protected MapleData etcStringData;
    protected MapleData insStringData;
    protected MapleData petStringData;
    protected Map<Integer, Boolean> isQuestItemCache = new HashMap<>();
    protected Map<Integer, Boolean> isPartyQuestItemCache = new HashMap<>();
    protected List<Pair<Integer, String>> itemNameCache = new ArrayList<>();

    private MapleItemInformationProvider() {
        itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
        equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
        stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
        etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
        cashStringData = stringData.getData("Cash.img");
        consumeStringData = stringData.getData("Consume.img");
        eqpStringData = stringData.getData("Eqp.img");
        etcStringData = stringData.getData("Etc.img");
        insStringData = stringData.getData("Ins.img");
        petStringData = stringData.getData("Pet.img");
        
        isQuestItemCache.put(0, false);
        isPartyQuestItemCache.put(0, false);
    }

//    public MapleInventoryType getInventoryType(int itemId) {
//        if (inventoryTypeCache.containsKey(itemId)) {
//            return inventoryTypeCache.get(itemId);
//        }
//        MapleInventoryType ret;
//        String idStr = "0" + String.valueOf(itemId);
//        MapleDataDirectoryEntry root = itemData.getRoot();
//        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
//            for (MapleDataFileEntry iFile : topDir.getFiles()) {
//                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
//                    ret = MapleInventoryType.getByWZName(topDir.getName());
//                    inventoryTypeCache.put(itemId, ret);
//                    return ret;
//                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
//                    ret = MapleInventoryType.getByWZName(topDir.getName());
//                    inventoryTypeCache.put(itemId, ret);
//                    return ret;
//                }
//            }
//        }
//        root = equipData.getRoot();
//        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
//            for (MapleDataFileEntry iFile : topDir.getFiles()) {
//                if (iFile.getName().equals(idStr + ".img")) {
//                    ret = MapleInventoryType.EQUIP;
//                    inventoryTypeCache.put(itemId, ret);
//                    return ret;
//                }
//            }
//        }
//        ret = MapleInventoryType.UNDEFINED;
//        inventoryTypeCache.put(itemId, ret);
//        return ret;
//    }

    public List<Pair<Integer, String>> getAllItems() {
        if (!itemNameCache.isEmpty()) {
            return itemNameCache;
        }
        List<Pair<Integer, String>> itemPairs = new ArrayList<>();
        MapleData itemsData;
        itemsData = stringData.getData("Cash.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Consume.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
        for (MapleData eqpType : itemsData.getChildren()) {
            for (MapleData itemFolder : eqpType.getChildren()) {
                itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
            }
        }
        itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Ins.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = stringData.getData("Pet.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        return itemPairs;
    }
}
