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
package maplemapinforetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author RonanLana
 * 
 * The main objective of this tool is to locate all mapids that doesn't have
 * the "info" node in their WZ node tree.
 */
public class MapleMapInfoRetriever {
    static String mapWzPath = "../../wz/Map.wz/Map";
    static String newFile = "lib/MapReport.txt";
    
    static List<Integer> missingInfo = new ArrayList<>();

    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    static boolean hasInfo;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[50];
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

    private static boolean translateToken(String token) {
        String d;
        int temp;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {
                d = getName(token);
                if(d.contains("info")) {
                    hasInfo = true;
                    return true;
                }
                
                temp = status;
                forwardCursor(temp);
            }

            status += 1;
        }
        
        return false;
    }

    private static void searchMapDirectory(int mapArea) {
        try {
            Iterator iter = FileUtils.iterateFiles(new File(mapWzPath + "/Map" + mapArea), new String[]{"xml"}, true);
            System.out.println("Parsing map area " + mapArea);
            
            while(iter.hasNext()) {
                File file = (File) iter.next();
                searchMapFile(file);
            }
        } catch(IllegalArgumentException e) {}
    }
        
    private static void searchMapFile(File file) {
        // This will reference one line at a time
        String line = null;

        try {
            fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            hasInfo = false;
            status = 0;
            
            while((line = bufferedReader.readLine()) != null) {
                if(translateToken(line)) {
                    break;
                }
            }
            
            if(!hasInfo) missingInfo.add(Integer.valueOf(file.getName().split(".img.xml")[0]));

            bufferedReader.close();
            fileReader.close();
        }

        catch(IOException ex) {
            System.out.println("Error reading file '" + file.getName() + "'");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeReport() {
        try {
            printWriter = new PrintWriter(newFile, "UTF-8");

            if(!missingInfo.isEmpty()) {
                for(Integer i : missingInfo) {
                    printWriter.println(i);
                }
            } else {
                printWriter.println("All map files contains 'info' node.");
            }

            printWriter.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        for(int i = 0; i < 10; i++) {
            searchMapDirectory(i);
        }
        writeReport();
    }

}
