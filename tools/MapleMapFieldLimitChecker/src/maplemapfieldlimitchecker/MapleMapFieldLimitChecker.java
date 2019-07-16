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
package maplemapfieldlimitchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author RonanLana
 * 
  This application seeks from the XMLs all mapid entries that holds the specified
  fieldLimit.
 */
public class MapleMapFieldLimitChecker {
    
    static String newFile = "lib/Report.txt";
    static String outputWzPath = "lib";
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static String wzPath = "../../wz";
    static int initialStringLength = 50;
    static int itemFileNameSize = 13;
    
    static int fieldLimit = 0x400000;
    
    static byte status = 0;
    static int mapid = 0;
    
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
    
    private static int getMapIdFromFilename(String name) {
        try {
            return Integer.valueOf(name.substring(0, name.indexOf('.')));
        } catch(Exception e) {
            return -1;
        }
    }
    
    private static void translateToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if (status == 2) {
                String d = getName(token);
                if (!d.contentEquals("info")) {
                    forwardCursor(status);
                }
            }
        }
        else {
            if (status == 2) {
                String d = getName(token);
                
                if (d.contentEquals("fieldLimit")) {
                    int value = Integer.valueOf(getValue(token));
                    if ((value & fieldLimit) == fieldLimit) {
                        System.out.println(mapid + " " + value);
                    }
                }
            }
        }
    }
    
    private static void inspectMapEntry() {
        String line = null;

        try {
            while((line = bufferedReader.readLine()) != null) {
                translateToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void loadMapWz() throws IOException {
        System.out.println("Reading Map.wz ...");
        ArrayList<File> files = new ArrayList<>();
        listFiles(wzPath + "/Map.wz/Map", files);

        for(File f : files) {
            fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);
            
            mapid = getMapIdFromFilename(f.getName());
            inspectMapEntry();

            bufferedReader.close();
            fileReader.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            loadMapWz();
            System.out.println("Done!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
}
