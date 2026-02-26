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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

import org.junit.jupiter.api.Test;

class H2UrlTest
{
    @Test
    void testProtocol()
    {
        assertTrue(H2Url.PROTOCOL.equals("jdbc:h2"));
    }

    @Test
    void testConstructorsAndToString()
    {
        H2Url url = new H2Url(null, null);
        assertTrue(url.toString().equals("jdbc:h2:;"));
        
        Path pathToDb = Paths.get("path/to/db");
        
        url = new H2Url(pathToDb, null);
        assertTrue(url.toString().equals("jdbc:h2:" + pathToDb + ";"));

        Map<String, String> options = new Hashtable<String, String>();
        options.put("option", "true");
        url = new H2Url(pathToDb, options);
        assertTrue(url.toString().equals("jdbc:h2:" + pathToDb + ";OPTION=TRUE;"));

        url = new H2Url(Paths.get(""));
        assertTrue(url.toString().equals("jdbc:h2:;"));

        url = new H2Url("");
        assertTrue(url.toString().equals("jdbc:h2:;"));

        url = new H2Url("bad path string");
        assertTrue(url.toString().equals("jdbc:h2:;"));

        url = new H2Url("protocol:path;"); // bad because protocol does not have 2 :'s
        assertTrue(url.toString().equals("jdbc:h2:;"));

        url = new H2Url("protocol:p2:path;"); // bad because protocol does not have 2 :'s
        assertTrue(url.toString().equals("jdbc:h2:path;"));

        url = new H2Url("protocol:p2:path;badoption");
        assertTrue(url.toString().equals("jdbc:h2:path;"));

        url = new H2Url("protocol:p2:path;option=true");
        assertTrue(url.toString().equals("jdbc:h2:path;OPTION=TRUE;"));

        url = new H2Url("protocol:p2:path;option=true;option=false");
        assertTrue(url.toString().equals("jdbc:h2:path;OPTION=FALSE;"));

        url = new H2Url("protocol:p2:path;option1=true;option2=false");
        assertTrue(url.getDbPath().equals(Paths.get("path")));
        assertTrue(url.getOption("OPTION1").equals("TRUE"));
        assertTrue(url.getOption("OPTION2").equals("FALSE"));

        url = new H2Url("jdbc:h2:/path/to/db/secure-connector-db;option1=true;option2=false");
        Path scDb = Paths.get("/path/to/db/secure-connector-db");
        assertTrue(url.getDbPath().equals(scDb));
        assertTrue(url.getOption("OPTION1").equals("TRUE"));
        assertTrue(url.getOption("OPTION2").equals("FALSE"));
    }

    @Test
    void testEquals()
    {
        Map<String, String> options = new Hashtable<String, String>();
        options.put("OPTION1", "TRUE");
        options.put("OPTION2", "FALSE");
        H2Url url1 = new H2Url(Paths.get("path/to/db"), options);
        
        Map<String, String> options2 = new Hashtable<String, String>();
        options2.put("OPTION2", "FALSE");
        options2.put("OPTION1", "TRUE");
        H2Url url2 = new H2Url(Paths.get("path/to/db"), options2);
        assertTrue(url1.equals(url2));

        Map<String, String> options3 = new Hashtable<String, String>();
        options2.put("OPTION2", "FALSE");
        H2Url url3 = new H2Url(Paths.get("path/to/db"), options3);
        assertFalse(url2.equals(url3));
        
        H2Url url4 = new H2Url(Paths.get("path/to/db"), options3);
        assertTrue(url3.equals(url4));
        
        url4.setDbPath(Paths.get("some other path"));
        assertFalse(url4.equals(url3));
    }

    @Test
    void testParseOptionString()
    {
        Map<String, String> options = new Hashtable<String, String>();
        options.put("OPTION1", "TRUE");
        options.put("OPTION2", "FALSE");

        assertTrue(H2Url.parseOptionString("OPTION1=TRUE;OPTION2=FALSE").equals(options));
    }

    @Test
    void testOptions()
    {
        H2Url url = new H2Url("protocol:p2:path;option1=true;option2=false");
        assertTrue(url.getDbPath().equals(Paths.get("path")));
        assertTrue(url.getOption("OPTION1").equals("TRUE"));
        assertTrue(url.getOption("OPTION2").equals("FALSE"));

        assertNull(url.getOption(""));
        
        url.setOption("opt", "false");
        assertTrue(url.getOption("opt").equals("FALSE"));

        url.setOption(" opt", "true");
        assertTrue(url.getOption("opt ").equals("TRUE"));

        url.removeOption(" opt ");
        assertNull(url.getOption("opt"));
    }
}
