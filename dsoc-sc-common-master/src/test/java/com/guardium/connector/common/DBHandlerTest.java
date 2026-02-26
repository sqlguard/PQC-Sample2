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

package com.guardium.connector.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.test.helpers.TestWithDatabase;

class TestDBHandler extends DBHandler
{
    public static boolean preInitCalled = false;
    public static boolean initCalled = false;
    public static boolean postInitCalled = false;

    public TestDBHandler() throws DbException
    {
        super();
    }

    @Override
    public void preInit()
    {
        preInitCalled = true;
    }

    @Override
    public void init()
    {
        initCalled = true;
    }

    @Override
    public void postInit()
    {
        postInitCalled = true;
    }

}

class DBHandlerTest extends TestWithDatabase
{
    public DBHandlerTest() 
    {
        super(false); // do not automatically create the database
    }
    
    @Test
    void testGetConnection() throws DbException, SQLException, ClassNotFoundException
    {
        DBHandler.createDB();
        DBHandler dbHandler = new TestDBHandler();
        assertNotNull(dbHandler.getConnection());
    }

    @Test
    void testGetActiveConnectionPoolConnections() throws DbException, SQLException, ClassNotFoundException
    {
        DBHandler.createDB();
        DBHandler dbHandler = new TestDBHandler();
        assertNotNull(dbHandler.getConnection());
        assertTrue(TestDBHandler.getActiveConnectionCount() >= 1);
    }

    @Test
    void testDbDoesntExist() throws ClassNotFoundException, DbException
    {
        assertFalse(DBHandler.dbExists());
    }

    @Test
    void testPrepareStatement() throws DbException, SQLException, ClassNotFoundException
    {
        DBHandler.createDB();
        DBHandler dbHandler = new TestDBHandler();
        assertNotNull(dbHandler.prepareStatement("select true;", dbHandler.getConnection()));
    }

    @Test
    void testInitialization() throws DbException, ClassNotFoundException, SQLException
    {
        DBHandler.createDB();
        DBHandler dbHandler = new TestDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();

        assertTrue(TestDBHandler.preInitCalled);
        assertTrue(TestDBHandler.initCalled);
        assertTrue(TestDBHandler.postInitCalled);
    }
}
