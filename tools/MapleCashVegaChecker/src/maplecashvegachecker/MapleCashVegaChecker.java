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
package maplecashvegachecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author RonanLana
 * 
   This application main objective is to read Vega-related information from
   the item's description report back missing nodes for these items.
  
   Estimated parse time: 10 seconds
 */
public class MapleCashVegaChecker {
    
    private static String wzPath = "../../wz";
    
    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static int initialStringLength = 1000;
    static int currentItem;
    
    static byte status = 0;
    
    static Set<Integer> vegaItems = new HashSet<>();
    
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

        i = token.lastIndexOf("value=");
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
    
    private static void translateItemToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if (status == 2) {
                currentItem = Integer.valueOf(getName(token));
            }
        } else {
            if (status == 2) {
                if (getValue(token).endsWith("Vega&apos;s Spell.")) {
                    vegaItems.add(currentItem);
                }
            }
        }
    }
    
    private static void translateVegaToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
        } else {
            if (status == 2) {
                if (getName(token).contentEquals("item")) {
                    vegaItems.remove(Integer.valueOf(getValue(token)));
                }
            }
        }
    }
    
    private static void readItemDescriptionFile(File f) {
        System.out.print("Reading String.wz... ");
        try {
            fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine())!=null){
                translateItemToken(line);
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(vegaItems.size() + " Vega Scroll items found");
    }
    
    private static void readVegaDescriptionFile(File f) {
        System.out.println("Reading Etc.wz...");
        try {
            fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine())!=null){
                translateVegaToken(line);
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleCashVegaChecker feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void reportMissingVegaItems() {
        System.out.println("Reporting results ...");
        
        try {
            printWriter = new PrintWriter("lib/result.txt", "UTF-8");
        
            printReportFileHeader();

            for (Integer itemid : vegaItems) {
                printWriter.println("  " + itemid);
            }
            
            printWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {
        
        readItemDescriptionFile(new File(wzPath + "/String.wz/Consume.img.xml"));
        readVegaDescriptionFile(new File(wzPath + "/Etc.wz/VegaSpell.img.xml"));
        
        reportMissingVegaItems();
    }
    
}
