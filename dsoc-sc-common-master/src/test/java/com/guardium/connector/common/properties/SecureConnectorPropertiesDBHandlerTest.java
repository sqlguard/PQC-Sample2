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

package com.guardium.connector.common.properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.exceptions.PropertiesException;
import com.guardium.test.helpers.TestWithDatabase;

class SecureConnectorPropertiesDBHandlerTest extends TestWithDatabase {
    @Test
    void testGetConnection() throws SQLException, DbException {
        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        assertNotNull(dbHandler.getConnection());
    }

    @Test
    void testExportPropertiesToDB() throws DbException, PropertiesException {
        Properties props = new Properties();
        props.setProperty("testProp1", "testValue1");
        props.setProperty("testProp2", "testValue2");

        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        dbHandler.saveProperties(props);
    }

    @Test
    void testImportPropertiesFromDB() throws DbException, PropertiesException {
        Properties props = new Properties();
        props.setProperty("testProp3", "testValue3");
        props.setProperty("testProp4", "testValue4");

        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        dbHandler.saveProperties(props);

        Properties imported = dbHandler.getProperties();

        assertTrue(imported.get("testProp3").equals("testValue3"));
        assertTrue(imported.get("testProp4").equals("testValue4"));
    }

    @Test
    void testSavePropertyToDB() throws PropertiesException, DbException {
        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        dbHandler.saveProperty("newProp", "newValue");
    }

    @Test
    void testGetPropertyFromDB() throws PropertiesException, DbException {
        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        dbHandler.saveProperty("newProp2", "newValue2");
        assertTrue(dbHandler.getProperty("newProp2").equals("newValue2"));
    }
}
