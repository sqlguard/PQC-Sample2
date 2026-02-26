/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.properties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.guardium.connector.common.LoggerUtils;
import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.exceptions.PropertiesException;

public class SecureConnectorPropertiesDBHandler extends DBHandler implements PropertiesStoreInterface
{
    private static Logger log = null;
    
	public SecureConnectorPropertiesDBHandler() throws DbException
    {
        super();
        if (null == log)
        {
        	log = LoggerUtils.getLogger(SecureConnectorPropertiesDBHandler.class);
        }
    }

    @Override
    public void init() throws DbException
    {
        Connection conn = null;
        try
        {
            conn = getConnection();
            Statement stmt = conn.createStatement();

            String ddl = new String("DROP TABLE IF EXISTS SECURE_CONNECTOR_PROPERTIES;"
                    + "CREATE TABLE IF NOT EXISTS SECURE_CONNECTOR_PROPERTIES ("
                    + "property_name             VARCHAR(256) NOT NULL,"
                    + "property_value            VARCHAR(4096));");

            stmt.execute(ddl);
            stmt.close();
        }
        catch (SQLException sql)
        {
            log.error("Properties tables installation error: " + sql.getMessage());
            throw new DbException("db.init.error", sql.getMessage());
        }
    }

    @Override
    public void saveProperties(Properties properties) throws PropertiesException
    {
        if (properties == null)
        {
            throw new PropertiesException("properties.uninitialized");
        }

        log.debug("Saving properties to " + getDBUrl());

        Connection conn = null;
        PreparedStatement prep = null;

        try
        {
            conn = getConnection();
            prep = conn.prepareStatement("delete from SECURE_CONNECTOR_PROPERTIES");
            prep.executeUpdate();
            prep.close();
            prep = conn.prepareStatement("insert into SECURE_CONNECTOR_PROPERTIES values(?, ?)");

            Enumeration<?> keys = properties.keys();
            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                String value = (String) properties.get(key);
                prep.setString(1, key);
                prep.setString(2, value);
                prep.executeUpdate();
            }
        }
        catch (Exception e)
        {
            log.error("Can't insert client properties to DB", e);
            throw new PropertiesException("properties.db.insert.error", e.getMessage());
        }
        finally
        {
            if (prep != null)
            {
                try
                {
                    prep.close();
                }
                catch (Exception e1)
                {
                }
            }
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e2)
                {
                }
            }
        }
    }

    @Override
    public Properties getProperties() throws PropertiesException
    {
        Properties ret = new Properties();
        Connection conn = null;
        Statement stmt = null;

        String query = "select * from SECURE_CONNECTOR_PROPERTIES";
        try
        {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next())
            {
                // no properties - this is not an error condition - the client
                // may be about to save the propties to the database -- the caller
                // is responsible for checking to see if Properties are empty
                return ret;
            }

            do
            {
                ret.setProperty(rs.getString(1), rs.getString(2));
            }
            while (rs.next());

            rs.close();
        }
        catch (SQLException e)
        {
            log.error("Cannot import client properties from DB", e);
            throw new PropertiesException("properties.db.import.error", e.getMessage());
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (Exception e1)
                {
                }
            }
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e2)
                {
                }
            }
        }

        return ret;
    }

    /**
     * Save a property to the database. If a row with the "propName" is found it will be updated, otherwise it will be inserted.
     * 
     * @param propName
     *            The name of the property to be saved
     * @param value
     *            The value of the property to be saved
     * @throws PropertiesException
     */
    @Override
    public void saveProperty(String propName, String value) throws PropertiesException
    {
        Connection conn = null;
        PreparedStatement prep = null;

        try
        {
            conn = getConnection();
            prep = conn.prepareStatement("merge into SECURE_CONNECTOR_PROPERTIES key(PROPERTY_NAME) values(?, ?)");
            prep.setString(1, propName);
            prep.setString(2, value);
            prep.executeUpdate();
            // log.debug("Saved " + propName + "=[" + value + "] to database.");
        }
        catch (SQLException e)
        {
            log.error("Can't save property to DB", e);
            throw new PropertiesException("properties.db.save.error", e.getMessage());
        }
        finally
        {
            if (prep != null)
            {
                try
                {
                    prep.close();
                }
                catch (Exception e1)
                {
                }
            }
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e2)
                {
                }
            }
        }
    }

    /**
     * Gets a the value of a property from the database
     * 
     * @param propName
     *            Name of the property to get
     * @return Value of the property
     * @throws PropertiesException
     *             Throws if the property was not found
     */
    @Override
    public String getProperty(String propName) throws PropertiesException
    {
        String value = null;
        Connection conn = null;
        Statement stmt = null;

        try
        {
            conn = getConnection();
            stmt = conn.createStatement();
            String query = "select PROPERTY_VALUE from SECURE_CONNECTOR_PROPERTIES where PROPERTY_NAME = '" + propName
                    + "'";
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next())
            {
                // no properties
                rs.close();
                log.error("No properties found in the DB");
                throw new PropertiesException("properties.db.import.error");
            }

            value = rs.getString(1);
            rs.close();
        }
        catch (SQLException e)
        {
            log.error("Can't get client property from DB", e);
            throw new PropertiesException("properties.db.select.error", e.getMessage());
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (Exception e1)
                {
                }
            }
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e2)
                {
                }
            }
        }

        return value;
    }
}
