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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.guardium.connector.common.exceptions.InvalidVersionException;

class VersionTest
{
    @Test
    void testValid() throws InvalidVersionException
    {
        assertNotNull(new Version("1.0.34.test"));
    }

    @Test
    void testInvalid()
    {
        Version version = null;
        try
        {
            version = new Version("bad.version");
        }
        catch (InvalidVersionException e)
        {
            assertNull(version);
        }
    }

    @Test
    void testGettersAndSetters() throws InvalidVersionException
    {
        Version v = new Version();
        v.setMajor(1);
        assertTrue(v.getMajor() == 1);
        v.setMinor(2);
        assertTrue(v.getMinor() == 2);
        v.setBuild(3);
        assertTrue(v.getBuild() == 3);
        v.setBuildType("dev");
        assertTrue(v.getBuildType().equals("dev"));

        Version v2 = new Version("1.2.3.dev");
        assertTrue(v2.getMajor() == 1);
        assertTrue(v2.getMinor() == 2);
        assertTrue(v2.getBuild() == 3);
        assertTrue(v2.getBuildType().equals("dev"));

        Version v3 = new Version("1.2.3.dev.4.5.6");
        assertTrue(v3.getMajor() == 1);
        assertTrue(v3.getMinor() == 2);
        assertTrue(v3.getBuild() == 3);
        assertTrue(v3.getBuildType().equals("dev.4.5.6"));
    }

    @Test
    void testEquals() throws InvalidVersionException
    {
        Version v1 = new Version("1.2.3.dev");
        Version v2 = new Version("1.2.3.dev");
        Version v3 = new Version("1.2.3");
        Version v4 = new Version("1.2.3");
        Version v5 = new Version("1.2.4");
        Version v6 = new Version("1.3.3");
        Version v7 = new Version("2.3.3");

        assertTrue(new Version().equals(new Version()));
        assertFalse(new Version().equals(null));

        assertTrue(v1.equals(v2));
        assertTrue(v3.equals(v4));

        assertFalse(v1.equals(v3));
        assertFalse(v3.equals(v5));
        assertFalse(v3.equals(v6));
        assertFalse(v3.equals(v7));
    }

    @Test
    void testCompare() throws InvalidVersionException
    {
        Version v1 = new Version("1.2.3.dev");
        Version v2 = new Version("1.2.3.dev");
        Version v2a = new Version("1.2.3.a");
        Version v2b = new Version("1.2.3.b");
        Version v3 = new Version("1.2.3");
        Version v4 = new Version("1.2.3");
        Version v5 = new Version("1.2.4");
        Version v6 = new Version("1.3.3");
        Version v7 = new Version("2.2.3");

        assertTrue(new Version().compareTo(new Version()) == 0);
        
        // it is illegal to compareTo(null) and since Version.java checks this case,
        // we test for it here
        assertThrows(NullPointerException.class, () ->
        {
            new Version().compareTo(null);
        });

        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v3.compareTo(v4) == 0);

        assertTrue(v2.compareTo(v3) > 0);
        assertTrue(v3.compareTo(v2) < 0);
        assertTrue(v2a.compareTo(v2b) < 0);
        assertTrue(v2b.compareTo(v2a) > 0);

        assertTrue(v3.compareTo(v5) < 0);
        assertTrue(v5.compareTo(v3) > 0);

        assertTrue(v5.compareTo(v6) < 0);
        assertTrue(v6.compareTo(v5) > 0);

        assertTrue(v6.compareTo(v7) < 0);
        assertTrue(v7.compareTo(v6) > 0);
    }

    @Test
    void testToString() throws InvalidVersionException
    {
        assertTrue(new Version("1.2.3.dev").toString().equals("1.2.3.dev"));
    }

}
