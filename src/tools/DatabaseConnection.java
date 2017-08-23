package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;

import constants.ServerConstants;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 * @author Ronan (some connection pool to this beautiful code)
 */
public class DatabaseConnection {
    private static Properties prop;
    
    public static Connection getConnection() throws SQLException {
        if(prop != null) {
            Connection con = null;
            while(con == null) {   //oh yes, this will loop until success!
                try {
                    con = DriverManager.getConnection(ServerConstants.DB_URL, prop);
                } catch (SQLException sqle) {}
            }
            
            return con;
        } else {
            return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
        }
    }
    
    private static Properties getProperties() {
        Properties connectionProperties = new Properties();
        connectionProperties.put("user", ServerConstants.DB_USER);
        connectionProperties.put("password", ServerConstants.DB_PASS);

        connectionProperties.put("minIdle",  "-1");
        connectionProperties.put("maxIdle",  "-1");

        connectionProperties.put("testOnBorrow", "true");
        connectionProperties.put("lifo", "false");	

        connectionProperties.put("maxTotal", "42");                     //max allotted connections
        connectionProperties.put("maxConnLifetimeMillis", "60000");     //connection remains valid for 1 min
        connectionProperties.put("maxWaitMillis", "777");               //there are more pools, if this one is unavailable then pass to another already
        connectionProperties.put("poolPreparedStatements", "true");
        connectionProperties.put("maxOpenPreparedStatements", "100");   //max allotted PS

        return connectionProperties;
    }
    
    private PoolingDriver installDriver(short id) {
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(ServerConstants.DB_URL, prop);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool.getJmxName());
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool("maplesolaxiapool" + id, connectionPool);
        
        return driver;
    }

    public DatabaseConnection() {
        prop = null;
        
        try {
            Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
        } catch (ClassNotFoundException e) {
            System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
            e.printStackTrace();
            return;
        }
        
        if(ServerConstants.DB_EXPERIMENTAL_POOLS > 0) {
            // Connection Pool on database ftw!
            
            prop = getProperties();
            try {
                for(short i = 0; i < ServerConstants.DB_EXPERIMENTAL_POOLS; i++) {
                    DriverManager.registerDriver(installDriver(i));
                }
            } catch(SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
