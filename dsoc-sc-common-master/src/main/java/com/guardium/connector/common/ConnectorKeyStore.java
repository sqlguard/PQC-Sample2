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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class ConnectorKeyStore
{
    private static Path ksDir = SecureConnectorDefaultProperties.getKeystoreDir();
    private static Path ksName = SecureConnectorDefaultProperties.getKeystoreName();

    public static void initialize() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException
    {
        if (!ksExists())
        {
            Path ksFile = ksDir.resolve(ksName);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            FileOutputStream fos = new FileOutputStream(ksFile.toFile());
            
            // do not make password a class member - we want it to go out of scope after this method runs
            // so that it is in memory as short as possible -- we don't want to give attackers a chance to
            // dump the process and find the password
            String ksPassword = SecureConnectorDefaultProperties.getKeystorePw();
            
            ks.load(null, ksPassword.toCharArray());
            ks.store(fos, ksPassword.toCharArray());

            // null the password to reduce the chance of an attacker dumping core and finding it
            ksPassword = null;
        }
    }

    public static boolean ksExists()
    {
        return Files.exists(ksDir.resolve(ksName));
    }
}
