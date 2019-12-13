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
package maplecashcosmeticschecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 *
 * @author RonanLana
 
  This application parses the cosmetic recipes defined within "lib/care" folder, loads
  every present cosmetic itemid from the XML data, then checks the scripts for missed
  cosmetics within the stylist/surgeon. Results from the search are reported in a report
  file.
  
  Note: to best make use of this feature, set ignoreCurrentScriptCosmetics = true. This
  way, every available cosmetic present on the recipes will be listed on the report.
  
  Estimated parse time: 1 minute

 */
public class MapleCashCosmeticsChecker {
    static String libPath = "lib";
    static String handbookPath = "../../handbook";
    static String wzPath = "../../wz";
    static String scriptPath = "../../scripts";
    
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static boolean ignoreCurrentScriptCosmetics = false;
    
    static int initialStringLength = 50;
    
    static byte status = 0;
    
    static Map<Integer, Set<Integer>> scriptCosmetics = new HashMap<>();
    static Map<Integer, String> scriptEntries = new HashMap<>(500);
    
    static Set<Integer> allCosmetics = new HashSet<>();
    
    static Set<Integer> unusedCosmetics = new HashSet<>();
    static Map<Integer, List<Integer>> usedCosmetics = new HashMap<>();
    
    static Map<Integer, String> couponNames = new HashMap<>();
    static Map<Integer, Integer> cosmeticNpcs = new HashMap<>(); // expected only 1 NPC per cosmetic coupon (town care/salon)
    static Map<List<String>, Integer> cosmeticNpcids = new HashMap<>();
    
    static Set<String> missingCosmeticNames = new HashSet<>();
    static Map<String, Integer> cosmeticNameIds = new HashMap<>();
    static Map<Integer, String> cosmeticIdNames = new HashMap<>();
    
