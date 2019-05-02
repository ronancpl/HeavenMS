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
package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.DatabaseConnection;

/**
 *
 * @author RonanLana
 */
public class MapleSkillbookInformationProvider {
    private final static MapleSkillbookInformationProvider instance = new MapleSkillbookInformationProvider();
    
    public static MapleSkillbookInformationProvider getInstance() {
        return instance;
    }
    
    protected static Map<Integer, SkillBookEntry> foundSkillbooks = new HashMap<>();
    
    public enum SkillBookEntry {
        UNAVAILABLE,
        QUEST,
        REACTOR,
        SCRIPT
    }
    
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String wzPath = "wz";
    static String rootDirectory = ".";
    
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialStringLength = 50;
    
    static int skillbookMinItemid = 2280000;
    static int skillbookMaxItemid = 2300000;  // exclusively
    
    static byte status = 0;
    static int questId = -1;
    static int isCompleteState = 0;
    
    static int currentItemid = 0;
    static int currentCount = 0;
    
    static {
        loadSkillbooks();
    }

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;
        
        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        if(j < i) {           //node value containing 'name' in it's scope, cheap fix since we don't deal with strings anyway
            System.out.println("[CRITICAL] Found this '" + token + "'");
            return "0";
        }
        
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
        else if(token.contains("imgdir") && !token.endsWith("/>")) {    // '\>' XML node description not being accounted, issue found thanks to Robin Schulz, CanIGetaPR
            status += 1;
        }
    }
    
    private static void inspectQuestItemList(int st) {
        String line = null;

        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                readItemToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSkillBook(int itemid) {
        return itemid >= skillbookMinItemid && itemid < skillbookMaxItemid;
    }
    
    private static void processCurrentItem() {
        try {
            if(isSkillBook(currentItemid)) {
                if(currentCount > 0) {
                    foundSkillbooks.put(currentItemid, SkillBookEntry.QUEST);
                }
            }
        } catch(Exception e) {}
    }
    
    private static void readItemToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            processCurrentItem();
            
            currentItemid = 0;
            currentCount = 0;
        }
        else if(token.contains("imgdir") && !token.endsWith("/>")) {
            status += 1;
        }
        else {
            String d = getName(token);
            
            if(d.equals("id")) {
                currentItemid = Integer.parseInt(getValue(token));
            } else if(d.equals("count")) {
                currentCount = Integer.parseInt(getValue(token));
            }
        }
    }

    private static void translateActToken(String token) {
        String d;
        int temp;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir") && !token.endsWith("/>")) {
            if(status == 1) {           //getting QuestId
                d = getName(token);
                questId = Integer.parseInt(d);
            }
            else if(status == 2) {      //start/complete
                d = getName(token);
                isCompleteState = Integer.parseInt(d);
            }
            else if(status == 3) {
                d = getName(token);

                if(d.contains("item")) {
                    temp = status;
                    inspectQuestItemList(temp);
                } else {
                    forwardCursor(status);
                }
            }

            status += 1;
        }
    }
    
    private static void fetchSkillbooksFromQuests() {
        String line = "";
        int lineNumber = 0; // add line number, thanks to Alex (CanIGetaPR)
        
        try {
            fileReader = new InputStreamReader(new FileInputStream(wzPath + "/Quest.wz/Act.img.xml"), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                translateActToken(line);
            }

            bufferedReader.close();
            fileReader.close();
        } catch(IOException ioe) {
            System.out.println("Failed to read Quest.wz file. Line " + lineNumber + ": " + line);
            ioe.printStackTrace();
        }
    }
    
    private static void fetchSkillbooksFromReactors() {
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            
            PreparedStatement ps = con.prepareStatement("SELECT itemid FROM reactordrops WHERE itemid >= ? AND itemid < ?;");
            ps.setInt(1, skillbookMinItemid);
            ps.setInt(2, skillbookMaxItemid);
            ResultSet rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {
                while(rs.next()) {
                    foundSkillbooks.put(rs.getInt("itemid"), SkillBookEntry.REACTOR);
                }
            }

            rs.close();
            ps.close();
            
            con.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
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
    
    private static List<File> listFilesFromDirectoryRecursively(String directory) {
        ArrayList<File> files = new ArrayList<>();
        listFiles(directory, files);
        
        return files;
    }
    
    private static void filterScriptDirectorySearchMatchingData(String path) {
        for (File file : listFilesFromDirectoryRecursively(rootDirectory + "/" + path)) {
            if (file.getName().endsWith(".js")) {
                fileSearchMatchingData(file);
            }
        }
    }
    
    private static Set<Integer> foundMatchingDataOnFile(String fileContent) {
        Set<Integer> matches = new HashSet<>(4);
        
        Matcher searchM = Pattern.compile("22(8|9)[0-9]{4}").matcher(fileContent);
        int idx = 0;
        while (searchM.find(idx)) {
            idx = searchM.end();
            matches.add(Integer.valueOf(fileContent.substring(searchM.start(), idx)));
        }
        
        return matches;
    }
    
    static String readFileToString(File file, String encoding) throws IOException {
        Scanner scanner = new Scanner(file, encoding);
        String text = "";
        try {
            try {
                text = scanner.useDelimiter("\\A").next();
            } finally {
                scanner.close();
            }
        } catch (NoSuchElementException e) {}
        
        return text;
    }
    
    private static void fileSearchMatchingData(File file) {
        try {
            String fileContent = readFileToString(file, "UTF-8");
            
            Set<Integer> books = foundMatchingDataOnFile(fileContent);
            for (Integer i : books) {
                foundSkillbooks.put(i, SkillBookEntry.SCRIPT);
            }
        } catch (IOException ioe) {
            System.out.println("Failed to read " + file.getName() + ".");
            ioe.printStackTrace();
        }
    }
    
    private static void fetchSkillbooksFromScripts() {
        filterScriptDirectorySearchMatchingData("scripts");
    }
    
    private static void loadSkillbooks() {
        fetchSkillbooksFromQuests();
        fetchSkillbooksFromReactors();
        fetchSkillbooksFromScripts();
    }
    
    public SkillBookEntry getSkillbookAvailability(int itemid) {
        SkillBookEntry sbe = foundSkillbooks.get(itemid);
        return sbe != null ? sbe : SkillBookEntry.UNAVAILABLE;
    }
    
}
