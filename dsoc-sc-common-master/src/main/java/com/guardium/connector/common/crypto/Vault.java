/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* 5900-A1B                                                          */
/*                                                                   */
/*  © Copyright IBM Corp. 2018, 2019                                   */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.guardium.connector.common.crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

import com.guardium.connector.common.exceptions.EncryptionException;
import com.guardium.connector.common.exceptions.VaultException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class Vault
{
    private static String secret;
    private static HashMap<Path, String> vaultMap = new HashMap<Path, String>();
    private static final Set<PosixFilePermission> OWNER_READ;

    static
    {
        HashSet<PosixFilePermission> aSet = new HashSet<PosixFilePermission>();
        aSet.add(PosixFilePermission.OWNER_READ);
        OWNER_READ = Collections.unmodifiableSet(aSet);
    }

    public synchronized static String getDBSecret() throws VaultException
    {
        return getSecret(SecureConnectorDefaultProperties.getVaultFileName());
    }

    public synchronized static String getKeyStoreSecret() throws VaultException
    {
        return getSecret(SecureConnectorDefaultProperties.getKSVaultFileName());
    }

    public synchronized static String getSecret(Path vaultFile) throws VaultException
    {
        if (!vaultMap.containsKey(vaultFile))
        {
            EncryptionUtils crypto = null;
            try
            {
                crypto = EncryptionUtils.instance();
                secret = crypto.decrypt(readSecret(vaultFile));
                vaultMap.put(vaultFile, secret);
            }
            catch (VaultException e)
            {
                secret = generateSecret();
                try
                {
                    writeSecret(crypto.encrypt(secret), vaultFile);
                }
                catch (EncryptionException | NoSuchAlgorithmException e1)
                {
                    throw new VaultException("vault.secret.error", e.getMessage());
                }
                vaultMap.put(vaultFile, secret);
            }
            catch (EncryptionException e)
            {
                throw new VaultException("vault.secret.error", e.getMessage());
            }
        }
        return vaultMap.get(vaultFile);
    }

    private static String generateSecret()
    {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    private synchronized static void writeSecret(String s, Path vaultFile) throws VaultException
    {
        BufferedWriter vaultWriter = null;
        try
        {
            Path vaultDir = SecureConnectorDefaultProperties.getDBDir();
            if (Files.notExists(vaultDir))
            {
                Files.createDirectories(vaultDir);
            }

            Path vf = vaultDir.resolve(vaultFile);
            vaultWriter = Files.newBufferedWriter(vf);
            vaultWriter.write(s);
            setPerms(vf);
        }
        catch (IOException e)
        {
            throw new VaultException("vault.secret.error", e.getMessage());
        }
        finally
        {
            if (null != vaultWriter)
            {
                try
                {
                    vaultWriter.close();
                }
                catch (IOException e)
                {
                    // safe to ignore this - we were trying to close the file
                }
            }
        }
    }

    /**
     * Set file permissions. On POSIX, sets file permissions to 0400. On DOS, sets readonly flag.
     * 
     * @param f
     *            Path representing the file
     * @return true if operation succeeds, false if file does not exist.
     * @throws SecurityException
     *             if error occurs
     */
    private static boolean setPerms(Path vaultFile)
    {
        if (Files.exists(vaultFile, LinkOption.NOFOLLOW_LINKS))
        {
            try
            {
                FileStore fileStore = Files.getFileStore(vaultFile);
                if (fileStore.supportsFileAttributeView(PosixFileAttributeView.class))
                {
                    // only owner can read:  -r--------
                    Files.setPosixFilePermissions(vaultFile, OWNER_READ);
                }
                else if (fileStore.supportsFileAttributeView(DosFileAttributeView.class))
                {
                    // Set read-only
                    Files.setAttribute(vaultFile, "dos:readonly", true);
                }
                else
                {
                    throw new SecurityException("cannot set pool permissions - unexpected fileStore type");
                }
            }
            catch (IOException e)
            {
                throw new SecurityException("cannot set pool permissions");
            }
            return true;
        }
        return false;
    }

    private synchronized static String readSecret(Path vaultFile) throws VaultException
    {
        Path vf = SecureConnectorDefaultProperties.getDBDir().resolve(vaultFile);

        BufferedReader vaultReader = null;
        String s;

        try
        {
            vaultReader = Files.newBufferedReader(vf);
            s = vaultReader.readLine();
        }
        catch (IOException e)
        {
            throw new VaultException("vault.secret.error", e.getMessage());
        }
        finally
        {
            if (null != vaultReader)
            {
                try
                {
                    vaultReader.close();
                }
                catch (IOException e)
                {
                    // safe to ignore this - we were trying to close the file
                }
            }
        }
        return s;
    }
}
