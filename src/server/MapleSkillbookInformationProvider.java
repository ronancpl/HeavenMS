/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import client.MapleCharacter;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.DatabaseConnection;

/**
 *
 * @author RonanLana
 */
public class MapleSkillbookInformationProvider {
    
    private final static MapleSkillbookInformationProvider instance = new MapleSkillbookInformationProvider();
    
    public static MapleSkillbookInformationProvider getInstance() {
        return instance;
    }
    
    protected static Map<Integer, SkillBookEntry> foundSkillbooks = new HashMap<>();
    
    public enum SkillBookEntry {
        UNAVAILABLE,
        QUEST,
        QUEST_BOOK,
        QUEST_REWARD,
        REACTOR,
        SCRIPT
    }
    
    private static String rootDirectory = ".";
    
    private static int skillbookMinItemid = 2280000;
    private static int skillbookMaxItemid = 2300000;  // exclusively
    
    static {
        loadSkillbooks();
    }
    
    private static boolean is4thJobSkill(int itemid) {
        return itemid / 10000 % 10 == 2;
    }
    
    private static boolean isSkillBook(int itemid) {
        return itemid >= skillbookMinItemid && itemid < skillbookMaxItemid;
    }
    
    private static boolean isQuestBook(int itemid) {
        return itemid >= 4001107 && itemid <= 4001114 || itemid >= 4161015 && itemid <= 4161023;
    }
    
    private static int fetchQuestbook(MapleData checkData, String quest) {
        MapleData questStartData = checkData.getChildByPath(quest).getChildByPath("0");
        
        MapleData startReqItemData = questStartData.getChildByPath("item");
        if (startReqItemData != null) {
            for (MapleData itemData : startReqItemData.getChildren()) {
                int itemid = MapleDataTool.getInt("id", itemData, 0);
                if (isQuestBook(itemid)) {
                    return itemid;
                }
            }
        }
            
        MapleData startReqQuestData = questStartData.getChildByPath("quest");
        if (startReqQuestData != null) {
            Set<Integer> reqQuests = new HashSet<>();
            
            for (MapleData questStatusData : startReqQuestData.getChildren()) {
                int reqQuest = MapleDataTool.getInt("id", questStatusData, 0);
                if (reqQuest > 0) {
                    reqQuests.add(reqQuest);
                }
            }
            
            for (Integer reqQuest : reqQuests) {
                int book = fetchQuestbook(checkData, Integer.toString(reqQuest));
                if (book > -1) {
                    return book;
                }
            }
        }
        
        return -1;
    }
    
    private static void fetchSkillbooksFromQuests() {
        MapleDataProvider questDataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "Quest.wz"));
        MapleData actData = questDataProvider.getData("Act.img");
        MapleData checkData = questDataProvider.getData("Check.img");
        
        for (MapleData questData : actData.getChildren()) {
            for (MapleData questStatusData : questData.getChildren()) {
                for (MapleData questNodeData : questStatusData.getChildren()) {
                    String actNodeName = questNodeData.getName();
                    if (actNodeName.contentEquals("item")) {
                        for (MapleData questItemData : questNodeData.getChildren()) {
                            int itemid = MapleDataTool.getInt("id", questItemData, 0);
                            int itemcount = MapleDataTool.getInt("count", questItemData, 0);
                            
                            if (isSkillBook(itemid) && itemcount > 0) {
                                int questbook = fetchQuestbook(checkData, questData.getName());
                                if (questbook < 0) {
                                    foundSkillbooks.put(itemid, SkillBookEntry.QUEST);
                                } else {
                                    foundSkillbooks.put(itemid, SkillBookEntry.QUEST_BOOK);
                                }
                            }
                        }
                    } else if (actNodeName.contentEquals("skill")) {
                        for (MapleData questSkillData : questNodeData.getChildren()) {
                            int skillid = MapleDataTool.getInt("id", questSkillData, 0);
                            if (is4thJobSkill(skillid)) {
                                // negative itemids are skill rewards
                                
                                int questbook = fetchQuestbook(checkData, questData.getName());
                                if (questbook < 0) {
                                    foundSkillbooks.put(-skillid, SkillBookEntry.QUEST_REWARD);
                                } else {
                                    foundSkillbooks.put(-skillid, SkillBookEntry.QUEST_BOOK);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void fetchSkillbooksFromReactors() {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("SELECT itemid FROM reactordrops WHERE itemid >= ? AND itemid < ?;");
            ps.setInt(1, skillbookMinItemid);
            ps.setInt(2, skillbookMaxItemid);
            ResultSet rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                while(rs.next()) {
                    foundSkillbooks.put(rs.getInt("itemid"), SkillBookEntry.REACTOR);
                }
            }

            rs.close();
            ps.close();
            
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    private static void listFiles(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), files);
            }
        }
    }
    
    private static List<File> listFilesFromDirectoryRecursively(String directory) {
        ArrayList<File> files = new ArrayList<>();
        listFiles(directory, files);
        
        return files;
    }
    
    private static void filterScriptDirectorySearchMatchingData(String path) {
        for (File file : listFilesFromDirectoryRecursively(rootDirectory + "/" + path)) {
            if (file.getName().endsWith(".js")) {
                fileSearchMatchingData(file);
            }
        }
    }
    
    private static Set<Integer> foundMatchingDataOnFile(String fileContent) {
        Set<Integer> matches = new HashSet<>(4);
        
        Matcher searchM = Pattern.compile("22(8|9)[0-9]{4}").matcher(fileContent);
        int idx = 0;
        while (searchM.find(idx)) {
            idx = searchM.end();
            matches.add(Integer.valueOf(fileContent.substring(searchM.start(), idx)));
        }
        
        return matches;
    }
    
    static String readFileToString(File file, String encoding) throws IOException {
        Scanner scanner = new Scanner(file, encoding);
        String text = "";
        try {
            try {
                text = scanner.useDelimiter("\\A").next();
            } finally {
                scanner.close();
            }
        } catch (NoSuchElementException e) {}
        
        return text;
    }
    
    private static void fileSearchMatchingData(File file) {
        try {
            String fileContent = readFileToString(file, "UTF-8");
            
            Set<Integer> books = foundMatchingDataOnFile(fileContent);
            for (Integer i : books) {
                foundSkillbooks.put(i, SkillBookEntry.SCRIPT);
            }
        } catch (IOException ioe) {
            System.out.println("Failed to read " + file.getName() + ".");
            ioe.printStackTrace();
        }
    }
    
    private static void fetchSkillbooksFromScripts() {
        filterScriptDirectorySearchMatchingData("scripts");
    }
    
    private static void loadSkillbooks() {
        fetchSkillbooksFromQuests();
        fetchSkillbooksFromReactors();
        fetchSkillbooksFromScripts();
    }
    
    public SkillBookEntry getSkillbookAvailability(int itemid) {
        SkillBookEntry sbe = foundSkillbooks.get(itemid);
        return sbe != null ? sbe : SkillBookEntry.UNAVAILABLE;
    }
    
    public List<Integer> getTeachableSkills(MapleCharacter chr) {
        List<Integer> list = new ArrayList<>();
        
        for (Integer book : foundSkillbooks.keySet()) {
            if (book >= 0) {
                continue;
            }
            
            int skillid = -book;
            if (skillid / 10000 == chr.getJob().getId()) {
                if (chr.getMasterLevel(skillid) == 0) {
                    list.add(-skillid);
                }
            }
        }
        
        return list;
    }
    
}
