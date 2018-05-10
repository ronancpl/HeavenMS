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
package mapleskillmakerreagentindexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author RonanLana
 * 
 * The main objective of this program is to index relevant reagent data
 * from the Item.wz folder and generate a SQL table with them, to be used
 * by the server source.
 * 
 */
public class MapleSkillMakerReagentIndexer {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String fileName = "../../wz/Item.wz/Etc/0425.img.xml";
    static String newFile = "lib/MakerReagentData.sql";

    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    
    static int id = -1;
    static List<Pair<Integer, Pair<String, Integer>>> reagentList = new ArrayList<>();
    
    static int initialStringLength = 50;
    
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

    private static void simpleToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
        }
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
    
    private static void translateToken(String token) {
        String d;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting id
                d = getName(token);
                id = Integer.parseInt(d);
                System.out.println("Parsing maker reagent id " + id);
            } else if(status == 2) {
                d = getName(token);
                if(!d.equals("info")) {
                    System.out.println("not info");
                    forwardCursor(status);
                }
            }
            
            status += 1;
        } else {
            if(status == 3) {
                if(token.contains("int")) {
                    d = getName(token);
                    
                    if(d.contains("inc") || d.contains("rand")) {
                        Integer v = Integer.valueOf(getValue(token));
                        Pair<String, Integer> reagBuff = new Pair<>(d, v);
                        
                        Pair<Integer, Pair<String, Integer>> reagItem = new Pair<>(id, reagBuff);
                        reagentList.add(reagItem);
                    }
                } else {
                    if(token.contains("canvas")) {
                        forwardCursor(status + 1);
                    }
                }
            }
        }
    }
    
    private static void SortReagentList() {
        Collections.sort(reagentList, new Comparator<Pair<Integer, Pair<String, Integer>>>() {
            @Override
            public int compare(Pair<Integer, Pair<String, Integer>> p1, Pair<Integer, Pair<String, Integer>> p2) {
                return p1.getLeft().compareTo(p2.getLeft());
            }
        });
    }
    
    private static void WriteMakerReagentTableFile() {
        printWriter.println(" # SQL File autogenerated from the MapleSkillMakerReagentIndexer feature by Ronan Lana.");
        printWriter.println(" # Generated data is conformant with the Item.wz folder used to compile this.");
        printWriter.println();
        
        printWriter.println("CREATE TABLE IF NOT EXISTS `makerreagentdata` (");
        printWriter.println("  `itemid` int(11) NOT NULL,");
        printWriter.println("  `stat` varchar(20) NOT NULL,");
        printWriter.println("  `value` smallint(6) NOT NULL,");
        printWriter.println("  PRIMARY KEY (`itemid`)");
        printWriter.println(") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        printWriter.println();
        
        StringBuilder sb = new StringBuilder("INSERT IGNORE INTO `makerreagentdata` (`itemid`, `stat`, `value`) VALUES\r\n");
        
        for(Pair<Integer, Pair<String, Integer>> it : reagentList) {
            sb.append("  (" + it.left + ", \"" + it.right.left + "\", " + it.right.right + "),\r\n");
        }
        
        sb.setLength(sb.length() - 3);
        sb.append(";");
        
        printWriter.println(sb);
    }

    private static void WriteMakerReagentTableData() {
        // This will reference one line at a time
        String line = null;

        try {
            fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);
            
            while((line = bufferedReader.readLine()) != null) {
                translateToken(line);
            }
            
            bufferedReader.close();
            fileReader.close();
            
            SortReagentList();
            
            printWriter = new PrintWriter(newFile, "UTF-8");
            WriteMakerReagentTableFile();
            printWriter.close();
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WriteMakerReagentTableData();
    }
}
