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
package maplecashcosmeticsfetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

import tools.MapleItemInformationProvider;

/**
 *
 * @author RonanLana
 
  This application gathers info from the WZ.XML files, fetching all cosmetic coupons and tickets from there, and then
  searches the NPC script files, identifying the stylish NPCs that supposedly uses them. It will reports all NPCs that
  uses up a card, as well as report those currently unused.
 
  Estimated parse time: 10 seconds

 */
public class MapleCashCosmeticsFetcher {
    static MapleItemInformationProvider ii;
    
    static String wzPath = "../../wz";
    static String scriptPath = "../../scripts";
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static Map<Integer, String> scriptEntries = new HashMap<>(500);

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
    
    private static int getNpcIdFromFilename(String name) {
        try {
            return Integer.valueOf(name.substring(0, name.indexOf('.')));
        } catch(Exception e) {
            return -1;
        }
    }
    
    private static void loadScripts() throws Exception {
        ArrayList<File> files = new ArrayList<>();
        listFiles(scriptPath + "/npc", files);

        for(File f : files) {
            Integer npcid = getNpcIdFromFilename(f.getName());
            
            //System.out.println("Parsing " + f.getAbsolutePath());
            fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            StringBuilder stringBuffer = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine())!=null){
                stringBuffer.append(line).append("\n");
            }
            
            scriptEntries.put(npcid, stringBuffer.toString());

            bufferedReader.close();
            fileReader.close();
        }
    }
    
    private static List<Integer> findItemidOnScript(int itemid) {
        List<Integer> files = new LinkedList<>();
        String t = String.valueOf(itemid);
        
        for (Entry<Integer, String> text : scriptEntries.entrySet()) {
            if (text.getValue().contains(t)) {
                files.add(text.getKey());
            }
        }
        
        return files;
    }
    
    private static void reportCosmeticCouponResults() {
        for (int itemid = 5150000; itemid <= 5154000; itemid++) {
            String itemName = ii.getName(itemid);

            if (itemName != null) {
                List<Integer> npcids = findItemidOnScript(itemid);

                if (!npcids.isEmpty()) {
                    System.out.println("Itemid " + itemid + " found on " + npcids + ". (" + itemName + ")");
                } else {
                    System.out.println("NOT FOUND ITEMID " + itemid + " (" + itemName + ")");
                }
            }
        }
    }
    
    public static void main(String[] args) {
        System.setProperty("wzpath", wzPath);
        ii = MapleItemInformationProvider.getInstance();
        
        try {
            loadScripts();
            System.out.println("Loaded scripts");
            
            reportCosmeticCouponResults();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}