/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobbookindexer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.*;


/**
 * @author RonanLana
 *
 * This application simply gets from the MonsterBook.img.xml all mobid's and
 * puts them on a SQL table with the correspondent mob cardid.
 *
 */
public class MobBookIndexer {
    static String host = "jdbc:mysql://localhost:3306/maplesolaxia";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String fileName = "lib/MonsterBook.img.xml";

    static Connection con = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    static int mobId = -1;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        if(j - i < 7) dest = new char[6];
        else dest = new char[7];
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

    private static boolean isCard(int itemId) {
        return itemId / 10000 == 238;
    }
    
    private static void loadPairFromMob() {
        System.out.println("Loading mob id " + mobId);

        try {
            int itemId, rid = 1;
            
            PreparedStatement ps, ps2;
            ResultSet rs;
            
            ps = con.prepareStatement("SELECT itemid FROM drop_data WHERE (dropperid = ? AND itemid > 0) GROUP BY itemid;");
            ps.setInt(1, mobId);
            rs = ps.executeQuery();

            while(rs.next()) {
                itemId = rs.getInt("itemid");
                if(isCard(itemId)) {
                    ps2 = con.prepareStatement("INSERT INTO `monstercardwz` (`cardid`, `mobid`) VALUES (?, ?)", rid);
                    rid++;
                    ps2.setInt(1, itemId);
                    ps2.setInt(2, mobId);
                    
                    ps2.executeUpdate();
                }
            }
            
            rs.close();
            ps.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void translateToken(String token) {
        String d;
        int temp;

        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting MobId
                d = getName(token);
                mobId = Integer.parseInt(d);
            }
            else if(status == 2) {
                d = getName(token);

                if(d.contains("reward")) {
                    temp = status;

                    loadPairFromMob();
                    forwardCursor(temp);
                }
            }

            status += 1;
        }

    }

    private static void IndexFromDropData() {
        // This will reference one line at a time
        String line = null;

        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(host, username, password);

            fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            PreparedStatement ps = con.prepareStatement("DROP TABLE IF EXISTS monstercardwz;");
            ps.execute();
            
            ps = con.prepareStatement("CREATE TABLE `monstercardwz` ("
                    + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`cardid` int(10) NOT NULL DEFAULT '-1',"
                    + "`mobid` int(10) NOT NULL DEFAULT '-1',"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
            ps.execute();
            
            while((line = bufferedReader.readLine()) != null) {
                translateToken(line);
            }

            bufferedReader.close();
            fileReader.close();

            con.close();
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IndexFromDropData();
    }

}
