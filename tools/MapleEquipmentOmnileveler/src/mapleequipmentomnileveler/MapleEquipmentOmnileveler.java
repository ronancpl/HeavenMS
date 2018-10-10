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
package mapleequipmentomnileveler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 *
 * @author RonanLana
 
 This application parses the Character.wz folder inputted and adds/updates the "info/level"
 node on every known equipment id. This addition enables client-side view of the equipment
 level attribute on every equipment in the game, given proper item visibility, be it from
 own equipments or from other players.
 
 Estimated parse time: 7 minutes
 
 */
public class MapleEquipmentOmnileveler {

    static String equipDirectory = "lib/original/";
    static String outputDirectory = "lib/updated/";
    
    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialStringLength = 250;
    
    static int fixedExp = 10000;
    static int maxEqpLevel = 30;
    
    static int infoTagState = -1, infoTagExpState = -1;
    
    static boolean infoTagLevel;
    static boolean infoTagLevelExp;
    static boolean infoTagLevelInfo;
    
    static int parsedLevels = 0;
    
    static byte status;
    static boolean upgradeable;
    static boolean cash;
    
    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;
        
        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[initialStringLength];
        try {
            token.getChars(i, j, dest, 0);
        } catch (StringIndexOutOfBoundsException e) {
            // do nothing
            return "";
        } catch (Exception e) {
            System.out.println("error in: " + token + "");
            e.printStackTrace();
            try {
                Thread.sleep(100000000);
            } catch (Exception ex) {}
        }
        

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
                printWriter.println(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateLevelCursor(int st) {
        String line = null;
        
        try {
            infoTagLevelInfo = false;
            while (status >= st && (line = bufferedReader.readLine()) != null) {
                translateLevelToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateInfoTag(int st) {
        infoTagLevel = false;
        String line = null;

        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) { // skipping directory & canvas definition
                translateInfoToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        if (!upgradeable || cash) {
            throw new RuntimeException();
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
    
    private static void printUpdatedLevelExp() {
        printWriter.println("          <int name=\"exp\" value=\"" + fixedExp + "\"/>");
    }
    
    private static void printDefaultLevel(int level) {
        printWriter.println("        <imgdir name=\"" + level + "\">");
        printUpdatedLevelExp();
        printWriter.println("        </imgdir>");
    }
    
    private static void printDefaultLevelInfoTag() {
        printWriter.println("      <imgdir name=\"info\">");
        for (int i = 1; i <= maxEqpLevel; i++) printDefaultLevel(i);
        printWriter.println("      </imgdir>");
    }
    
    private static void printDefaultLevelTag() {
        printWriter.println("    <imgdir name=\"level\">");
        printDefaultLevelInfoTag();
        printWriter.println("    </imgdir>");
    }
    
    private static void processLevelInfoTag(int st) {
        String line;
        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                translateLevelExpToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void processLevelInfoSet(int st) {
        parsedLevels = (1 << maxEqpLevel) - 1;
        
        String line;
        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                translateLevelInfoSetToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateLevelToken(String token) {
        if(token.contains("/imgdir")) {
            if (status == 3) {
                if (!infoTagLevelInfo) {
                    printDefaultLevelInfoTag();
                }
            }
            printWriter.println(token);
            
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;
            
            if (status == 4) {
                String d = getName(token);
                if(d.contentEquals("info")) {
                    infoTagLevelInfo = true;
                    processLevelInfoSet(status);
                } else {
                    forwardCursor(status);
                }
            }
        }
        else {
            printWriter.println(token);
        }
    }
    
    private static void translateLevelInfoSetToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if (status == 3) {
                if (parsedLevels != 0) {
                    for (int i = 0; i < maxEqpLevel; i++) {
                        if ((parsedLevels >> i) % 2 != 0) {
                            int level = i + 1;
                            printDefaultLevel(level);
                        }
                    }
                }
            }
            
            printWriter.println(token);
        }
        else if(token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;
            
            if (status == 5) {
                int level = Integer.parseInt(getName(token)) - 1;
                parsedLevels ^= (1 << level);
                
                infoTagLevelExp = false;
                infoTagExpState = status;  // status: 5
                processLevelInfoTag(status);        
                infoTagExpState = -1;
            }
        }
        else {
            printWriter.println(token);
        }
    }
    
    private static void translateLevelExpToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if (status < infoTagExpState) {
                if (!infoTagLevelExp) {
                    printUpdatedLevelExp();
                }
            }
            
            printWriter.println(token);
        }
        else if(token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;
            
            forwardCursor(status);
        }
        else {
            String name = getName(token);
            if (name.contentEquals("exp")) {
                infoTagLevelExp = true;
                printUpdatedLevelExp();
            } else {
                printWriter.println(token);
            }
        }
    }
    
    private static void translateInfoToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if (status < infoTagState) {
                if (!infoTagLevel) {
                    printDefaultLevelTag();
                }
            }
            
            printWriter.println(token);
        }
        else if(token.contains("imgdir")) {
            status += 1;
            printWriter.println(token);
            
            String d = getName(token);
            if (d.contentEquals("level")) {
                infoTagLevel = true;
                translateLevelCursor(status);
            } else {
                forwardCursor(status);
            }
        }
        else {
            String name = getName(token);
            
            switch(name) {
                case "cash":
                    if (!getValue(token).contentEquals("0")) {
                        cash = true;
                    }
                    break;
                    
                case "tuc":
                case "incPAD":
                case "incMAD":
                case "incPDD":
                case "incMDD":
                case "incACC":
                case "incEVA":
                case "incSpeed":
                case "incJump":
                case "incMHP":
                case "incMMP":
                case "incSTR":
                case "incDEX":
                case "incINT":
                case "incLUK":
                    if (!getValue(token).contentEquals("0")) {
                        upgradeable = true;
                    }
                    break;
            }
            
            printWriter.println(token);
        }
    }
    
    private static boolean translateToken(String token) {
        boolean accessInfoTag = false;
        
        if(token.contains("/imgdir")) {
            status -= 1;
            printWriter.println(token);
        }
        else if(token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;
            
            if (status == 2) {
                String d = getName(token);
                if(!d.contentEquals("info")) {
                    forwardCursor(status);
                } else {
                    accessInfoTag = true;
                }
            } else if (status > 2) {
                forwardCursor(status);
            }
        }
        else {
            printWriter.println(token);
        }
        
        return accessInfoTag;
    }
    
    private static void copyCashItemData(File file, String curPath) throws IOException {
        printWriter = new PrintWriter(outputDirectory + curPath + file.getName(), "UTF-8");
        
        fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);
        
        String line;
        while((line = bufferedReader.readLine()) != null) {
            printWriter.println(line);
        }

        bufferedReader.close();
        fileReader.close();

        printWriter.close();
    }
    
    private static void parseEquipData(File file, String curPath) throws IOException {
        printWriter = new PrintWriter(outputDirectory + curPath + file.getName(), "UTF-8");
        
        fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);
        
        try {
            status = 0;
            upgradeable = false;
            cash = false;
            
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if (translateToken(line)) {
                    infoTagState = status;  // status: 2
                    translateInfoTag(status);
                    infoTagState = -1;
                }
            }
            
            bufferedReader.close();
            fileReader.close();
            
            printFileFooter();
            printWriter.close();
        } catch (RuntimeException e) {
            bufferedReader.close();
            fileReader.close();
            
            printWriter.close();
            
            copyCashItemData(file, curPath);
        }
    }
    
    private static void printFileFooter() {
        printWriter.println("<!--");
        printWriter.println(" # WZ XML File parsed by the MapleEquipmentOmnilever feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account info from the server-side WZ.xmls.");
        printWriter.println("-->");
    }
    
    private static void parseDirectoryEquipData(String curPath) {
        File folder = new File(outputDirectory + curPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        System.out.println("Parsing directory '" + curPath + "'");
        folder = new File(equipDirectory + curPath);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    parseEquipData(file, curPath);
                }
                catch(FileNotFoundException ex) {
                    System.out.println("Unable to open equip file " + file.getAbsolutePath() + ".");
                }
                catch(IOException ex) {
                    System.out.println("Error reading equip file " + file.getAbsolutePath() + ".");
                }

                catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                parseDirectoryEquipData(curPath + file.getName() + "/");
            }
        }
    }
    
    public static void main(String[] args) {
        parseDirectoryEquipData("");
    }
    
}
