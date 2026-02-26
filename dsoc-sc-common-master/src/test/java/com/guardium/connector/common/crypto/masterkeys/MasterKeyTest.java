/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                 */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.crypto.masterkeys;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.test.helpers.TestWithLog;

public class MasterKeyTest extends TestWithLog
{
    protected Path dbPath;

    @BeforeEach
    public void cleanupKeyFile() throws IOException
    {
        dbPath = SecureConnectorDefaultProperties.getDBDir();

        // delete the file from the last test run
        dbPath.resolve("keyfile").toFile().delete();
        dbPath.resolve("keyfile1").toFile().delete();
        dbPath.resolve("keyfile2").toFile().delete();
    }

    @Test
    void testKeyLength(TestInfo testInfo) throws IllegalStateException, IOException
    {
        SecretKey key = new MasterKey(dbPath.resolve("keyfile")).getMasterKey();
        assertTrue(key.getEncoded().length == 256 / 8);
    }
    
    @Test
    void testEqual(TestInfo testInfo) throws GeneralSecurityException, IOException
    {
        SecretKey key1 = new MasterKey(dbPath.resolve("keyfile1")).getMasterKey();
        SecretKey key2 = new MasterKey(dbPath.resolve("keyfile1")).getMasterKey();
        assertTrue(key1.equals(key2));
    }

    @Test
    void testNotEqual(TestInfo testInfo) throws GeneralSecurityException, IOException
    {
        SecretKey key1 = new MasterKey(dbPath.resolve("keyfile1")).getMasterKey();
        SecretKey key2 = new MasterKey(dbPath.resolve("keyfile2")).getMasterKey();
        assertTrue(!key1.equals(key2));
    }

    @Test
    void testWrapping(TestInfo testInfo) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException
    {
        MasterKey mk = new MasterKey(dbPath.resolve("keyfile"));
        SecretKey k = mk.getMasterKey();
        assertTrue(k.getEncoded().length == 256 / 8);
        assertTrue(Arrays.equals(k.getEncoded(), mk.unwrapKey(mk.wrapKey(k)).getEncoded()));
    }
}
