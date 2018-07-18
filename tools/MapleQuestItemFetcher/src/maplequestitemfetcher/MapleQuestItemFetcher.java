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
package maplequestitemfetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import java.io.File;
import tools.MapleItemInformationProvider;

import tools.Pair;

/**
 *
 * @author RonanLana
 * 
 * This application haves 2 objectives: fetch missing drop data relevant to quests,
 * and update the questid from items that are labeled as "Quest Item" on the DB.
 * 
 * To test a server instance with this feature, MapleQuestItemFetcher must be set
 * just like it is displayed on the HeavenMS source: 2 folders ahead
 * of the root of the main source.
 * 
 * Running it should generate a report file under "lib" folder with the search results.
 * 
 * Estimated parse time: 1 minute
 */
public class MapleQuestItemFetcher {
    static MapleItemInformationProvider ii;
    
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String wzPath = "../../wz";
    static String directoryName = "../..";
    static String newFile = "lib/QuestReport.txt";

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    static boolean displayExtraInfo = true;     // display items with zero quantity over the quest act WZ
    
    static Map<Integer, Set<Integer>> startQuestItems = new HashMap<>(initialLength);
    static Map<Integer, Set<Integer>> completeQuestItems = new HashMap<>(initialLength);
    
    static Map<Integer, Set<Integer>> zeroedStartQuestItems = new HashMap<>();
    static Map<Integer, Set<Integer>> zeroedCompleteQuestItems = new HashMap<>();
    static Map<Integer, int[]> mixedQuestidItems = new HashMap<>();
    static Set<Integer> limitedQuestids = new HashSet<>();
    
    static byte status = 0;
    static int questId = -1;
    static int isCompleteState = 0;
    
    static int currentItemid = 0;
    static int currentCount = 0;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;
        
        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        if(j < i) return "0";           //node value containing 'name' in it's scope, cheap fix since we don't deal with strings anyway
        
