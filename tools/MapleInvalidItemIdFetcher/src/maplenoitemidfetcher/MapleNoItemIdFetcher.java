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
package maplenoitemidfetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author RonanLana
 * 
 * This application finds inexistent itemids within the drop data from
 * the Maplestory database specified by the URL below. This program
 * assumes all itemids uses 7 digits.
 * 
 * A file is generated listing all the inexistent ids.
 */
public class MapleNoItemIdFetcher {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String wzPath = "../../wz";
    static String newFile = "lib/result.txt";

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    static int itemId = -1;
    
    static Set<Integer> existingIds = new HashSet<>();
    static Set<Integer> nonExistingIds = new HashSet<>();

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[100];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return(d);
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
            if(status == 1) {           //getting ItemId
                d = getName(token);
                itemId = Integer.parseInt(d.substring(1, 8));
                
                existingIds.add(itemId);
                forwardCursor(status);
            }

            status += 1;
        }
    }

    private static void readItemDataFile(File file) {
        // This will reference one line at a time
        String line = null;

        try {            
            fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);
            
            status = 0;
            try {
                while((line = bufferedReader.readLine()) != null) {
                    translateToken(line);
                }
            } catch(NumberFormatException npe) {
                // second criteria, itemid is on the name of the file
                
                try {
                    itemId = Integer.parseInt(file.getName().substring(0, 7));
                    existingIds.add(itemId);
                } catch(NumberFormatException npe2) {}
            }

            bufferedReader.close();
            fileReader.close();
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file.getName() + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + file.getName() + "'");
        }
    }
    
    private static void readEquipDataDirectory(String dirPath) {
        File[] folders = new File(dirPath).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        for (File folder : folders) {   // enter all subfolders
            if (folder.isDirectory()) {
                System.out.println("Reading '" + dirPath + "/" + folder.getName() + "'...");
            
                try {
                    File[] files = folder.listFiles();
                    
                    for (File file : files) {   // enter all XML files under subfolders
                        if (file.isFile()) {
                            itemId = Integer.parseInt(file.getName().substring(0, 8));
                            existingIds.add(itemId);
                        }
                    }
                } catch (NumberFormatException nfe) {}
            }
        }
    }
    
    private static void readItemDataDirectory(String dirPath) {
        File[] folders = new File(dirPath).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        for (File folder : folders) {   // enter all subfolders
            if (folder.isDirectory()) {
                System.out.println("Reading '" + dirPath + "/" + folder.getName() + "'...");
            
                File[] files = folder.listFiles();
                
                for (File file : files) {   // enter all XML files under subfolders
                    if (file.isFile()) {
                        readItemDataFile(file);
                    }
                }
            }
        }
    }
    
    private static void evaluateDropsFromTable(String table) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT DISTINCT itemid FROM " + table + ";");
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            if(!existingIds.contains(rs.getInt(1))) {
                nonExistingIds.add(rs.getInt(1));
            }
        }

        rs.close();
        ps.close();
    }
    
    private static void evaluateDropsFromDb() {
        try {
            System.out.println("Evaluating item data on DB...");
            
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(host, username, password);
            
            evaluateDropsFromTable("drop_data");
            evaluateDropsFromTable("reactordrops");
            
            if(!nonExistingIds.isEmpty()) {
                List<Integer> list = new ArrayList<>(nonExistingIds);
                Collections.sort(list);
                
                for(Integer i : list) {
                    printWriter.println(i);
                }
            }
            
            System.out.println("Inexistent itemid count: " + nonExistingIds.size());
            System.out.println("Total itemid count: " + existingIds.size());
            
            con.close();
        }
        
        catch(ClassNotFoundException e) {
            System.out.println("Error: could not find class");
            System.out.println(e.getMessage());
        }

        catch(InstantiationException e) {
            System.out.println("Error: instantiation failure");
            System.out.println(e.getMessage());
        }
        
        catch(SQLException e) {
            e.printStackTrace();
        }
        
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            printWriter = new PrintWriter(newFile, "UTF-8");
        
            existingIds.add(0); // meso itemid
            readEquipDataDirectory(wzPath + "/Character.wz");
            readItemDataDirectory(wzPath + "/Item.wz");
            
            evaluateDropsFromDb();
            
            printWriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
