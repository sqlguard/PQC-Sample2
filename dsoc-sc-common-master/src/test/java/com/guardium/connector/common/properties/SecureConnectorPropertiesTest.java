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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.guardium.connector.common.exceptions.DbException;
import com.guardium.connector.common.exceptions.PropertiesException;
import com.guardium.test.helpers.TestWithDatabase;

class SecureConnectorPropertiesTest extends TestWithDatabase
{
    @Disabled
    @Test
    void testGetName()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetName()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetDescription()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetDescription()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetPublicKey()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetPublicKey()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetPublicKeyModulus()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetPublicKeyModulus()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetPublicKeyExponent()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetPublicKeyExponent()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetPrivateKey()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetPrivateKey()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetAuthentication()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetAuthentication()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetAuthorization()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetAuthorization()
    {
    }

    @Test
    void testLoginTime() throws DbException, PropertiesException, ParseException
    {
        SecureConnectorPropertiesDBHandler dbHandler = new SecureConnectorPropertiesDBHandler();
        dbHandler.preInit();
        dbHandler.init();
        dbHandler.postInit();
        SecureConnectorProperties.setPropertiesStore(dbHandler);

        Date now = new Date();
        SecureConnectorProperties.instance().setLoginTime(now);
        assertEquals(now, SecureConnectorProperties.instance().getLoginTime());
    }

    @Disabled
    @Test
    void testInstance()
    {
        fail("Not yet implemented");
    }
    
    @Disabled
    @Test
    void testSetPropertiesStore()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetProperties()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testReloadProperties()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSaveProperties()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetProperty()
    {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSaveProperty()
    {
        fail("Not yet implemented");
    }
}
