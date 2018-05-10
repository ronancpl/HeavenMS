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
package maplebosshpbarfetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.io.File;

/**
 *
 * @author RonanLana
 
 This application parses the Mob.wz file inputted and generates a report showing
 all cases where a mob has a boss HP bar and doesn't have a "boss" label.
 
 Running it should generate a report file under "lib" folder with the search results.
 
 */
public class MapleBossHpBarFetcher {
    static String mobDirectory = "../../wz/Mob.wz/";
    static String newFile = "lib/Report.txt";

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    
    static byte status = 0;
    
    static int curBoss;
    static int curHpTag;
    static int curMobId;
    
    static List<Integer> missingBosses = new LinkedList<>();
    
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
    
    private static void readMobLabel(String token) {
        String name = getName(token);
        String value = getValue(token);

        switch(name) {
            case "boss":
                curBoss = Integer.parseInt(value);
                break;

            case "hpTagColor":
                curHpTag = Integer.parseInt(value);
                break;
        }
    }
    
    private static void processMobData() {
        if(curHpTag != Integer.MAX_VALUE && curBoss == Integer.MAX_VALUE) {
            missingBosses.add(curMobId);
        }
    }
    
    private static void translateToken(String token) {
        String d;
        
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if(status == 1) {
                processMobData();
            }
        }
        else if(token.contains("imgdir")) {
            if(status == 0) {
                String mobText = getName(token);
                curMobId = Integer.valueOf(mobText.substring(0, mobText.lastIndexOf('.')));
            }
            else if(status == 1) {           //getting info
                d = getName(token);
                
                if(!d.contentEquals("info")) {
                    forwardCursor(status);
                } else {
                    curBoss = Integer.MAX_VALUE;
                    curHpTag = Integer.MAX_VALUE;
                }
            }
            else if(status == 2) {
                forwardCursor(status);
            }
            
            status += 1;
        }
        else {
            if(status == 2) {      //info tags
                readMobLabel(token);
            }
        }
    }
    
    private static void readBossHpBarData() throws IOException {
        String line;
        
        File folder = new File(mobDirectory);
        for(File file : folder.listFiles()) {
            if (file.isFile()) {
                fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                bufferedReader = new BufferedReader(fileReader);

                while((line = bufferedReader.readLine()) != null) {
                    translateToken(line);
                }

                bufferedReader.close();
                fileReader.close();
            }
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleBossHpBarFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void printReportFileResults() {
        for(Integer mid : missingBosses) {
            printWriter.println("Missing 'isBoss' on id: " + mid);
        }
    }
    
    private static void ReportBossHpBarData() {
        // This will reference one line at a time
        
        try {
            System.out.println("Reading WZs...");
            readBossHpBarData();
            
            System.out.println("Reporting results...");
            printWriter = new PrintWriter(newFile, "UTF-8");
            
            printReportFileHeader();
            printReportFileResults();
            
            printWriter.close();
            System.out.println("Done!");
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open mob file.");
        }
        catch(IOException ex) {
            System.out.println("Error reading mob file.");
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        ReportBossHpBarData();
    }
    
}
