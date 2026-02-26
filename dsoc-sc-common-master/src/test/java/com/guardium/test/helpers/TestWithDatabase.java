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

package com.guardium.test.helpers;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.guardium.connector.common.db.DBHandler;
import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class TestWithDatabase extends TestWithLog
{
    protected static String dbUser = "testuser";
    protected static String dbPassword = "test";
    protected boolean autoCreateDB = true;

    public TestWithDatabase()
    {
    }

    public TestWithDatabase(boolean autoCreateDB)
    {
        this.autoCreateDB = autoCreateDB;
    }

    @BeforeAll
    public static synchronized void setupDatabase() throws SQLException, DbException, IOException
    {
        SecureConnectorDefaultProperties.setDBUser(dbUser);
        SecureConnectorDefaultProperties.setDBPassword(dbPassword);
    }

    @BeforeEach
    public void setupEachTestDatabase(TestInfo testInfo) throws ClassNotFoundException, SQLException,
            IllegalAccessException, InstantiationException, DbException, IOException
    {
        // delete the backup dir from the previous test
        rmDir(SecureConnectorDefaultProperties.getDBDir());

        if (autoCreateDB)
        {
            DBHandler.createDB();
        }
    }
}
