package tools;

import java.sql.Connection;
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
            return ds.getConnection();
        } else {
            return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
        }
    }
    
    public DatabaseConnection() {
        ds = null;
        
        if(ServerConstants.DB_EXPERIMENTAL_POOL) {
            // Connection Pool on database ftw!
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(ServerConstants.DB_URL);
            
            config.setUsername(ServerConstants.DB_USER);
            config.setPassword(ServerConstants.DB_PASS);
            
            config.addDataSourceProperty("connectionTimeout", "30000");
            config.addDataSourceProperty("maximumPoolSize", "100");
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(config);
        } else {
            try {
                Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
            } catch (ClassNotFoundException e) {
                System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
                e.printStackTrace();
                return;
            }
        }
    }
}
