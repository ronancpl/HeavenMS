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
package mapleskillbookstackupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 *
 * @author RonanLana
 * 
 * This application parses skillbook XMLs, filling up stack amount of those
 * items to 100 (eliminating limitations on held skillbooks, now using
 * default stack quantity expected from USE items).
 * 
 * Estimated parse time: 10 seconds
 */
public class MapleSkillbookStackUpdate {
    
    static String wzPath = "../../wz";
    static String outputWzPath = "lib";
    

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    static boolean displayExtraInfo = true;     // display items with zero quantity over the quest act WZ
    
    static int status = 0;
    
    private static boolean isSkillMasteryBook(int itemid) {
        return itemid >= 2280000 && itemid < 2300000;
    }
    
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
        printWriter.println(token);
    }
    
    private static void translateItemToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if(status == 2) {      //itemid
                int itemid = Integer.valueOf(getName(token));
                
                if (!isSkillMasteryBook(itemid)) {
                    printWriter.println(token);
                    forwardCursor(status);
                    return;
                }
            }
        } else {
            if(status == 3) {
                if (getName(token).contentEquals("slotMax")) {
                    printWriter.println("      <int name=\"slotMax\" value=\"100\"/>");
                    return;
                }
            }
        }
        
        printWriter.println(token);
    }
    
    private static void parseItemFile(File file, String outputName) {
        // This will reference one line at a time
        String line = null;
        
        try {
            printWriter = new PrintWriter(outputName);
            fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                translateItemToken(line);
            }
            
            bufferedReader.close();
            fileReader.close();
            printWriter.close();
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + file.getName() + "'");
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void parseItemDirectory(String wzPath, String outputPath) {
        File wzDir = new File(wzPath);
        
        for (File f : wzDir.listFiles()) {
            parseItemFile(f, outputPath + f.getName());
        }
        
    }
    
    public static void main(String[] args) {
        System.out.println("Reading item files...");
        parseItemDirectory(wzPath + "/Item.wz/Consume/", outputWzPath + "/");
        System.out.println("Done!");
    }
    
}
