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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.test.helpers.TestWithLog;

public class ConnectorKeyStoreTest extends TestWithLog
{
    private static String password = "passw0rdpassw0rd";
    private static Path keystoreName = Paths.get("keystore");
    private Path keyStoreDir;
    
    @BeforeAll
    static void setupTest() throws Exception
    {
        SecureConnectorDefaultProperties.setKeystoreName(keystoreName);
        SecureConnectorDefaultProperties.setkeystorePw(password);
    }

    @BeforeEach
    void testInitialize() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        keyStoreDir = SecureConnectorDefaultProperties.getKeystoreDir();

        // cleanup previous test
        keyStoreDir.resolve(keystoreName).toFile().delete();

        ConnectorKeyStore.initialize();
        assertTrue(ConnectorKeyStore.ksExists());
    }

    @Test
    void testKeyStoreLoad() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            FileNotFoundException, IOException
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStoreDir.resolve(keystoreName).toFile()), password.toCharArray());
        assertNotNull(ks);
    }

    @Test
    void testKeyStoreLoadBadPassword() throws KeyStoreException
    {
        String wrong_password = "badPassw0rd";
        KeyStore ks = KeyStore.getInstance("JKS");
        assertThrows(IOException.class, () ->
        {
            ks.load(new FileInputStream(keyStoreDir.resolve(keystoreName).toFile()), wrong_password.toCharArray());
        });
    }

}
