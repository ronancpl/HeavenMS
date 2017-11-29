/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dropspider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import tools.Pair;

/**
 *
 * @author Simon
 */
public class DataTool {
    private static Map<String, Integer> hardcodedMobs = new HashMap<>();
    
    private static ArrayList<Pair<Integer, String>> npc_list = null;
    private static LinkedList<Pair<Integer, String>> mob_pairs = null;
    private static MapleDataProvider data = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Mob.wz"));
    private static HashSet<Integer> bosses = null;

    public static void setHardcodedMobNames() {
        hardcodedMobs.put("Red Slime [2]", 7120103);
        hardcodedMobs.put("Gold Slime", 7120105);
        hardcodedMobs.put("Nibelung [3]", 8220015);
    }
    
    public static void addMonsterIdsFromHardcodedName(List<Integer> monster_ids, String monster_name) {
        Integer id = hardcodedMobs.get(monster_name);
        if(id != null) {
            monster_ids.add(id);
        }
    }
    
    public static ArrayList<Integer> monsterIdsFromName(String name) {
        MapleData data = null;
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz"));
        ArrayList<Integer> ret = new ArrayList<>();
        data = dataProvider.getData("Mob.img");
        if (mob_pairs == null) {
            mob_pairs = new LinkedList<>();
            for (MapleData mobIdData : data.getChildren()) {
                int mobIdFromData = Integer.parseInt(mobIdData.getName());
                String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                mob_pairs.add(new Pair<>(mobIdFromData, mobNameFromData));
            }
        }
        for (Pair<Integer, String> mobPair : mob_pairs) {
            if (mobPair.getRight().toLowerCase().equals(name.toLowerCase())) {
                ret.add(mobPair.getLeft());
            }
        }
        return ret;
    }

    private static void populateBossList() {
        bosses = new HashSet<>();
        MapleDataDirectoryEntry mob_data = data.getRoot();
        for (MapleDataFileEntry mdfe : mob_data.getFiles()) {
            MapleData boss_candidate = data.getData(mdfe.getName());
            MapleData monsterInfoData = boss_candidate.getChildByPath("info");
            int mid = Integer.valueOf(boss_candidate.getName().replaceAll("[^0-9]", ""));
            boolean boss = MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0 || mid == 8810018 || mid == 9410066;
            if (boss) {
                bosses.add(mid);
            }
        }
    }

    public static boolean isBoss(int mid) {
        if (bosses == null) {
            populateBossList();
        }
        return bosses.contains(mid);
    }

    public static ArrayList<Integer> itemIdsFromName(String name) {

        ArrayList<Integer> ret = new ArrayList<>();
        for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
            String item_name = itemPair.getRight().toLowerCase().replaceAll("\\&quot;", "");
            item_name = item_name.replaceAll("'", "");
            item_name = item_name.replaceAll("\\'", "");

            name = name.toLowerCase().replaceAll("\\&quot;", "");
            name = name.replaceAll("'", "");
            name = name.replaceAll("\\'", "");

            if (item_name.equals(name)) {
                ret.add(itemPair.getLeft());
                return ret;
            }
        }
        return ret;
    }

    public static ArrayList<Integer> npcIdsFromName(String name) {
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz"));
        ArrayList<Integer> ret = new ArrayList<>();
        if (npc_list == null) {
            ArrayList<Pair<Integer, String>> searchList = new ArrayList<>();
            for (MapleData searchData : dataProvider.getData("Npc.img").getChildren()) {
                int searchFromData = Integer.parseInt(searchData.getName());
                String infoFromData = MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                searchList.add(new Pair<>(searchFromData, infoFromData));
            }
            npc_list = searchList;
        }
        for (Pair<Integer, String> searched : npc_list) {
            if (searched.getRight().toLowerCase().contains(name.toLowerCase())) {
                ret.add(searched.getLeft());
            }
        }
        return ret;
    }
}
