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
package maplequestmesofetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author RonanLana
 
 This application parses the Quest.wz file inputted and generates a report showing
 all cases where a quest takes a meso fee to complete a quest, but it doesn't
 properly checks the player for the needed amount before completing it.
 
 Running it should generate a report file under "lib" folder with the search results.
 
 */
public class MapleQuestMesoFetcher {
    static boolean printFees = true;    // print missing values as additional info report
    
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
    
    static int currentMeso = 0;
    
    static Map<Integer, Integer> checkedMesoQuests = new HashMap<>();
    static Map<Integer, Integer> appliedMesoQuests = new HashMap<>();
    static Set<Integer> checkedEndscriptQuests = new HashSet<>();

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
                forwardCursor(status);
            }

            status += 1;
        }
        else {
            if(token.contains("money")) {
                if(isCompleteState != 0) {
                    d = getValue(token);
                    
                    currentMeso = -1 * Integer.valueOf(d);
                    
                    if(currentMeso > 0) {
                        appliedMesoQuests.put(questId, currentMeso);
                    }
                }
            }
        }
    }
    
    private static void translateTokenCheck(String token) {
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
        }
        else {
            if(token.contains("endmeso")) {
                d = getValue(token);
                currentMeso = Integer.valueOf(d);
                
                checkedMesoQuests.put(questId, currentMeso);
            } else if(token.contains("endscript")) {
                checkedEndscriptQuests.add(questId);
            }
        }
    }

    private static void readQuestMesoData() throws IOException {
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
        printWriter.println(" # Report File autogenerated from the MapleQuestMesoFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void printReportFileResults(Map<Integer, Integer> target, Map<Integer, Integer> base, boolean testingCheck) {
        List<Integer> result = new ArrayList<>();
        List<Integer> error = new ArrayList<>();
        
        Map<Integer, Integer> questFee = new HashMap<>();
        
        for(Entry<Integer, Integer> e : base.entrySet()) {
            Integer v = target.get(e.getKey());
            
            if(v == null) {
                if(testingCheck || !checkedEndscriptQuests.contains(e.getKey())) {
                    result.add(e.getKey());
                    questFee.put(e.getKey(), e.getValue());
                }                
            } else if(v.intValue() != e.getValue().intValue()) {
                error.add(e.getKey());
            }
        }
        
        if(!result.isEmpty() || !error.isEmpty()) {
            printWriter.println("MISMATCH INFORMATION ON '" + (testingCheck ? "check" : "act") + "':");
            if(!result.isEmpty()) {
                Collections.sort(result, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                });
                
                printWriter.println("# MISSING");
                
                if(!printFees) {
                    for(Integer i : result) {
                        printWriter.println(i);
                    }
                } else {
                    for(Integer i : result) {
                        printWriter.println(i + " " + questFee.get(i));
                    }
                }
                
                printWriter.println();
            }
            
            if(!error.isEmpty() && testingCheck) {
                Collections.sort(error, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                });
                
                printWriter.println("# WRONG VALUE");
                
                for(Integer i : error) {
                    printWriter.println(i);
                }
                
                printWriter.println();
            }
            
            printWriter.println("\r\n");
        }
    }
    
    private static void ReportQuestMesoData() {
        // This will reference one line at a time
        
        try {
            System.out.println("Reading WZs...");
            readQuestMesoData();
            
            System.out.println("Reporting results...");
            // report missing meso checks on quest completes
            printWriter = new PrintWriter(newFile, "UTF-8");
            
            printReportFileHeader();
            
            printReportFileResults(checkedMesoQuests, appliedMesoQuests, true);
            printReportFileResults(appliedMesoQuests, checkedMesoQuests, false);

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
        ReportQuestMesoData();
    }
    
}
