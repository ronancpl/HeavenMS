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
package maplecodecoupongenerator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author RonanLana
 
 This application parses the coupon descriptor XML file and automatically generates
 code entries on the DB reflecting the descriptions found. Parse time relies on the
 sum of coupon codes created and amount of current codes on DB.
 
 Estimated parse time: 2 minutes (for 100 code entries)
 */
public class MapleCodeCouponGenerator {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";
    
    static Connection con = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static String fileName = "lib/CouponCodes.img.xml";
    static long currentTime;
    
    static int initialStringLength = 250;
    
    static String name;
    static boolean active;
    static int quantity, duration;
    static int maplePoint, nxCredit, nxPrepaid;
    
    static List<Pair<Integer, Integer>> itemList = new ArrayList<>();
    static Pair<Integer, Integer> item;
    
    
    static List<CodeCouponDescriptor> activeCoupons = new ArrayList<>();
    static List<Integer> generatedKeys;
    static Set<String> usedCodes = new HashSet<>();
    
    static byte status;
    
    private static void resetCouponPackage() {
        name = null;
        active = false;
        quantity = 1;
        duration = 7;
        maplePoint = 0;
        nxCredit = 0;
        nxPrepaid = 0;
        itemList.clear();
    }
    
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
    
    private static void translateToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
            
