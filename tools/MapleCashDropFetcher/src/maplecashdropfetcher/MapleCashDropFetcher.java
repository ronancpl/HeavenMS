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
package maplecashdropfetcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import java.io.File;

import tools.Pair;

/**
 *
 * @author RonanLana
 
 This application gets info from the WZ.XML files regarding cash itemids then searches the drop data on the DB
 after any NX (cash item) drops and reports them.
 
 Estimated parse time: 2 minutes
 */
public class MapleCashDropFetcher {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String wzPath = "../../wz";
    
    static String directoryName = "../..";
    static String newFile = "lib/CashDropReport.txt";

    static Connection con = null;
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialLength = 200;
    static int initialStringLength = 50;
    static int itemFileNameSize = 13;
    
    static Set<Integer> nxItems = new HashSet<>();
    static Set<Integer> nxDrops = new HashSet<>();
    
    static byte status = 0;
    static int currentItemid = 0;

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
    }
    
    
    private static void inspectEquipWzEntry() {
        String line = null;

        try {
            while((line = bufferedReader.readLine()) != null) {
                translateEquipToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateEquipToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {
                if(!getName(token).equals("info")) {
                    forwardCursor(status);
                }
            }
            
            status += 1;
        }
        else {
            if(status == 2) {
                String d = getName(token);
            
                if(d.equals("cash")) {
                    if(!getValue(token).equals("0")) {
                        nxItems.add(currentItemid);
                    }
                    
                    forwardCursor(status);
                }
            }
        }
    }
    
    private static void inspectItemWzEntry() {
        String line = null;

        try {
            while((line = bufferedReader.readLine()) != null) {
                translateItemToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateItemToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {
                currentItemid = Integer.valueOf(getName(token));
            }
            else if(status == 2) {
                if(!getName(token).equals("info")) {
                    forwardCursor(status);
                }
            }
            
            status += 1;
        }
        else {
            if(status == 3) {
                String d = getName(token);
            
                if(d.equals("cash")) {
                    if(!getValue(token).equals("0")) {
                        nxItems.add(currentItemid);
                    }
                    
                    forwardCursor(status);
                }
            }
        }
    }

    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleCashDropFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the underlying DB and the server-side WZ.xmls.");
        printWriter.println();
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
    
    private static int getItemIdFromFilename(String name) {
        try {
            return Integer.valueOf(name.substring(0, name.indexOf('.')));
        } catch(Exception e) {
            return -1;
        }
    }
    
    private static String getDropTableName(boolean dropdata) {
        return (dropdata ? "drop_data" : "reactordrops");
    }
    
    private static String getDropElementName(boolean dropdata) {
        return (dropdata ? "dropperid" : "reactorid");
    }
    
    private static void filterNxDropsOnDB(boolean dropdata) throws SQLException {
        nxDrops.clear();
        
        PreparedStatement ps = con.prepareStatement("SELECT DISTINCT itemid FROM " + getDropTableName(dropdata));
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            int itemid = rs.getInt("itemid");
            
            if(nxItems.contains(itemid)) {
                nxDrops.add(itemid);
            }
        }
        
        rs.close();
        ps.close();
    }
    
    private static List<Pair<Integer, Integer>> getNxDropsEntries(boolean dropdata) throws SQLException {
        List<Pair<Integer, Integer>> entries = new ArrayList<>();
        
        List<Integer> sortedNxDrops = new ArrayList<>(nxDrops);
        Collections.sort(sortedNxDrops);
        
        for(Integer nx : sortedNxDrops) {
            PreparedStatement ps = con.prepareStatement("SELECT " + getDropElementName(dropdata) + " FROM " + getDropTableName(dropdata) + " WHERE itemid = ?");
            ps.setInt(1, nx);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                entries.add(new Pair<>(nx, rs.getInt(getDropElementName(dropdata))));
            }
            
            rs.close();
            ps.close();
        }
        
        return entries;
    }
    
    private static void reportNxDropResults(boolean dropdata) throws SQLException {
        filterNxDropsOnDB(dropdata);
        
        if(!nxDrops.isEmpty()) {
            List<Pair<Integer, Integer>> nxEntries = getNxDropsEntries(dropdata);

            printWriter.println("NX DROPS ON " + getDropTableName(dropdata));
            for(Pair<Integer, Integer> nx : nxEntries) {
                printWriter.println(nx.left + " : " + nx.right);
            }
            printWriter.println("\n\n\n");
        }
    }
    
    private static void ReportNxDropData() {
        try {
            Class.forName(driver).newInstance();
            
            System.out.println("Reading Character.wz ...");
            ArrayList<File> files = new ArrayList<>();
            listFiles(wzPath + "/Character.wz", files);
            
            for(File f : files) {
                //System.out.println("Parsing " + f.getAbsolutePath());
                int itemid = getItemIdFromFilename(f.getName());
                if(itemid < 0) {
                    continue;
                }
                
                fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                bufferedReader = new BufferedReader(fileReader);

                currentItemid = itemid;
                inspectEquipWzEntry();

                bufferedReader.close();
                fileReader.close();
            }
            
            System.out.println("Reading Item.wz ...");
            files = new ArrayList<>();
            listFiles(wzPath + "/Item.wz", files);
            
            for(File f : files) {
                //System.out.println("Parsing " + f.getAbsolutePath());
                fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                bufferedReader = new BufferedReader(fileReader);

                if(f.getName().length() <= itemFileNameSize) {
                    inspectItemWzEntry();
                } else {    // pet file structure is similar to equips, maybe there are other item-types following this behaviour?
                    int itemid = getItemIdFromFilename(f.getName());
                    if(itemid < 0) {
                        continue;
                    }
                    
                    currentItemid = itemid;
                    inspectEquipWzEntry();
                }

                bufferedReader.close();
                fileReader.close();
            }
            
            System.out.println("Reporting results...");
            
            // filter drop data on DB
            con = DriverManager.getConnection(host, username, password);
            
            // report suspects of missing quest drop data, as well as those drop data that may have incorrect questids.
            printWriter = new PrintWriter(newFile, "UTF-8");
            printReportFileHeader();
            
            reportNxDropResults(true);
            reportNxDropResults(false);
            
            /*
            printWriter.println("NX LIST");     // list of all cash items found
            for(Integer nx : nxItems) {
                printWriter.println(nx);
            }
            */
            
            con.close();
            printWriter.close();
            System.out.println("Done!");
        }

        catch(SQLException e) {
            System.out.println("Warning: Could not establish connection to database to report quest data.");
            System.out.println(e.getMessage());
        }

        catch(ClassNotFoundException e) {
            System.out.println("Error: could not find class");
            System.out.println(e.getMessage());
        }

        catch(InstantiationException e) {
            System.out.println("Error: instantiation failure");
            System.out.println(e.getMessage());
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        ReportNxDropData();
    }
    
}
