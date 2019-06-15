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
package maplegachaponitemidretriever;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.io.BufferedReader;
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
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author RonanLana
 * 
 * This application reads metadata for the gachapons found on the "gachapon_items.txt"
 * recipe file, then checks up the Handbook DB (installed through MapleIdRetriever)
 * and translates the item names from the recipe file into their respective itemids.
 * The translated itemids are then stored in specific gachapon files inside the
 * "lib/gachapons" folder.
 *
 * Estimated parse time: 1 minute
 */
public class MapleGachaponItemidRetriever {

    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";
    
    static Connection con = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static PrintWriter printWriter = null;
    
    // ------- SET-UP section arguments --------
    
    static String directoryName = "./handbook/";
    
    // ------- SEARCH section arguments --------
    
    static String inputName = "lib/gachapon_items.txt";
    static String outputPath = "lib/gachapons/";
    
    static Pattern p = Pattern.compile("(\\d*)%");
    static int[] scrollsChances = new int[]{10, 15, 30, 60, 65, 70, 100};
    
    static Map<GachaponScroll, List<Integer>> scrollItemids = new HashMap<>();
    
    private static void insertGachaponScrollItemid(Integer id, String name, String description, boolean both) {
        GachaponScroll gachaScroll = getGachaponScroll(name, description, both);

        List<Integer> list = scrollItemids.get(gachaScroll);
        if (list == null) {
            list = new LinkedList<>();
            scrollItemids.put(gachaScroll, list);
        }

        list.add(id);
    }
    
    private static void loadHandbookUseNames() throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM `handbook` WHERE `id` >= 2040000 AND `id` < 2050000 ORDER BY `id` ASC;");
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            Integer id = rs.getInt("id");
            String name = rs.getString("name");
            
