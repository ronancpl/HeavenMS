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
package maplequestitemcountfetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author RonanLana
 
 This application parses the Quest.wz file inputted and generates a report showing
 all cases where a quest requires an item, but doesn't take them, which may happen
 because the node representing the item doesn't have a "count" clause.
 
 Running it should generate a report file under "lib" folder with the search results.
 
 */
public class MapleQuestItemCountFetcher {
    static String actName = "../../wz/Quest.wz/Act.img.xml";
    static String checkName = "../../wz/Quest.wz/Check.img.xml";
    static String newFile = "lib/QuestReport.txt";
    
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    
    static byte status = 0;
    static int questId = -1;
    static int isCompleteState = 0;
    
    static int curItemId;
    static int curItemCount;
    
    static Map<Integer, Map<Integer, Integer>> checkItems = new HashMap<>();
    static Map<Integer, Map<Integer, Integer>> actItems = new HashMap<>();
    
    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

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
    
    private static void readItemLabel(String token) {
        String name = getName(token);
        String value = getValue(token);

        switch(name) {
            case "id":
                curItemId = Integer.parseInt(value);
                break;

            case "count":
                curItemCount = Integer.parseInt(value);
                break;
        }
    }
    
    private static void commitQuestItemPair(Map<Integer, Map<Integer, Integer>> map) {
        Map<Integer, Integer> list = map.get(questId);
        if(list == null) {
            list = new LinkedHashMap<>();
            map.put(questId, list);
        }
        
        list.put(curItemId, curItemCount);
    }
    
    private static void translateTokenAct(String token) {
        String d;
        
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if(status == 4) {
                if(curItemCount == Integer.MAX_VALUE && isCompleteState == 1) {
                    commitQuestItemPair(actItems);
                }
            }
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
                if(!token.contains("item")) {
                    forwardCursor(status);
                }
            }
            else if(status == 4) {
                curItemId = Integer.MAX_VALUE;
                curItemCount = Integer.MAX_VALUE;
            }

            status += 1;
        }
        else {
            if(status == 5) {
                readItemLabel(token);
            }
        }
    }
    
    private static void translateTokenCheck(String token) {
        String d;
        
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if(status == 4) {
                Map<Integer, Integer> missedItems = actItems.get(questId);
                
                if(missedItems != null && missedItems.containsKey(curItemId) && isCompleteState == 1) {
                    commitQuestItemPair(checkItems);
                }
            }
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
                if(!token.contains("item")) {
                    forwardCursor(status);
                }
            }
            else if(status == 4) {
                curItemId = Integer.MAX_VALUE;
                curItemCount = Integer.MAX_VALUE;
            }

            status += 1;
        }
        else {
            if(status == 5) {
                readItemLabel(token);
            }
        }
    }

    private static void readQuestItemCountData() throws IOException {
        String line;
        
        fileReader = new InputStreamReader(new FileInputStream(actName), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            translateTokenAct(line);
        }

        bufferedReader.close();
        fileReader.close();
        
        fileReader = new InputStreamReader(new FileInputStream(checkName), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            translateTokenCheck(line);
        }

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleQuestItemCountFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void printReportFileResults() {
        List<Pair<Integer, Pair<Integer, Integer>>> reports = new ArrayList<>();
        List<Pair<Integer, Integer>> notChecked = new ArrayList<>();
        
        for(Entry<Integer, Map<Integer, Integer>> actItem : actItems.entrySet()) {
            int questid = actItem.getKey();
            
            for(Entry<Integer, Integer> actData : actItem.getValue().entrySet()) {
                int itemid = actData.getKey();
                
                Map<Integer, Integer> checkData = checkItems.get(questid);
                if(checkData != null) {
                    Integer count = checkData.get(itemid);
                    if(count != null) {
                        reports.add(new Pair<>(questid, new Pair<>(itemid, -count)));
                    }
                } else {
                    notChecked.add(new Pair<>(questid, itemid));
                }
            }
        }
        
        for(Pair<Integer, Pair<Integer, Integer>> r : reports) {
            printWriter.println("Questid " + r.left + " : Itemid " + r.right.left + " should have qty " + r.right.right);
        }
        
        for(Pair<Integer, Integer> r : notChecked) {
            printWriter.println("Questid " + r.left + " : Itemid " + r.right + " is unchecked");
        }
    }
    
    private static void ReportQuestItemCountData() {
        // This will reference one line at a time
        
        try {
            System.out.println("Reading WZs...");
            readQuestItemCountData();
            
            System.out.println("Reporting results...");
            printWriter = new PrintWriter(newFile, "UTF-8");
            
            printReportFileHeader();
            printReportFileResults();
            
            printWriter.close();
            System.out.println("Done!");
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open quest file.");
        }
        catch(IOException ex) {
            System.out.println("Error reading quest file.");
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        ReportQuestItemCountData();
    }
    
}
