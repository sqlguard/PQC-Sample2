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

package com.guardium.connector.common.crypto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.guardium.connector.common.exceptions.VaultException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;
import com.guardium.test.helpers.TestWithLog;

class VaultTest extends TestWithLog
{
    private static Path VAULT = Paths.get("testVault");
    private static Path KVAULT = Paths.get("testKeystoreVault");

    @BeforeEach
    public void setup() throws IOException
    {
        // cleanup file from last test run
        VAULT.toFile().delete();
        KVAULT.toFile().delete();
    }

    @Test
    void testGetSecret() throws VaultException
    {
        // first call initializes vault
        String secret = Vault.getSecret(VAULT);
        assertNotNull(secret);
        assertFalse(secret.isEmpty());

        // second call just returns already initialized secret
        String secret2 = Vault.getSecret(VAULT);
        assertNotNull(secret2);
        assertFalse(secret2.isEmpty());

        assertTrue(secret.equals(secret2));
    }
    
    @Test
    void getKeyStoreSecret() throws VaultException
    {
        SecureConnectorDefaultProperties.setKSVaultFileName(KVAULT);
        String secret = Vault.getKeyStoreSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());

        String secret2 = Vault.getKeyStoreSecret();
        assertNotNull(secret2);
        assertFalse(secret2.isEmpty());

        assertTrue(secret.equals(secret2));
    }
}
