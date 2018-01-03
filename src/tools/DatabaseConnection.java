package tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import constants.ServerConstants;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 * @author Ronan (some connection pool to this beautiful code)
 */
public class DatabaseConnection {
    private static HikariDataSource ds;
    
    public static Connection getConnection() throws SQLException {
        if(ds != null) {
            try {
                return ds.getConnection();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
        int denies = 0;
        while(true) {   // There is no way it can pass with a null out of here?
            try {
                return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
            } catch (SQLException sqle) {
                denies++;
                
                if(denies == 3) {
                    // Give up, throw exception. Nothing good will come from this.
                    FilePrinter.printError(FilePrinter.SQL_EXCEPTION, "SQL Driver refused to give a connection after " + denies + " tries. Problem: " + sqle.getMessage());
                    throw sqle;
                }
            }
        }
    }
    
    private static int getNumberOfAccounts() {
        try {
            Connection con = DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
            try (PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM accounts")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            } finally {
                con.close();
            }
        } catch(SQLException sqle) {
            return 20;
        }
    }
    
    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
        } catch (ClassNotFoundException e) {
            System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
            e.printStackTrace();
        }
        
        ds = null;
        
        if(ServerConstants.DB_CONNECTION_POOL) {
            // Connection Pool on database ftw!
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(ServerConstants.DB_URL);
            
            config.setUsername(ServerConstants.DB_USER);
            config.setPassword(ServerConstants.DB_PASS);
            
            // Make sure pool size is comfortable for the worst case scenario.
            // Under 100 accounts? Make it 10. Over 10000 accounts? Make it 30.
            int poolSize = (int)Math.ceil(0.00202020202 * getNumberOfAccounts() + 9.797979798);
            if(poolSize < 10) poolSize = 10;
            else if(poolSize > 30) poolSize = 30;
            
            config.setConnectionTimeout(30 * 1000);
            config.setMaximumPoolSize(poolSize);
            
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 25);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

            ds = new HikariDataSource(config);
        }
    }
}
