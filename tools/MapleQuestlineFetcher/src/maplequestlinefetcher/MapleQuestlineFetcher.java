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
package maplequestlinefetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author RonanLana
 
 This application parses the Quest.wz file inputted and generates a report showing
 all cases where quest script files have not been found for quests that requires a
 script file.
 As an extension, it highlights missing script files for questlines that hand over
 skills as rewards.
 
 Running it should generate a report file under "lib" folder with the search results.
 
 */
public class MapleQuestlineFetcher {
    static String actName = "../../wz/Quest.wz/Act.img.xml";
    static String checkName = "../../wz/Quest.wz/Check.img.xml";
    static String directoryName = "../..";
    static String newFile = "lib/QuestReport.txt";

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    
    static byte status = 0;
    static int questId = -1;
    static int isCompleteState = 0;
    static boolean isScriptedQuest;
    static boolean isExpiredQuest;
    static List<Integer> questDependencyList;
    
    static int curQuestId;
    static int curQuestState;
    
    static Stack<Integer> skillObtainableQuests = new Stack<>();
    static Set<Integer> scriptedQuestFiles = new HashSet<>();
    static Set<Integer> expiredQuests = new HashSet<>();
    
    static Map<Integer, List<Integer>> questDependencies = new HashMap<>();
    
    static Set<Integer> nonScriptedQuests = new HashSet<>();
    static Set<Integer> skillObtainableNonScriptedQuests = new HashSet<>();
    
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
    
    private static void translateTokenCheck(String token) {
        String d;
        
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if(status == 1) {
                evaluateCurrentQuest();
            }
            else if(status == 4) {
                evaluateCurrentQuestDependency();
            }
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting QuestId
                d = getName(token);
                questId = Integer.parseInt(d);
                
                isScriptedQuest = false;
                isExpiredQuest = false;
                questDependencyList = new LinkedList<>();
            }
            else if(status == 2) {      //start/complete
                d = getName(token);
                isCompleteState = Integer.parseInt(d);
            }
            else if(status == 3) {
                if(isCompleteState == 1 || !token.contains("quest")) {
                    forwardCursor(status);
                }
            }
            
            status += 1;
        }
        else {
            if(status == 3) {
                d = getName(token);
                
                if(d.contains("script")) {
                    isScriptedQuest = true;
                } else if(d.contains("end")) {
                    isExpiredQuest = true;
                }
            }
            else if(status == 5) {
                readQuestLabel(token);
            }
        }
    }
    
    private static void translateTokenAct(String token) {
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
                if(isCompleteState == 1 && token.contains("skill")) {
                    skillObtainableQuests.add(questId);
                }
                
                forwardCursor(status);
            }
            
            status += 1;
        }
    }

    private static void readQuestLabel(String token) {
        String name = getName(token);
        String value = getValue(token);

        switch(name) {
            case "id":
                curQuestId = Integer.parseInt(value);
                break;

            case "state":
                curQuestState = Integer.parseInt(value);
                break;
        }
    }
    
    private static void evaluateCurrentQuestDependency() {
        if(curQuestState == 2) {
            questDependencyList.add(curQuestId);
        }
    }
    
    private static void evaluateCurrentQuest() {
        if(isScriptedQuest && !scriptedQuestFiles.contains(questId)) {
            nonScriptedQuests.add(questId);
        }
        if(isExpiredQuest) {
            expiredQuests.add(questId);
        }
        
        questDependencies.put(questId, questDependencyList);
    }
    
    private static void instantiateQuestScriptFiles(String directoryName) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                String fname = file.getName();
                
                try {
                    Integer questid = Integer.parseInt(fname.substring(0, fname.indexOf('.')));
                    scriptedQuestFiles.add(questid);
                } catch(NumberFormatException nfe) {}
            }
        }
    }
    
    private static void readQuestsWithMissingScripts() throws IOException {
        String line;
        
        fileReader = new InputStreamReader(new FileInputStream(checkName), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            translateTokenCheck(line);
        }

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void readQuestsWithSkillReward() throws IOException {
        String line;
        
        fileReader = new InputStreamReader(new FileInputStream(actName), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            translateTokenAct(line);
        }

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void calculateSkillRelatedMissingQuestScripts() {
        Stack<Integer> frontierQuests = skillObtainableQuests;
        Set<Integer> solvedQuests = new HashSet<>();
        
        while(!frontierQuests.isEmpty()) {
            Integer questid = frontierQuests.pop();
            solvedQuests.add(questid);
            
            if(nonScriptedQuests.contains(questid)) {
                skillObtainableNonScriptedQuests.add(questid);
                nonScriptedQuests.remove(questid);
            }
            
            List<Integer> questDependency = questDependencies.get(questid);
            for(Integer i : questDependency) {
                if(!solvedQuests.contains(i)) {
                    frontierQuests.add(i);
                }
            }
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleQuestlineFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static List<Integer> getSortedListEntries(Set<Integer> set) {
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list);
        
        return list;
    }
    
    private static void printReportFileResults() {
        if(!skillObtainableNonScriptedQuests.isEmpty()) {
            printWriter.println("SKILL-RELATED NON-SCRIPTED QUESTS");
            for(Integer nsq : getSortedListEntries(skillObtainableNonScriptedQuests)) {
                printWriter.println("  " + nsq + (expiredQuests.contains(nsq) ? " EXPIRED" : ""));
            }
            
            printWriter.println();
        }
        
        printWriter.println("\nCOMMON NON-SCRIPTED QUESTS");
        for(Integer nsq : getSortedListEntries(nonScriptedQuests)) {
            printWriter.println("  " + nsq + (expiredQuests.contains(nsq) ? " EXPIRED" : ""));
        }
    }
    
    private static void ReportQuestlineData() {
        // This will reference one line at a time
        
        try {
            System.out.println("Reading quest scripts...");
            instantiateQuestScriptFiles(directoryName + "/scripts/quest");
            
            System.out.println("Reading WZs...");
            readQuestsWithSkillReward();
            readQuestsWithMissingScripts();
            
            System.out.println("Calculating skill related quests...");
            calculateSkillRelatedMissingQuestScripts();
            
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
    
    /*
    private static List<Pair<Integer, List<Integer>>> getSortedMapEntries(Map<Integer, List<Integer>> map) {
        List<Pair<Integer, List<Integer>>> list = new ArrayList<>(map.size());
        for(Map.Entry<Integer, List<Integer>> e : map.entrySet()) {
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
    
    private static void DumpQuestlineData() {
        for(Pair<Integer, List<Integer>> questDependency : getSortedMapEntries(questDependencies)) {
            if(!questDependency.right.isEmpty()) {
                System.out.println(questDependency);
            }
        }
    }
    */
    
    public static void main(String[] args) {
        ReportQuestlineData();
    }
    
}
