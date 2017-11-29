package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 */
public class DatabaseConnection {
    private static String DB_URL = "jdbc:mysql://localhost:3306/heavenms";
    private static String DB_USER = "root";
    private static String DB_PASS = "";
    
    public static final int RETURN_GENERATED_KEYS = 1;

    private static ThreadLocal<Connection> con = new ThreadLocalConnection();

    public static Connection getConnection() {
        Connection c = con.get();
        try {
            c.getMetaData();
        } catch (SQLException e) { // connection is dead, therefore discard old object 5ever
            con.remove();
            c = con.get();
        }
        return c;
    }

    private static class ThreadLocalConnection extends ThreadLocal<Connection> {

        @Override
        protected Connection initialValue() {
            try {
                Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
            } catch (ClassNotFoundException e) {
                System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
                e.printStackTrace();
                return null;
            }
            try {
                return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (SQLException e) {
                System.out.println("[SEVERE] Unable to make database connection.");
                e.printStackTrace();
                return null;
            }
        }
    }
}