        dest = new char[initialStringLength];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return(d.trim());
    }
    
    private static String getValue(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("value");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[initialStringLength];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return(d.trim());
    }
    
    private static void forwardCursor(int st) {
        String line = null;

        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                simpleToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void simpleToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
        }
    }
    
    private static void inspectQuestItemList(int st) {
        String line = null;

        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                readItemToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCurrentItem() {
        try {
            if(ii.isQuestItem(currentItemid)) {
                if(currentCount != 0) {
                    if(isCompleteState == 1) {
                        if(currentCount < 0) {
                            Set<Integer> qi = completeQuestItems.get(questId);
                            if(qi == null) {
                                Set<Integer> newSet = new HashSet<>();
                                newSet.add(currentItemid);

                                completeQuestItems.put(questId, newSet);
                            } else {
                                qi.add(currentItemid);
                            }
                        }
                    } else {
                        if(currentCount > 0) {
                            Set<Integer> qi = startQuestItems.get(questId);
                            if(qi == null) {
                                Set<Integer> newSet = new HashSet<>();
                                newSet.add(currentItemid);

                                startQuestItems.put(questId, newSet);
                            } else {
                                qi.add(currentItemid);
                            }
                        }
                    }
                } else {
                    if(isCompleteState == 1) {
                        Set<Integer> qi = zeroedCompleteQuestItems.get(questId);
                        if(qi == null) {
                            Set<Integer> newSet = new HashSet<>();
                            newSet.add(currentItemid);

                            zeroedCompleteQuestItems.put(questId, newSet);
                        } else {
                            qi.add(currentItemid);
                        }
                    } else {
                        Set<Integer> qi = zeroedStartQuestItems.get(questId);
                        if(qi == null) {
                            Set<Integer> newSet = new HashSet<>();
                            newSet.add(currentItemid);

                            zeroedStartQuestItems.put(questId, newSet);
                        } else {
                            qi.add(currentItemid);
                        }
                    }
                }
            }
        } catch(Exception e) {}
    }
    
    private static void readItemToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            processCurrentItem();
            
            currentItemid = 0;
            currentCount = 0;
        }
        else if(token.contains("imgdir")) {
            status += 1;
        }
        else {
            String d = getName(token);
            
            if(d.equals("id")) {
                currentItemid = Integer.parseInt(getValue(token));
            } else if(d.equals("count")) {
                currentCount = Integer.parseInt(getValue(token));
            }
        }
    }

    private static void translateActToken(String token) {
        String d;
        int temp;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting QuestId
                d = getName(token);
                questId = Integer.parseInt(d);
            }
            else if(status == 2) {      //start/complete
                d = getName(token);
                isCompleteState = Integer.parseInt(d);
            }
            else if(status == 3) {
                d = getName(token);

                if(d.contains("item")) {
                    temp = status;
                    inspectQuestItemList(temp);
                } else {
                    forwardCursor(status);
                }
            }

            status += 1;
        } else {
            if(status == 3) {
                d = getName(token);

                if(d.equals("end")) {
                    limitedQuestids.add(questId);
                }
            }
        }
    }
    
    private static void translateCheckToken(String token) {
        String d;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting QuestId
                d = getName(token);
                questId = Integer.parseInt(d);
            }
            else if(status == 2) {      //start/complete
                d = getName(token);
                isCompleteState = Integer.parseInt(d);
            }
            else if(status == 3) {
                forwardCursor(status);
            }

            status += 1;
        } else {
            if(status == 3) {
                d = getName(token);

                if(d.equals("end")) {
                    limitedQuestids.add(questId);
                }
            }
        }
    }

    private static void calculateQuestItemDiff() {
        // This will remove started quest items from the "to complete" item set.
        
        for(Entry<Integer, Set<Integer>> qd : startQuestItems.entrySet()) {
            for(Integer qi : qd.getValue()) {
                Set<Integer> questSet = completeQuestItems.get(qd.getKey());
                
                if(questSet != null) {
                    if(questSet.remove(qi)) {
                        if(completeQuestItems.isEmpty()) {
                            completeQuestItems.remove(qd.getKey());
                        }
                    }
                }
            }
        }
    }
    
    private static List<Pair<Integer, Integer>> getPairsQuestItem() {   // quest items not gained at WZ's quest start
        List<Pair<Integer, Integer>> list = new ArrayList<>(initialLength);
        
        for(Entry<Integer, Set<Integer>> qd : completeQuestItems.entrySet()) {
            for(Integer qi : qd.getValue()) {
                list.add(new Pair<>(qi, qd.getKey()));
            }
        }
        
        return list;
    }
    
    private static String getTableName(boolean dropdata) {
        return dropdata ? "drop_data" : "reactordrops";
    }
    
    private static void filterQuestDropsOnTable(Pair<Integer, Integer> iq, List<Pair<Integer, Integer>> itemsWithQuest, boolean dropdata) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT questid FROM " + getTableName(dropdata) + " WHERE itemid = ?;");
        ps.setInt(1, iq.getLeft());
        ResultSet rs = ps.executeQuery();

        if (rs.isBeforeFirst()) {
            while(rs.next()) {
                int curQuest = rs.getInt(1);
                if(curQuest != iq.getRight()) {
                    Set<Integer> sqSet = startQuestItems.get(curQuest);
                    if(sqSet != null && sqSet.contains(iq.getLeft())) {
                        continue;
                    }
                    
                    int[] mixed = new int[3];
                    mixed[0] = iq.getLeft();
                    mixed[1] = curQuest;
                    mixed[2] = iq.getRight();

                    mixedQuestidItems.put(iq.getLeft(), mixed);
                }
            }

            itemsWithQuest.remove(iq);
        }

        rs.close();
        ps.close();
    }
    
    private static void filterQuestDropsOnDB(List<Pair<Integer, Integer>> itemsWithQuest) throws SQLException {
        List<Pair<Integer, Integer>> copyItemsWithQuest = new ArrayList<>(itemsWithQuest);
        try {
            for(Pair<Integer, Integer> iq : copyItemsWithQuest) {
                filterQuestDropsOnTable(iq, itemsWithQuest, true);
                filterQuestDropsOnTable(iq, itemsWithQuest, false);
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void filterDirectorySearchMatchingData(String path, List<Pair<Integer, Integer>> itemsWithQuest) {
        Iterator iter = FileUtils.iterateFiles(new File(directoryName + "/" + path), new String[]{"sql", "js", "txt","java"}, true);

        while(iter.hasNext()) {
            File file = (File) iter.next();
            fileSearchMatchingData(file, itemsWithQuest);
        }
    }
    
    private static boolean foundMatchingDataOnFile(String fileContent, String searchStr) {
        return fileContent.contains(searchStr);
    }
    
    private static void fileSearchMatchingData(File file, List<Pair<Integer, Integer>> itemsWithQuest) {
        try {
            String fileContent = FileUtils.readFileToString(file, "UTF-8");
            
            List<Pair<Integer, Integer>> copyItemsWithQuest = new ArrayList<>(itemsWithQuest);
            for(Pair<Integer, Integer> iq : copyItemsWithQuest) {
                if(foundMatchingDataOnFile(fileContent, String.valueOf(iq.getLeft()))) {
                    itemsWithQuest.remove(iq);
                }
            }
        } catch(IOException ioe) {
            System.out.println("Failed to read file: " + file.getAbsolutePath());
            ioe.printStackTrace();
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleQuestItemFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the underlying DB, server source files and the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static List<Entry<Integer, Integer>> getSortedMapEntries0(Map<Integer, Integer> map) {
        List<Entry<Integer, Integer>> list = new ArrayList<>(map.size());
        for(Entry<Integer, Integer> e : map.entrySet()) {
            list.add(e);
        }
        
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
            @Override
            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        
        return list;
    }
    
    private static List<Entry<Integer, int[]>> getSortedMapEntries1(Map<Integer, int[]> map) {
        List<Entry<Integer, int[]>> list = new ArrayList<>(map.size());
        for(Entry<Integer, int[]> e : map.entrySet()) {
            list.add(e);
        }
        
        Collections.sort(list, new Comparator<Entry<Integer, int[]>>() {
            @Override
            public int compare(Entry<Integer, int[]> o1, Entry<Integer, int[]> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        
        return list;
    }
    
    private static List<Pair<Integer, List<Integer>>> getSortedMapEntries2(Map<Integer, Set<Integer>> map) {
        List<Pair<Integer, List<Integer>>> list = new ArrayList<>(map.size());
        for(Entry<Integer, Set<Integer>> e : map.entrySet()) {
            List<Integer> il = new ArrayList<>(2);
            for(Integer i : e.getValue()) {
                il.add(i);
            }
            
            Collections.sort(il, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            
            list.add(new Pair<>(e.getKey(), il));
        }
        
        Collections.sort(list, new Comparator<Pair<Integer, List<Integer>>>() {
            @Override
            public int compare(Pair<Integer, List<Integer>> o1, Pair<Integer, List<Integer>> o2) {
                return o1.getLeft() - o2.getLeft();
            }
        });
        
        return list;
    }
    
    private static String getExpiredStringLabel(int questid) {
        return (!limitedQuestids.contains(questid) ? "" : " EXPIRED");
    }
    
    private static void ReportQuestItemData() {
        // This will reference one line at a time
        String line = null;
        String fileName = null;

        try {
            Class.forName(driver).newInstance();
            
            System.out.println("Reading WZs...");
    
            fileName = wzPath + "/Quest.wz/Check.img.xml";
            fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                translateCheckToken(line);  // fetch expired quests through here as well
            }
            
            bufferedReader.close();
            fileReader.close();
            
            fileName = wzPath + "/Quest.wz/Act.img.xml";
            fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                translateActToken(line);
            }
            
            bufferedReader.close();
            fileReader.close();
            
            System.out.println("Calculating table diffs...");
            calculateQuestItemDiff();
            
            System.out.println("Filtering drops on DB...");
            List<Pair<Integer, Integer>> itemsWithQuest = getPairsQuestItem();
            
            // filter drop data on DB
            con = DriverManager.getConnection(host, username, password);
            filterQuestDropsOnDB(itemsWithQuest);
            con.close();
            
            System.out.println("Filtering drops on project files...");
            // finally, filter whether this item is mentioned on the source code or not.
            filterDirectorySearchMatchingData("scripts", itemsWithQuest);
            filterDirectorySearchMatchingData("sql", itemsWithQuest);
            filterDirectorySearchMatchingData("src", itemsWithQuest);
            
            System.out.println("Reporting results...");
            // report suspects of missing quest drop data, as well as those drop data that may have incorrect questids.
            printWriter = new PrintWriter(newFile, "UTF-8");
            
            printReportFileHeader();
            
            if(!mixedQuestidItems.isEmpty()) {
                printWriter.println("INCORRECT QUESTIDS ON DB");
                for(Entry<Integer, int[]> emqi : getSortedMapEntries1(mixedQuestidItems)) {
                    int[] mqi = emqi.getValue();
                    printWriter.println(mqi[0] + " : " + mqi[1] + " -> " + mqi[2] + getExpiredStringLabel(mqi[2]));
                }
                printWriter.println("\n\n\n\n\n");
            }
            
            if(!itemsWithQuest.isEmpty()) {
                Map<Integer, Integer> mapIwq = new HashMap<>(itemsWithQuest.size());
                for(Pair<Integer, Integer> iwq : itemsWithQuest) {
                    mapIwq.put(iwq.getLeft(), iwq.getRight());
                }
                
                printWriter.println("ITEMS WITH NO QUEST DROP DATA ON DB");
                for(Entry<Integer, Integer> iwq : getSortedMapEntries0(mapIwq)) {
                    printWriter.println(iwq.getKey() + " - " + iwq.getValue() + getExpiredStringLabel(iwq.getValue()));
                }
                printWriter.println("\n\n\n\n\n");
            }
            
            if(displayExtraInfo) {
                if(!zeroedStartQuestItems.isEmpty()) {
                    printWriter.println("START QUEST ITEMS WITH ZERO QUANTITY");
                    for(Pair<Integer, List<Integer>> iwq : getSortedMapEntries2(zeroedStartQuestItems)) {
                        printWriter.println(iwq.getLeft() + getExpiredStringLabel(iwq.getLeft()) + ":");
                        for(Integer i : iwq.getRight()) {
                            printWriter.println("  " + i);
                        }
                        printWriter.println();
                    }
                    printWriter.println("\n\n\n\n\n");
                }

                if(!zeroedCompleteQuestItems.isEmpty()) {
                    printWriter.println("COMPLETE QUEST ITEMS WITH ZERO QUANTITY");
                    for(Pair<Integer, List<Integer>> iwq : getSortedMapEntries2(zeroedCompleteQuestItems)) {
                        printWriter.println(iwq.getLeft() + getExpiredStringLabel(iwq.getLeft()) + ":");
                        for(Integer i : iwq.getRight()) {
                            printWriter.println("  " + i);
                        }
                        printWriter.println();
                    }
                    printWriter.println("\n\n\n\n\n");
                }
            }

            printWriter.close();
            System.out.println("Done!");
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }

        catch(SQLException e) {
            System.out.println("Warning: Could not establish connection to database to report quest data.");
            System.out.println(e.getMessage());
        }

        catch(ClassNotFoundException e) {
            System.out.println("Error: could not find class");
            System.out.println(e.getMessage());
        }

        catch(InstantiationException e) {
            System.out.println("Error: instantiation failure");
            System.out.println(e.getMessage());
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.setProperty("wzpath", wzPath);
        ii = MapleItemInformationProvider.getInstance();
        
        ReportQuestItemData();
    }
    
}