    static Map<Pair<Integer, String>, Set<Integer>> missingCosmeticsNpcTypes = new HashMap<>();
    
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
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if (status == 3) {
                String d = getName(token);
                
                if (!(d.contentEquals("Face") || d.contentEquals("Hair"))) {
                    forwardCursor(status);
                }
            } else if (status == 4) {
                String d = getName(token);
                int itemid = Integer.valueOf(d);
                
                int cosmeticid;
                if (itemid >= 30000) {
                    cosmeticid = (itemid / 10) * 10;
                } else {
                    cosmeticid = itemid - ((itemid / 100) % 10) * 100;
                }
                
                allCosmetics.add(cosmeticid);
                forwardCursor(status);
            }
        }
    }
    
    private static void readEqpStringData(String eqpStringDirectory) throws IOException {
        String line;
        
        fileReader = new InputStreamReader(new FileInputStream(eqpStringDirectory), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
            translateToken(line);
        }

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void loadCosmeticWzData() throws IOException {
        System.out.println("Reading String.wz ...");
        readEqpStringData(wzPath + "/String.wz/Eqp.img.xml");
    }
    
    private static void setCosmeticUsage(List<Integer> usedByNpcids, int cosmeticid) {
        if (!usedByNpcids.isEmpty()) {
            usedCosmetics.put(cosmeticid, usedByNpcids);
        } else {
            unusedCosmetics.add(cosmeticid);
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
    
    private static int getNpcIdFromFilename(String name) {
        try {
            return Integer.valueOf(name.substring(0, name.indexOf('.')));
        } catch(Exception e) {
            return -1;
        }
    }
    
    private static List<Integer> findCosmeticDataNpcids(int itemid) {
        List<Integer> npcids = new LinkedList<>();
        for (Entry<Integer, Set<Integer>> sc : scriptCosmetics.entrySet()) {
            if (sc.getValue().contains(itemid)) {
                npcids.add(itemid);
            }
        }
        
        return npcids;
    }
    
    private static void loadScripts() throws IOException {
        ArrayList<File> files = new ArrayList<>();
        listFiles(scriptPath + "/npc", files);

        for(File f : files) {
            Integer npcid = getNpcIdFromFilename(f.getName());
            
            //System.out.println("Parsing " + f.getAbsolutePath());
            fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            String line;

            StringBuilder stringBuffer = new StringBuilder();
            
            boolean cosmeticNpc = false;
            Set<Integer> cosmeticids = new HashSet<>();
            while((line = bufferedReader.readLine())!=null){
                String[] s = line.split("hair_. = Array\\(", 2);
                
                if (s.length > 1) {
                    cosmeticNpc = true;
                    s = s[1].split("\\)", 2);
                    s = s[0].split(", ");
                    
                    for (String st : s) {
                        if (!st.isEmpty()) {
                            int itemid = Integer.valueOf(st);
                            cosmeticids.add(itemid);
                        }
                    }
                } else {
                    s = line.split("face_. = Array\\(", 2);
                
                    if (s.length > 1) {
                        cosmeticNpc = true;
                        s = s[1].split("\\)", 2);
                        s = s[0].split(", ");

                        for (String st : s) {
                            if (!st.isEmpty()) {
                                int itemid = Integer.valueOf(st);
                                cosmeticids.add(itemid);
                            }
                        }
                    }
                }
                
                stringBuffer.append(line).append("\n");
            }
            
            scriptEntries.put(npcid, stringBuffer.toString());
            
            if (cosmeticNpc) {
                scriptCosmetics.put(npcid, cosmeticids);
            }

            bufferedReader.close();
            fileReader.close();
        }
    }
    
    private static void processCosmeticScriptData() throws IOException {
        System.out.println("Reading script files ...");
        loadScripts();
        
        if (ignoreCurrentScriptCosmetics) {
            for (Set<Integer> npcCosmetics : scriptCosmetics.values()) {
                npcCosmetics.clear();
            }
        }
        
        for (Integer itemid : allCosmetics) {
            List<Integer> npcids = findCosmeticDataNpcids(itemid);
            setCosmeticUsage(npcids, itemid);
        }
    }
    
    private static List<Integer> loadCosmeticCouponids() throws IOException {
        List<Integer> couponItemids = new LinkedList<>();
        
        fileReader = new InputStreamReader(new FileInputStream(handbookPath + "/Cash.txt"), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        String line;
        while((line = bufferedReader.readLine())!=null){
            if (line.isEmpty()) continue;
            String[] s = line.split(" - ", 3);
            
            int itemid = Integer.valueOf(s[0]);
            if (itemid >= 5150000 && itemid < 5160000) {
                couponItemids.add(itemid);
                couponNames.put(itemid, s[1]);
            }
        }
        
        bufferedReader.close();
        fileReader.close();
        
        return couponItemids;
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
    
    private static void loadCosmeticCouponNpcs() throws IOException {
        System.out.println("Locating cosmetic NPCs ...");
        
        for (Integer itemid : loadCosmeticCouponids()) {
            List<Integer> npcids = findItemidOnScript(itemid);
            
            if (!npcids.isEmpty()) {
                cosmeticNpcs.put(itemid, npcids.get(0));
            }
        }
    }
    
    private enum CosmeticType {
        HAIRSTYLE,
        HAIRCOLOR,
        DIRTYHAIR,
        FACE_SURGERY,
        EYE_COLOR,
        SKIN_CARE
    }
    
    private static Pair<Integer, CosmeticType> parseCosmeticCoupon(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            String s = tokens[i];
            
            if (s.startsWith("Hair")) {
                if (s.contentEquals("Hairstyle")) {
                    return new Pair<>(i, CosmeticType.HAIRSTYLE);
                } else {
                    if (i - 1 >= 0 && tokens[i - 1].contentEquals("Dirty")) {
                        return new Pair<>(i - 1, CosmeticType.DIRTYHAIR);
                    } else if (i + 1 < tokens.length && tokens[i + 1].contentEquals("Color")) {
                        return new Pair<>(i, CosmeticType.HAIRCOLOR);
                    } else {
                        return new Pair<>(i, CosmeticType.HAIRSTYLE);
                    }
                }
            } else if (s.startsWith("Face")) {
                return new Pair<>(i, CosmeticType.FACE_SURGERY);
            } else if (s.startsWith("Cosmetic")) {
                return new Pair<>(i, CosmeticType.EYE_COLOR);
            } else if (s.startsWith("Plastic")) {
                return new Pair<>(i, CosmeticType.FACE_SURGERY);
            } else if (s.startsWith("Skin")) {
                return new Pair<>(i, CosmeticType.SKIN_CARE);
            }
        }
        
        return null;
    }
    
    private static List<String> getCosmeticCouponData(String town, String type, String subtype) {
        List<String> ret = new ArrayList<>(3);
        ret.add(town);
        ret.add(type);
        ret.add(subtype);
        return ret;
    }
    
    private static List<String> parseCosmeticCoupon(String couponName) {
        String town, type, subtype = "EXP";
        
        String[] s = couponName.split(" Coupon ", 2);
        
        if (s.length > 1) {
            subtype = s[1].substring(1, s[1].length() - 1);
        }
        
        String[] tokens = s[0].split(" ");
        Pair<Integer, CosmeticType> cosmeticData = parseCosmeticCoupon(tokens);
        if (cosmeticData == null) return null;
        
        town = "";
        for (int i = 0; i < cosmeticData.left; i++) {
            town += (tokens[i] + "_");
        }
        town = town.substring(0, town.length() - 1).toLowerCase();
        
        switch (cosmeticData.right) {
            case HAIRSTYLE:
                type = "hair";
                break;
                
            case FACE_SURGERY:
                type = "face";
                break;
                
            default:
                return null;
        }
        
        return getCosmeticCouponData(town, type, subtype);
    }
    
    private static void generateCosmeticPlaceNpcs() {
        for (Entry<Integer, String> e : couponNames.entrySet()) {
            Integer npcid = cosmeticNpcs.get(e.getKey());
            if (npcid == null) continue;
            
            String couponName = e.getValue();
            List<String> couponData = parseCosmeticCoupon(couponName);
            
            if (couponData == null) continue;
            cosmeticNpcids.put(couponData, npcid);
        }
    }
    
    private static Integer getCosmeticNpcid(String townName, String typeCosmetic, String typeCoupon) {
        return cosmeticNpcids.get(getCosmeticCouponData(townName, typeCosmetic, typeCoupon));
    }
    
    private static String getCosmeticName(String name, boolean gender) {
        String ret = name + " (" + (gender ? "F" : "M") + ")";
        return ret;
    }
    
    private static void loadCosmeticNames(String cosmeticPath) throws IOException {
        fileReader = new InputStreamReader(new FileInputStream(cosmeticPath), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] s = line.split(" - ", 3);
            int itemid = Integer.valueOf(s[0]);
            
            String name;
            if (itemid < 30000) {
                itemid = itemid - ((itemid / 100) % 10) * 100;
                
                int idx = s[1].lastIndexOf(" ");
                if (idx > -1) {
                    name = s[1].substring(0, idx);
                } else {
                    name = s[1];
                }
            } else {
                itemid = (Integer.valueOf(s[0]) / 10) * 10;
                
                int idx = s[1].indexOf(" ");
                if (idx > -1) {
                    name = s[1].substring(idx + 1);
                } else {
                    name = s[1];
                }
            }
            
            name = name.trim();
            
            String cname = getCosmeticName(name, (((itemid / 1000) % 10) % 3) != 0);
            
            /*
            if (cosmeticNameIds.containsKey(cname) && Math.abs(cosmeticNameIds.get(cname) - itemid) > 50) {
                System.out.println("Clashing '" + name + "' " + itemid + "/" + cosmeticNameIds.get(cname));
            }
            */
            
            cosmeticNameIds.put(cname, itemid);
            cosmeticIdNames.put(itemid, name);
        }

        bufferedReader.close();
        fileReader.close();
    }
    
    private static void loadCosmeticNames() throws IOException {
        System.out.println("Reading cosmetics from handbook ...");
        
        loadCosmeticNames(handbookPath + "/Equip/Face.txt");
        loadCosmeticNames(handbookPath + "/Equip/Hair.txt");
    }
    
    private static List<Integer> fetchExpectedCosmetics(String[] cosmeticList, boolean gender) {
        List<Integer> list = new LinkedList<>();
        
        for (String cosmetic : cosmeticList) {
            String cname = getCosmeticName(cosmetic, gender);
            Integer itemid = cosmeticNameIds.get(cname);
            if (itemid != null) {
                list.add(itemid);
            } else {
                missingCosmeticNames.add(cosmetic);
            }
        }
        
        return list;
    }
    
    private static void verifyCosmeticExpectedFile(File f) throws IOException {
        String townName = f.getParent().substring(f.getParent().lastIndexOf("\\") + 1);
        String typeCosmetic = f.getName().substring(0, f.getName().indexOf("."));
        
        fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);

        String line;
        while((line = bufferedReader.readLine())!=null){
            String[] s = line.split(": ", 2);
            String[] t = s[0].split("ale ");
            
            String typeCoupon = t[1];
            boolean gender = !t[0].contentEquals("M");
            
            Integer npcid = getCosmeticNpcid(townName, typeCosmetic, typeCoupon);
            if (npcid != null) {
                String[] cosmetics = s[1].split(", ");
                List<Integer> cosmeticItemids = fetchExpectedCosmetics(cosmetics, gender);
                
                Set<Integer> npcCosmetics = scriptCosmetics.get(npcid);
                Set<Integer> missingCosmetics = new HashSet<>();
                for (Integer itemid : cosmeticItemids) {
                    if (!npcCosmetics.contains(itemid)) {
                        missingCosmetics.add(itemid);
                    }
                }
                
                if (!missingCosmetics.isEmpty()) {
                    Pair<Integer, String> key = new Pair<>(npcid, typeCoupon);
                    
                    Set<Integer> list = missingCosmeticsNpcTypes.get(key);
                    if (list == null) {
                        missingCosmeticsNpcTypes.put(key, missingCosmetics);
                    } else {
                        list.addAll(missingCosmetics);
                    }
                }
            }
        }
        
        bufferedReader.close();
        fileReader.close();
    }
    
    private static void verifyCosmeticExpectedData() throws IOException {
        System.out.println("Analyzing cosmetic NPC scripts ...");
        
        ArrayList<File> cosmeticRecipes = new ArrayList<>();
        listFiles(libPath + "/care", cosmeticRecipes);
        
        for (File f : cosmeticRecipes) {
            verifyCosmeticExpectedFile(f);
        }
    }
    
    private static List<Pair<Pair<Integer, String>, List<Integer>>> getSortedMapEntries(Map<Pair<Integer, String>, Set<Integer>> map) {
        List<Pair<Pair<Integer, String>, List<Integer>>> list = new ArrayList<>(map.size());
        for(Entry<Pair<Integer, String>, Set<Integer>> e : map.entrySet()) {
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
        
        Collections.sort(list, new Comparator<Pair<Pair<Integer, String>, List<Integer>>>() {
            @Override
            public int compare(Pair<Pair<Integer, String>, List<Integer>> o1, Pair<Pair<Integer, String>, List<Integer>> o2) {
                int cmp = o1.getLeft().getLeft() - o2.getLeft().getLeft();
                if (cmp == 0) {
                    return o1.getLeft().getRight().compareTo(o2.getLeft().getRight());
                } else {
                    return cmp;
                }
            }
        });
        
        return list;
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleCashCosmeticsChecker feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server source files and the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static Pair<List<Integer>, List<Integer>> getCosmeticReport(List<Integer> itemids) {
        List<Integer> maleItemids = new LinkedList<>();
        List<Integer> femaleItemids = new LinkedList<>();
        
        for (Integer i : itemids) {
            if ((((i / 1000) % 10) % 3) == 0) {
                maleItemids.add(i);
            } else {
                femaleItemids.add(i);
            }
        }
        
        return new Pair<>(maleItemids, femaleItemids);
    }
    
    private static void reportNpcCosmetics(List<Integer> itemids) {
        if (!itemids.isEmpty()) {
            String res = "    ";
            for (Integer i : itemids) {
                res += (i + ", ");
                unusedCosmetics.remove(i);
            }
            
            printWriter.println(res.substring(0, res.length() - 2));
        }
    }
    
    private static void reportCosmeticResults() throws IOException {
        System.out.println("Reporting results ...");
        
        printWriter = new PrintWriter("lib/result.txt", "UTF-8");
        
        printReportFileHeader();
        
        if (!missingCosmeticsNpcTypes.isEmpty()) {
            printWriter.println("Found " + missingCosmeticsNpcTypes.size() + " entries with missing cosmetic entries.");
            
            for (Pair<Pair<Integer, String>, List<Integer>> mcn : getSortedMapEntries(missingCosmeticsNpcTypes)) {
                printWriter.println("  NPC " + mcn.getLeft());
                
                Pair<List<Integer>, List<Integer>> genderItemids = getCosmeticReport(mcn.getRight());
                reportNpcCosmetics(genderItemids.getLeft());
                reportNpcCosmetics(genderItemids.getRight());
                printWriter.println();
            }
        }
        
        if (!unusedCosmetics.isEmpty()) {
            printWriter.println("Unused cosmetics: " + unusedCosmetics.size());
            
            List<Integer> list = new ArrayList<>(unusedCosmetics);
            Collections.sort(list);
            
            for (Integer i : list) {
                printWriter.println(i + " " + cosmeticIdNames.get(i));
            }
            
            printWriter.println();
        }
        
        if (!missingCosmeticNames.isEmpty()) {
            printWriter.println("Missing cosmetic itemids: " + missingCosmeticNames.size());
            
            List<String> listString = new ArrayList<>(missingCosmeticNames);
            Collections.sort(listString);
            
            for (String c : listString) {
                printWriter.println(c);
            }
            
            printWriter.println();
        }

        printWriter.close();
    }
    
    public static void main(String[] args) {
        try {
            loadCosmeticWzData();
            processCosmeticScriptData();
            
            loadCosmeticCouponNpcs();
            generateCosmeticPlaceNpcs();
            
            loadCosmeticNames();
            verifyCosmeticExpectedData();
            
            reportCosmeticResults();
            System.out.println("Done!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}