            if (isUpgradeScroll(name)) {
                String description = rs.getString("description");
                insertGachaponScrollItemid(id, name, description, false);
                insertGachaponScrollItemid(id, name, description, true);
            }
        }
        
        rs.close();
        ps.close();
        
        /*
        for (Entry<GachaponScroll, List<Integer>> e : scrollItemids.entrySet()) {
            System.out.println(e);
        }
        System.out.println("------------");
        */
    }
    
    private static class GachaponScroll {
        private String header;
        private String target;
        private String buff;
        private int prop;
        
        private GachaponScroll(GachaponScroll from, int prop) {
            this.header = from.header;
            this.target = from.target;
            this.buff = from.buff;
            this.prop = prop;
        }
        
        private GachaponScroll(String name, String description, boolean both) {
            String[] params = name.split(" for ");
            if (params.length < 3) {
                return;
            }
            
            String header = both ? "scroll" : " " + params[0];
            String target = params[1];

            int prop = 0;
            String buff = params[2];

            Matcher m = p.matcher(buff);
            if (m.find()) {
                prop = Integer.valueOf(m.group(1));
                buff = buff.substring(0, m.start() - 1).trim();
            } else {
                m = p.matcher(description);
                
                if (m.find()) {
                    prop = Integer.valueOf(m.group(1));
                }
            }

            int idx = buff.indexOf(" (");   // remove percentage & dots from name checking
            if (idx > -1) {
                buff = buff.substring(0, idx);
            }
            buff = buff.replace(".", "");
            
            this.header = header;
            this.target = target;
            this.buff = buff;
            this.prop = prop;
        }
        
        @Override    
        public int hashCode() {
            int result = (int) (prop ^ (prop >>> 32));
            result = 31 * result + (header != null ? header.hashCode() : 0);        
            result = 31 * result + (target != null ? target.hashCode() : 0);
            result = 31 * result + (buff != null ? buff.hashCode() : 0);
            return result;    
        }
        
        @Override    
        public boolean equals(Object o) {        
            if (this == o) return true;        
            if (o == null || getClass() != o.getClass()) return false;        
            GachaponScroll sc = (GachaponScroll) o;        
            if (header != null ? !header.equals(sc.header) : sc.header != null) return false;        
            if (target != null ? !target.equals(sc.target) : sc.target != null) return false;        
            if (buff != null ? !buff.equals(sc.buff) : sc.buff != null) return false;
            if (prop != sc.prop) return false;
            return true;
        }
        
        @Override
        public String toString() {
            return header + " for " + target + " for " + buff + " - " + prop + "%";
        }
        
    }
    
    private static String getGachaponScrollResults(String line, boolean both) {
        String str = "";
        List<GachaponScroll> gachaScrollList;
            
        GachaponScroll gachaScroll = getGachaponScroll(line, "", both);
        if (gachaScroll.prop != 0) {
            gachaScrollList = Collections.singletonList(gachaScroll);
        } else {
            gachaScrollList = new ArrayList<>(scrollsChances.length);

            for (int prop : scrollsChances) {
                gachaScrollList.add(new GachaponScroll(gachaScroll, prop));
            }
        }
        
        for (GachaponScroll gs : gachaScrollList) {
            List<Integer> gachaItemids = scrollItemids.get(gs);
            if (gachaItemids != null) {
                String listStr = "";
                for (Integer id : gachaItemids) {
                    listStr += id.toString();
                    listStr += " ";
                }

                if (gachaItemids.size() > 1) {
                    str += "[" + listStr + "]";
                } else {
                    str += listStr;
                }
            }
        }
        
        return str;
    }
    
    private static GachaponScroll getGachaponScroll(String name, String description, boolean both) {
        name = name.toLowerCase();
        name = name.replace("for acc ", "for accuracy ");
        name = name.replace("blunt weapon", "bw");
        name = name.replace("eye eqp.", "eye accessory");
        name = name.replace("face eqp.", "face accessory");
        name = name.replace("for attack", "for att");
        name = name.replace("1-handed", "one-handed");
        name = name.replace("2-handed", "two-handed");
        
        return new GachaponScroll(name, description, both);
    }
    
    private static boolean isUpgradeScroll(String name) {
        return name.matches("^(([D|d]ark )?[S|s]croll for).*");
    }
    
    private static void fetchLineOnMapleHandbook(String line, String rarity) throws SQLException {
        String str = "";
        if (!isUpgradeScroll(line)) {
            PreparedStatement ps = con.prepareStatement("SELECT `id` FROM `handbook` WHERE `name` LIKE ? COLLATE latin1_general_ci ORDER BY `id` ASC;");
            ps.setString(1, line);

            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Integer id = rs.getInt("id");

                str += id.toString();
                str += " ";
            }

            rs.close();
            ps.close();
        } else {
            str += getGachaponScrollResults(line, false);
            if (str.isEmpty()) {
                str += getGachaponScrollResults(line, true);
                
                if (str.isEmpty()) {
                    System.out.println("NONE for '" + line + "' : " + getGachaponScroll(line, "", false));
                }
            }
        }
        
        if (str.isEmpty()) {
            str += line;
        }
        
        if (rarity != null) {
            str += ("- " + rarity);
        }

        printWriter.println(str);
    }
    
    private static void fetchDataOnMapleHandbook() throws SQLException {
        String line;
        
        try {
            fileReader = new InputStreamReader(new FileInputStream(inputName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);
            
            int skip = 0;
            boolean lineHeader = false;
            while((line = bufferedReader.readLine()) != null) {
                if (skip > 0) {
                    skip--;
                    
                    if (lineHeader) {
                        if (!line.isEmpty()) {
                            lineHeader = false;
                            printWriter.println();
                            printWriter.println(line + ":");
                        }
                    }
                } else if (line.isEmpty()) {
                    printWriter.println("");
                } else if (line.startsWith("Gachapon ")) {
                    String s[] = line.split("� ");
                    String gachaponName = s[s.length - 1];
                    gachaponName = gachaponName.replace(" ", "_");
                    gachaponName = gachaponName.toLowerCase();
                    
                    if (printWriter != null) printWriter.close();
                    printWriter = new PrintWriter(outputPath + gachaponName + ".txt", "UTF-8");
                    
                    skip = 2;
                    lineHeader = true;
                } else if (line.startsWith(".")) {
                    skip = 1;
                    lineHeader = true;
                } else {
                    line = line.replace("�", "'");
                    for (String item : line.split("\\s\\|\\s")) {
                        item = item.trim();
                        if (!item.contentEquals("n/a")) {
                            String[] itemInfo = item.split(" - ");
                            fetchLineOnMapleHandbook(itemInfo[0], itemInfo.length > 1 ? itemInfo[1] : null);
                        }
                    }
                }
            }

            if (printWriter != null) printWriter.close();
            bufferedReader.close();
            fileReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(host, username, password);
            
            loadHandbookUseNames();
            fetchDataOnMapleHandbook();

            con.close();
        }
        
        catch(SQLException e) {
            System.out.println("Error: invalid SQL syntax");
            System.out.println(e.getMessage());
        }
        
        catch(ClassNotFoundException e) {
            System.out.println("Error: could not find class");
            System.out.println(e.getMessage());
        }

        catch(InstantiationException | IllegalAccessException e) {
            System.out.println("Error: instantiation failure");
            System.out.println(e.getMessage());
        }
    }
    
}
