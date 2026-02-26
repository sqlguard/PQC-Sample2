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

package com.guardium.connector.common.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.guardium.test.helpers.TestWithLog;

class TestException extends SecureConnectorException
{
    private static final long serialVersionUID = 31411810827068183L;

    public TestException(String key, String message)
    {
        super(key, message);
    }

    public TestException(String key)
    {
        super(key);
    }
}

class SecureConnectorExceptionTest extends TestWithLog
{
    @Test
    void testSecureConnectorExceptionStringString()
    {
        try
        {
            throw new TestException("key1", "message");
        }
        catch (TestException e)
        {
            assertTrue(new String("value 1: message").equals(e.getMessage()));
        }
    }

    @Test
    void testSecureConnectorExceptionRethrow()
    {
        try
        {
            throw new TestException("key1", "message");
        }
        catch (TestException e1)
        {
            assertTrue(new String("value 1: message").equals(e1.getMessage()));

            try
            {
                throw new TestException(e1.getMessage());
            }
            catch (TestException e2)
            {
                assertTrue(new String("value 1: message").equals(e2.getMessage()));
            }
        }
    }

    @Test
    void testSecureConnectorExceptionString()
    {
        String k2 = new String("key2");

        try
        {
            throw new TestException(k2);
        }
        catch (TestException e)
        {
            assertTrue(k2.equals(e.getMessage()));
        }
    }

    @Test
    void testKeyNotFoundNoDetails()
    {
        try
        {
            throw new TestException("bogus key", "details");
        }
        catch (TestException e)
        {
            assertTrue(e.getMessage().equals("details"));
        }
    }

    @Test
    void testKeyNotFoundWithDetails()
    {
        try
        {
            throw new TestException("bogus key", "details");
        }
        catch (TestException e)
        {
            assertTrue(e.getMessage().equals("details"));
        }
    }

    @Test
    void testToJson()
    {
        assertNotNull(new TestException("key1").toJson());
    }

    @Test
    void testToString()
    {
        assertNotNull(new TestException("key1").toString());
    }
}
