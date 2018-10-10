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
package mapledojoupdate;

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
 
 Estimated parse time: 10 seconds
 
 */
public class MapleDojoUpdate {

    static String dojoDirectory = "lib/original/";
    static String outputDirectory = "lib/updated/";
    
    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialStringLength = 250;
    static boolean isDojoMapid;
    
    static byte status;
    
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
            printWriter.println(token);
        }
        else if(token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;
            
            if (status == 2) {
                String d = getName(token);
                if(!d.contentEquals("info")) {
                    forwardCursor(status);
                }
            } else if (status > 2) {
                forwardCursor(status);
            }
        }
        else {
            if (status == 2 && isDojoMapid) {
                String item = getName(token);
                
                if (item.contentEquals("onFirstUserEnter")) {
                    printWriter.println("    <string name=\"onFirstUserEnter\" value=\"dojang_1st\"/>");
                } else if (item.contentEquals("onUserEnter")) {
                    printWriter.println("    <string name=\"onUserEnter\" value=\"dojang_Eff\"/>");
                } else {
                    printWriter.println(token);
                }
            } else {
                printWriter.println(token);
            }
        }
    }
    
    private static int getMapId(String fileName) {
        return Integer.parseInt(fileName.substring(0, 9));
    }
    
    private static void parseDojoData(File file, String curPath) throws IOException {
        printWriter = new PrintWriter(outputDirectory + curPath + file.getName(), "UTF-8");
        
        fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);
        
        status = 0;
        
        int mapid = getMapId(file.getName());
        isDojoMapid = mapid >= 925020100 && mapid < 925040000;
        
        String line;
        while((line = bufferedReader.readLine()) != null) {
            translateToken(line);
        }

        bufferedReader.close();
        fileReader.close();

        printFileFooter();
        printWriter.close();
    }
    
    private static void printFileFooter() {
        printWriter.println("<!--");
        printWriter.println(" # WZ XML File updated by the MapleDojoUpdate feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account info from the server-side WZ.xmls.");
        printWriter.println("-->");
    }
    
    private static void parseDirectoryDojoData(String curPath) {
        File folder = new File(outputDirectory + curPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        System.out.println("Parsing directory '" + curPath + "'");
        folder = new File(dojoDirectory + curPath);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    parseDojoData(file, curPath);
                }
                catch(FileNotFoundException ex) {
                    System.out.println("Unable to open dojo file " + file.getAbsolutePath() + ".");
                }
                catch(IOException ex) {
                    System.out.println("Error reading dojo file " + file.getAbsolutePath() + ".");
                }

                catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                parseDirectoryDojoData(curPath + file.getName() + "/");
            }
        }
    }
    
    public static void main(String[] args) {
        parseDirectoryDojoData("");
    }
    
}
