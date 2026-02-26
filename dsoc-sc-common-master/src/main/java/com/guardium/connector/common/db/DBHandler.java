/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/* © Copyright IBM Corp. 2018, 2019                                  */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class DBHandler
{
    private static Map<H2Url, JdbcConnectionPool> cp = new Hashtable<H2Url, JdbcConnectionPool>();
    protected static Logger log = LoggerUtils.getLogger(DBHandler.class);
    private H2Url dbUrl;
    private String dbUser;
    private String dbPassword;

    // use connector default values
    public DBHandler() throws DbException
    {
        this.dbUrl = SecureConnectorDefaultProperties.getDBUrl();
        this.dbUser = SecureConnectorDefaultProperties.getDBUser();
        this.dbPassword = SecureConnectorDefaultProperties.getDBPassword();

        setupConnectionPool(dbUrl, dbUser, dbPassword);
    }

    // allow caller to specify db args - useful for testing
    public DBHandler(H2Url dbUrl, String dbUser, String dbPassword) throws DbException
    {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        
        setupConnectionPool(dbUrl, dbUser, dbPassword);
    }
    
    protected void setupConnectionPool(H2Url dbUrl, String dbUser, String dbPassword) throws DbException
    {
        try
        {
            Class.forName(SecureConnectorDefaultProperties.getDBDriver());
        }
        catch (ClassNotFoundException e)
        {
            throw new DbException("db.init.error", e.getMessage());
        }

        if (null == cp.get(dbUrl))
        {
            if (log.isDebugEnabled())
                log.debug("Creating connection pool for " + dbUrl);

            JdbcConnectionPool pool = JdbcConnectionPool.create(dbUrl.toString(), dbUser, dbPassword);
            pool.setMaxConnections(SecureConnectorDefaultProperties.getMaxPoolConnections());
            cp.put(dbUrl, pool);
        }
    }

    public H2Url getDBUrl()
    {
        return dbUrl;
    }

    public String getDBUser()
    {
        return dbUser;
    }

    public String getDBPassword()
    {
        return dbPassword;
    }

    // need to make this method protected
    public Connection getConnection() throws SQLException
    {
        return cp.get(dbUrl).getConnection();
    }

    public static int getActiveConnectionCount()
    {
        int total = 0;
        for (JdbcConnectionPool pool : cp.values())
        {
            total += pool.getActiveConnections();
        }
        return total;
    }

    public static void closeConnectionPool()
    {
        for (JdbcConnectionPool pool : cp.values())
        {
            pool.dispose();
        }
        cp.clear();
    }

    public static boolean dbExists() throws ClassNotFoundException
    {
        return dbExists(SecureConnectorDefaultProperties.getDBUrl().toString(), SecureConnectorDefaultProperties.getDBUser(),
                SecureConnectorDefaultProperties.getDBPassword());
    }

    public static void createDB() throws SQLException, ClassNotFoundException
    {
        createDB(SecureConnectorDefaultProperties.getDBCreationUrl(),
                SecureConnectorDefaultProperties.getDBUser(),
                SecureConnectorDefaultProperties.getDBPassword());
    }

    public static void createDB(H2Url dbUrl, String dbUser, String dbPassword) throws ClassNotFoundException,
            SQLException
    {
        if (!dbExists())
        {
            // getting a connection to the db will cause H2 to automatically create it;
            // however, the dbPath string must not have "IFEXISTS=TRUE" in it in order
            // for that to work - we can just close the connection
            if (log.isDebugEnabled())
                log.debug("Creating database " + dbUrl);

            DriverManager.getConnection(dbUrl.toString(), dbUser, dbPassword).close();
        }
    }

    public void checkpoint() throws SQLException
    {
        Connection conn = getConnection();
        PreparedStatement checkpoint = conn.prepareStatement("checkpoint;");
        checkpoint.execute();
        checkpoint.close();
        conn.close();

        if (log.isDebugEnabled())
        {
            log.debug("Checkpoint");
        }
    }

    public static boolean dbExists(String dbUrl, String dbUser, String dbPassword) throws ClassNotFoundException
    {
        if (log.isDebugEnabled())
            log.debug("Checking for database " + dbUrl);
        
        // if we can't find the driver we will throw
        Class.forName(SecureConnectorDefaultProperties.getDBDriver());

        try
        {
            // IFEXISTS=TRUE causes the connection to fail (without creating the db) if it does not already exist
            // N.B. this will never throw when running against an embedded server (such as during unit tests)
            DriverManager.getConnection(dbUrl, dbUser, dbPassword).close();
        }
        catch (SQLException e)
        {
            // DB does not exist; this might be expected so no need to log anything, just return false.
            // This could also occur if the login credentials are bad.
            if (log.isDebugEnabled())
                log.debug("Exception checking for db: " + e.getMessage());
            return false;
        }
        return true;
    }

    public PreparedStatement prepareStatement(String sql, Connection conn) throws SQLException
    {
        if (log.isTraceEnabled())
            log.trace("[DBHandler::prepareStatement] preparing statement:\n" + sql);

        return conn.prepareStatement(sql);
    }

    public void preInit() throws DbException
    {
    }

    public void init() throws DbException
    {
    }

    public void postInit() throws DbException
    {
    }
}
