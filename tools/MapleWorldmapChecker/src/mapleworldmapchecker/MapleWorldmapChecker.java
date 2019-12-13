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
package mapleworldmapchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author RonanLana
 
 This application parses the Map.wz file inputted and reports areas (mapids) that are supposed to be referenced 
 throughout the map tree (area map -> continent map -> world map) but are currently missing.
 
 */
public class MapleWorldmapChecker {
    
    static String newFile = "lib/Report.txt";
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static String worldmapPath = "../../wz/Map.wz/WorldMap";
    static int initialStringLength = 50;
    
    static Map<String, Set<Integer>> worldMapids = new HashMap<>();
    static Map<String, String> parentWorldmaps = new HashMap<>();
    static Set<String> rootWorldmaps = new HashSet<>();
    //static String rootWorldmap = "";
    
    static Set<Integer> currentWorldMapids;
    static String currentParent;
    
    static byte status = 0;
    static boolean isInfo;
    
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
    
    private static void translateToken(String token) {
        String d;
        
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if (status == 2) {
                d = getName(token);
                
                switch (d) {
                    case "MapList":
                        isInfo = false;
                        break;
                        
                    case "info":
                        isInfo = true;
                        break;
                        
                    default:
                        forwardCursor(status);
                }
            } else if (status == 4) {
                d = getName(token);
                
                if (!d.contentEquals("mapNo")) {
                    forwardCursor(status);
                }
            }
        }
        else {
            if (status == 4) {
                currentWorldMapids.add(Integer.valueOf(getValue(token)));
            } else if (status == 2 && isInfo) {
                try {
                    d = getName(token);
                    if (d.contentEquals("parentMap")) {
                        currentParent = (getValue(token) + ".img.xml");
                    } else {
                        forwardCursor(status);
                    }
                } catch (Exception e) {
                    System.out.println("failed '" + token + "'");
                    
                }
            }
        }
    }
    
    private static void parseWorldmapFile(File worldmapFile) throws IOException {
        String line;
        
        fileReader = new InputStreamReader(new FileInputStream(worldmapFile), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);
        
        currentParent = "";
        status = 0;
        
        currentWorldMapids = new HashSet<>();
        while((line = bufferedReader.readLine()) != null) {
            translateToken(line);
        }
        
        String worldmapName = worldmapFile.getName();
        worldMapids.put(worldmapName, currentWorldMapids);
        
        if (!currentParent.isEmpty()) parentWorldmaps.put(worldmapName, currentParent);
        else rootWorldmaps.add(worldmapName);

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void parseWorldmapDirectory() {
        System.out.println("Parsing directory '" + worldmapPath + "'");
        File folder = new File(worldmapPath);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    parseWorldmapFile(file);
                }
                catch(FileNotFoundException ex) {
                    System.out.println("Unable to open worldmap file " + file.getAbsolutePath() + ".");
                }
                catch(IOException ex) {
                    System.out.println("Error reading worldmap file " + file.getAbsolutePath() + ".");
                }

                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleWorldmapChecker feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void printReportFileResults(List<Pair<String, List<Pair<Integer, String>>>> results) {
        printWriter.println("Missing mapid references in top hierarchy:\n");
        for (Pair<String, List<Pair<Integer, String>>> res : results) {
            printWriter.println("'" + res.getLeft() + "':");

            for (Pair<Integer, String> i : res.getRight()) {
                printWriter.println("  " + i);
            }

            printWriter.println("\n");
        }
    }
    
    private static void verifyWorldmapTreeMapids() {
        try {
            printWriter = new PrintWriter(newFile, "UTF-8");    
            printReportFileHeader();
            
            if (rootWorldmaps.size() > 1) {
                printWriter.println("[WARNING] Detected several root worldmaps: " + rootWorldmaps + "\n");
            }
            
            Set<String> worldmaps = new HashSet<>(parentWorldmaps.keySet());
            worldmaps.addAll(rootWorldmaps);
            
            Map<String, Set<Integer>> tempMapids = new HashMap<>(worldMapids.size());
            for (Entry<String, Set<Integer>> e : worldMapids.entrySet()) {
                tempMapids.put(e.getKey(), new HashSet<>(e.getValue()));
            }
            
            Map<String, List<Pair<Integer, String>>> unreferencedMapids = new HashMap<>();
            
            for (String s : worldmaps) {
                List<Pair<Integer, String>> currentUnreferencedMapids = new ArrayList<>();

                for (Integer i : tempMapids.get(s)) {
                    String parent = parentWorldmaps.get(s);
                    
                    while (parent != null) {
                        Set<Integer> mapids = worldMapids.get(parent);
                        if (!mapids.contains(i)) {
                            currentUnreferencedMapids.add(new Pair<>(i, parent));
                            break;
                        } else {
                            tempMapids.get(parent).remove(i);
                        }
                        
                        parent = parentWorldmaps.get(parent);
                    }
                }
                
                if (!currentUnreferencedMapids.isEmpty()) {
                    unreferencedMapids.put(s, currentUnreferencedMapids);
                }
            }
            
            if (!unreferencedMapids.isEmpty()) {
                List<Pair<String, List<Pair<Integer, String>>>> unreferencedEntries = new ArrayList<>(20);
                for (Entry<String, List<Pair<Integer, String>>> e : unreferencedMapids.entrySet()) {
                    List<Pair<Integer, String>> list = new ArrayList<>(e.getValue());
                    Collections.sort(list, new Comparator<Pair<Integer, String>>() {
                        @Override
                        public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
                            return o1.getLeft().compareTo(o2.getLeft());
                        }
                    });

                    unreferencedEntries.add(new Pair<>(e.getKey(), list));
                }
                
                Collections.sort(unreferencedEntries, new Comparator<Pair<String, List<Pair<Integer, String>>>>() {
                    @Override
                    public int compare(Pair<String, List<Pair<Integer, String>>> o1, Pair<String, List<Pair<Integer, String>>> o2) {
                        return o1.getLeft().compareTo(o2.getLeft());
                    }
                });

                printReportFileResults(unreferencedEntries);
            }
            
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        parseWorldmapDirectory();
        verifyWorldmapTreeMapids();
    }
    
}