            if (status == 1) {
                if (active) {
                    activeCoupons.add(new CodeCouponDescriptor(name, quantity, duration, maplePoint, nxCredit, nxPrepaid, itemList));
                }
                
                resetCouponPackage();
            } else if (status == 3) {
                itemList.add(item);
            }
        }
        else if(token.contains("imgdir")) {
            status += 1;
            
            if (status == 4) {
                item = new Pair<>(-1, -1);
            } else if (status == 2) {
                String d = getName(token);
                
                System.out.println("  Reading coupon '" + d + "'");
                name = d;
            }
        }
        else {
            String d = getName(token);
            
            if (status == 2) {
                switch (d) {
                    case "active":
                        if (Integer.valueOf(getValue(token)) == 0) {
                            forwardCursor(status);
                            resetCouponPackage();
                        } else {
                            active = true;
                        }
                        break;

                    case "quantity":
                        quantity = Integer.valueOf(getValue(token));
                        break;
                    case "duration":
                        duration = Integer.valueOf(getValue(token));
                        break;
                    case "maplePoint":
                        maplePoint = Integer.valueOf(getValue(token));
                        break;
                    case "nxCredit":
                        nxCredit = Integer.valueOf(getValue(token));
                        break;
                    case "nxPrepaid":
                        nxPrepaid = Integer.valueOf(getValue(token));
                        break;
                }
            } else if (status == 4) {
                switch (d) {
                    case "count":
                        item.right = Integer.valueOf(getValue(token));
                        break;
                    case "id":
                        item.left = Integer.valueOf(getValue(token));
                        break;
                }
            }
        }
    }
    
    private static class CodeCouponDescriptor {
        protected String name;
        protected int quantity, duration;
        protected int nxCredit, maplePoint, nxPrepaid;
        protected List<Pair<Integer, Integer>> itemList;
        
        protected CodeCouponDescriptor(String name, int quantity, int duration, int maplePoint, int nxCredit, int nxPrepaid, List<Pair<Integer, Integer>> itemList) {
            this.name = name;
            this.quantity = quantity;
            this.duration = duration;
            this.maplePoint = maplePoint;
            this.nxCredit = nxCredit;
            this.nxPrepaid = nxPrepaid;
            
            this.itemList = new ArrayList<>(itemList);
        }
    }
    
    private static String randomizeCouponCode() {
        StringBuilder rnd = new StringBuilder(Long.toHexString(Double.doubleToLongBits(Math.random())));
        rnd.setCharAt(5, '-');
        rnd.insert(11, '-');
        return rnd.toString();
    }
    
    private static String generateCouponCode() {
        String newCode;
        do {
            newCode = randomizeCouponCode();
        } while (usedCodes.contains(newCode));
        
        usedCodes.add(newCode);
        return newCode;
    }
    
    private static List<Integer> getGeneratedKeys(PreparedStatement ps) throws SQLException {
        if (generatedKeys == null) {
            generatedKeys = new ArrayList<>();
            
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                generatedKeys.add(rs.getInt(1));
            }
            rs.close();
        }
        
        return generatedKeys;
    }
    
    private static void commitCodeCouponDescription(CodeCouponDescriptor recipe) throws SQLException {
        if (recipe.quantity < 1) return;
        
        System.out.println("  Generating coupon '" + recipe.name + "'");
        generatedKeys = null;
        
        PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO `nxcode` (`code`, `expiration`) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setLong(2, currentTime + (recipe.duration * 60 * 60 * 1000));
        
        for(int i = 0; i < recipe.quantity; i++) {
            ps.setString(1, generateCouponCode());
            ps.addBatch();
        }
        ps.executeBatch();
        
        PreparedStatement ps2 = con.prepareStatement("INSERT IGNORE INTO `nxcode_items` (`codeid`, `type`, `item`, `quantity`) VALUES (?, ?, ?, ?)");
        if (!recipe.itemList.isEmpty()) {
            ps2.setInt(2, 5);
            List<Integer> keys = getGeneratedKeys(ps);
            
            for (Pair<Integer, Integer> p : recipe.itemList) {
                ps2.setInt(3, p.getLeft());
                ps2.setInt(4, p.getRight());
                
                for (Integer codeid : keys) {
                    ps2.setInt(1, codeid);
                    ps2.addBatch();
                }
            }
        }
        
        ps2.setInt(4, 0);
        if (recipe.nxCredit > 0) {
            ps2.setInt(2, 0);
            ps2.setInt(3, recipe.nxCredit);
            List<Integer> keys = getGeneratedKeys(ps);
            
            for(Integer codeid : keys) {
                ps2.setInt(1, codeid);
                ps2.addBatch();
            }
        }
        
        if (recipe.maplePoint > 0) {
            ps2.setInt(2, 1);
            ps2.setInt(3, recipe.maplePoint);
            List<Integer> keys = getGeneratedKeys(ps);
            
            for(Integer codeid : keys) {
                ps2.setInt(1, codeid);
                ps2.addBatch();
            }
        }
        
        if (recipe.nxPrepaid > 0) {
            ps2.setInt(2, 2);
            ps2.setInt(3, recipe.nxPrepaid);
            List<Integer> keys = getGeneratedKeys(ps);
            
            for(Integer codeid : keys) {
                ps2.setInt(1, codeid);
                ps2.addBatch();
            }
        }
        
        ps2.executeBatch();
        ps2.close();
        ps.close();
    }
    
    private static void loadUsedCouponCodes() throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT code FROM nxcode", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            usedCodes.add(rs.getString("code"));
        }
        rs.close();
        ps.close();
    }
    
    private static void generateCodeCoupons(String fileName) throws IOException {
        fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
        bufferedReader = new BufferedReader(fileReader);
        
        resetCouponPackage();
        status = 0;
        
        System.out.println("Reading XML coupon information...");
        String line;
        while((line = bufferedReader.readLine()) != null) {
            translateToken(line);
        }

        bufferedReader.close();
        fileReader.close();
        System.out.println();
        
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(host, username, password);
            
            System.out.println("Loading DB coupon codes...");
            loadUsedCouponCodes();
            System.out.println();
            
            System.out.println("Saving generated coupons...");
            currentTime = System.currentTimeMillis();
            for (CodeCouponDescriptor ccd : activeCoupons) {
                commitCodeCouponDescription(ccd);
            }
            System.out.println();
            
            con.close();
            System.out.println("Done.");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            generateCodeCoupons(fileName);
        } catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
    }
}
