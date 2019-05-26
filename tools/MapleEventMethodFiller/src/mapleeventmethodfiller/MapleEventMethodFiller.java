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
package mapleeventmethodfiller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author RonanLana
 * 
  This application objective is to read all scripts from the event folder
  and fill empty functions for every function name not yet present in the
  script.
  
  Estimated parse time: 10 seconds
 */
public class MapleEventMethodFiller {

    private static boolean foundMatchingDataOnFile(String fileContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(fileContent);
        return matcher.find();
    }
    
    private static void fileSearchMatchingData(File file, Map<Pattern, String> functions) {
        try {
            String fileContent = FileUtils.readFileToString(file, "UTF-8");
            List<String> fillFunctions = new LinkedList<>();

            for(Entry<Pattern, String> f : functions.entrySet()) {
                if(!foundMatchingDataOnFile(fileContent, f.getKey())) {
                    fillFunctions.add(f.getValue());
                }
            }
            
            if (!fillFunctions.isEmpty()) {
                System.out.println("Filling out " + file.getName() + "...");
                
                FileWriter fileWriter = new FileWriter(file, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                
                printWriter.println();
                printWriter.println();
                printWriter.println("// ---------- FILLER FUNCTIONS ----------");
                printWriter.println();
                
                for (String s : fillFunctions) {
                    printWriter.println(s);
                    printWriter.println();
                }
                
                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void filterDirectorySearchMatchingData(String directoryPath, Map<Pattern, String> functions) {
        Iterator iter = FileUtils.iterateFiles(new File(directoryPath), new String[]{"sql", "js", "txt","java"}, true);

        while(iter.hasNext()) {
            File file = (File) iter.next();
            fileSearchMatchingData(file, functions);
        }
    }
    
    private static String jsFunction = "function(\\s)+";
    
    private static Pattern compileJsFunctionPattern(String function) {
        return Pattern.compile(jsFunction + function);
    }
    
    public static final Map<Pattern, String> functions = new HashMap<Pattern, String>(17) {{
        put(compileJsFunctionPattern("playerEntry"), "function playerEntry(eim, player) {}");
        put(compileJsFunctionPattern("playerExit"), "function playerExit(eim, player) {}");
        put(compileJsFunctionPattern("scheduledTimeout"), "function scheduledTimeout(eim) {}");
        put(compileJsFunctionPattern("playerUnregistered"), "function playerUnregistered(eim, player) {}");
        put(compileJsFunctionPattern("changedLeader"), "function changedLeader(eim, leader) {}");
        put(compileJsFunctionPattern("monsterKilled"), "function monsterKilled(mob, eim) {}");
        put(compileJsFunctionPattern("allMonstersDead"), "function allMonstersDead(eim) {}");
        put(compileJsFunctionPattern("playerDisconnected"), "function playerDisconnected(eim, player) {}");
        put(compileJsFunctionPattern("monsterValue"), "function monsterValue(eim, mobid) {return 0;}");
        put(compileJsFunctionPattern("dispose"), "function dispose() {}");
        put(compileJsFunctionPattern("leftParty"), "function leftParty(eim, player) {}");
        put(compileJsFunctionPattern("disbandParty"), "function disbandParty(eim, player) {}");
        put(compileJsFunctionPattern("clearPQ"), "function clearPQ(eim) {}");
        put(compileJsFunctionPattern("afterSetup"), "function afterSetup(eim) {}");
        put(compileJsFunctionPattern("cancelSchedule"), "function cancelSchedule() {}");
        put(compileJsFunctionPattern("setup"), "function setup(eim, leaderid) {}");
        //put(compileJsFunctionPattern("getEligibleParty"), "function getEligibleParty(party) {}"); not really needed
    }};
    
    public static void main(String[] args) {
        filterDirectorySearchMatchingData("../../scripts/event", functions);
    }
    
}
