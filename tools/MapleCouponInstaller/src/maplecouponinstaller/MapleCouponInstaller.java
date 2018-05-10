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
package maplecouponinstaller;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author RonanLana
 * 
 * This application gathers information about the Cash Shop's EXP & DROP coupons,
 * such as applied rates, active times of day and days of week and dumps them in
 * a SQL table, in which will be used by the server.
 * 
 */
public class MapleCouponInstaller {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";
    
    static Connection con = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    static int itemId = -1, itemMultiplier = 1, startHour = -1, endHour = -1, activeDay = 0;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        if(i < 0) return "";
        
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[8];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return(d);
    }
    
    private static String getNodeValue(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("value=");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound
        
        if(j - i < 1) return "";
        
        dest = new char[j - i];
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
    
    private static int getDayOfWeek(String day) {
        switch(day) {
            case "SUN":
                return 1;
                
            case "MON":
                return 2;
                    
            case "TUE":
                return 3;
                        
            case "WED":
                return 4;
                            
            case "THU":
                return 5;
                                
            case "FRI":
                return 6;
                                    
            case "SAT":
                return 7;
                
            default:
                return 0;
        }
    }
    
    private static void processHourTimeString(String time) {
        startHour = Integer.parseInt(time.substring(4, 6));
        endHour = Integer.parseInt(time.substring(7, 9));
    }
    
    private static void processDayTimeString(String time) {
        String day = time.substring(0, 3);
        int d = getDayOfWeek(day);
        
        activeDay |= (1 << d);
    }

    private static void loadTimeFromCoupon(int st) {
        System.out.println("Loading coupon id " + itemId + ". Rate: " + itemMultiplier + "x.");
        
        String line = null;
        try {
            startHour = -1;
            endHour = -1;
            activeDay = 0;
            
            String time = null;
            while((line = bufferedReader.readLine()) != null) {
                simpleToken(line);
                if(status < st) break;
                
                time = getNodeValue(line);
                processDayTimeString(time);
                
                simpleToken(line);
            }
            
            if(time != null) {
                processHourTimeString(time);
                
                PreparedStatement ps = con.prepareStatement("INSERT INTO nxcoupons (couponid, rate, activeday, starthour, endhour) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, itemId);
                ps.setInt(2, itemMultiplier);
                ps.setInt(3, activeDay);
                ps.setInt(4, startHour);
                ps.setInt(5, endHour);
                ps.execute();

                ps.close();
            }
        }
        catch(SQLException | IOException e) {
            e.printStackTrace();
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
                itemId = Integer.parseInt(d);
            }
            else if(status == 2) {
                d = getName(token);

                if(!d.contains("info")) {
                    forwardCursor(status);
                }
            }
            else if(status == 3) {
                d = getName(token);

                if(!d.contains("time")) {
                    forwardCursor(status);
                }
                else {
                    loadTimeFromCoupon(status);
                }
            }

            status += 1;
        }
        else {
            if(status == 3) {
                d = getName(token);

                if(d.contains("rate")) {
                    String r = getNodeValue(token);
                    
                    Double db = Double.parseDouble(r);
                    itemMultiplier = db.intValue();
                }
            }
        }
    }

    private static void installRateCoupons(String fileName) {
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
    
    private static void installCouponsTable() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(host, username, password);
            
            PreparedStatement ps = con.prepareStatement("DROP TABLE IF EXISTS `nxcoupons`;");
            ps.execute();
            ps.close();
            
            ps = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `nxcoupons` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `couponid` int(11) NOT NULL DEFAULT '0',\n" +
                    "  `rate` int(11) NOT NULL DEFAULT '0',\n" +
                    "  `activeday` int(11) NOT NULL DEFAULT '0',\n" +
                    "  `starthour` int(11) NOT NULL DEFAULT '0',\n" +
                    "  `endhour` int(11) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
            
            ps.execute();
            ps.close();

            installRateCoupons("lib/0521.img.xml");
            installRateCoupons("lib/0536.img.xml");

            con.close();
        }

        catch(SQLException e) {
            System.out.println("Warning: Could not establish connection to database to change card chance rate.");
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
        installCouponsTable();
    }
}
