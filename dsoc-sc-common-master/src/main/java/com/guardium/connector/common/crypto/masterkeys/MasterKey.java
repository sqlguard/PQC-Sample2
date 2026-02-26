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

package com.guardium.connector.common.crypto.masterkeys;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class MasterKey
{
    private static final int ENTROPY_BYTES = 256;
    private static final int DIGEST_BYTES = 32;
    private static final int KEY_BITS = 256; // follow export rules!
    private static final String notAPwd = "Just for checksumming";
    private static final Set<PosixFilePermission> OWNER_READ;

    private Path entropyFilePath = null;
    private int entropyBytes = 0;
    private int digestBytes = 0;
    private int keyBits = 0;

    private SecretKey theKey = null;

    static
    {
        HashSet<PosixFilePermission> aSet = new HashSet<PosixFilePermission>();
        aSet.add(PosixFilePermission.OWNER_READ);
        OWNER_READ = Collections.unmodifiableSet(aSet);
    }

    public MasterKey(Path entropyFile)
    {
        this(entropyFile, KEY_BITS, ENTROPY_BYTES, DIGEST_BYTES);
    }

    public MasterKey(Path entropyFile, int keyBits)
    {
        this(entropyFile, keyBits, ENTROPY_BYTES, DIGEST_BYTES);
    }

    public MasterKey(Path entropyFile, int keyBits, int entropyBytes)
    {
        this(entropyFile, keyBits, entropyBytes, DIGEST_BYTES);
    }

    public MasterKey(Path entropyFile, int keyBits, int entropyBytes, int digestBytes)
    {
        entropyFilePath = entropyFile;

        this.keyBits = keyBits;
        this.entropyBytes = entropyBytes;
        this.digestBytes = digestBytes;
    }

    /**
     * Initialize a MasterKey by creating an entropy pool and generating a KEK. This separate method exists in case the
     * client wishes to delay initialization to some point in time after getting the MasterKey instance. If already
     * initialized, this method sould be idempotent.
     * 
     * @throws SecurityException
     *             if an error occurs
     */
    private void init()
    {
        if (null != theKey)
        {
            return;
        }

        byte[] fileBytes = null;
        try
        {
            // If we've set up the system already, load the key
            if (!fileIsReadyToRead())
            {
                // create random bytes and hash
                fileBytes = createPoolData();

                // write bytes to file. save as hex to simplify backup
                String s = bytesToHex(fileBytes);
                fileBytes = s.getBytes("UTF-8");
                Files.write(entropyFilePath, fileBytes, StandardOpenOption.CREATE_NEW);

                // set file permissions
                if (!setPerms())
                {
                    throw new SecurityException("cannot set entropy pool permissions");
                }
            }

            // create master key
            theKey = generateKEK(keyBits);
        }
        catch (Throwable t)
        {
            throw new SecurityException("problem creating entropy pool", t);
        }
        finally
        {
            wipe(fileBytes);
        }
    }

    /**
     * Returns the KEK value. Use <code>isInitialized()</code> to ensure KEK is initialized before calling this.
     * 
     * @return SecretKey representing the KEK
     * @throws IllegalStateException
     *             if KEK is not initialized
     */
    public SecretKey getMasterKey() throws IllegalStateException
    {
        if (null == theKey)
        {
            init();
        }

        return theKey;
    }

    /**
     * Wrap a key with the KEK. The wrapped key can only be recovered using the KEK.
     * 
     * @param key
     *            the key to be wrapped
     * @return byte[] containing the wrapped key bytes
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws IllegalStateException
     *             if KEK is not initialized
     */
    public byte[] wrapKey(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException
    {
        byte[] result = null;
        Cipher c = Cipher.getInstance("AESWrap");
        c.init(Cipher.WRAP_MODE, getMasterKey());
        result = c.wrap(key);
        return result;
    }

    /**
     * Unwrap a key that was previously wrapped with the KEK. The wrapped key can only be recovered using the KEK.
     * 
     * @param key
     *            the Key to be unwrapped
     * @return the unwrapped Key
     * @throws IllegalStateException
     *             if KEK is not initialized
     * @throws SecurityException
     *             if an error occurs during unwrapping operation
     */
    public Key unwrapKey(Key key)
    {
        return unwrapKey(key.getEncoded());
    }

    /**
     * Unwrap a key that was previously wrapped with the KEK. The wrapped key can only be recovered using the KEK.
     * 
     * @param keyBytes
     *            the bytes of the wrapped key
     * @return the unwrapped Key
     * @throws IllegalStateException
     *             if KEK is not initialized
     * @throws SecurityException
     *             if an error occurs during unwrapping operation
     */
    public Key unwrapKey(byte[] keyBytes)
    {
        Key result = null;
        try
        {
            Cipher c = Cipher.getInstance("AESWrap");
            c.init(Cipher.UNWRAP_MODE, getMasterKey());
            result = c.unwrap(keyBytes, "AES", Cipher.SECRET_KEY); // Secret AES key
        }
        catch (Throwable t)
        {
            throw new SecurityException("cannot unwrap key", t);
        }
        return result;
    }

    /**
     * Check that file exists, and is a regular read-only file. On POSIX systems, also verifies 0400 permissions.
     * 
     * @param f
     * @return <code>false</code> if file does not exist, <code>true</code> if file is a regular read-only file
     * @throws IOException
     *             if an I/O error occurs
     * @throws SecurityException
     *             if file exists with incorrect permissions
     */
    private boolean fileIsReadyToRead() throws IOException, SecurityException
    {
        if (!Files.exists(entropyFilePath, LinkOption.NOFOLLOW_LINKS))
        {
            return false;
        }

        BasicFileAttributes attr = Files.readAttributes(entropyFilePath, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (!attr.isRegularFile())
        {
            throw new SecurityException("entropy pool is not a regular file");
        }

        FileStore fileStore = Files.getFileStore(entropyFilePath);
        if (fileStore.supportsFileAttributeView(PosixFileAttributeView.class))
        {
            // only owner can read: -r--------
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(entropyFilePath);
            if (!perms.equals(OWNER_READ))
            {
                throw new SecurityException("entropy pool has invalid permissions");
            }
            return true;
        }
        else if (fileStore.supportsFileAttributeView(DosFileAttributeView.class))
        {
            DosFileAttributes dosAttr = Files.readAttributes(entropyFilePath, DosFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (!dosAttr.isReadOnly())
            {
                throw new SecurityException("entropy pool has invalid permissions");
            }
            return true;
        }
        else if (Files.isRegularFile(entropyFilePath, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(entropyFilePath)
                && !Files.isWritable(entropyFilePath))
        {
            return true;
        }
        else
        {
            throw new SecurityException("entropy pool has invalid permissions");
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
    private boolean setPerms()
    {
        if (Files.exists(entropyFilePath, LinkOption.NOFOLLOW_LINKS))
        {
            try
            {
                FileStore fileStore = Files.getFileStore(entropyFilePath);
                if (fileStore.supportsFileAttributeView(PosixFileAttributeView.class))
                {
                    // only owner can read:  -r--------
                    Files.setPosixFilePermissions(entropyFilePath, OWNER_READ);
                }
                else if (fileStore.supportsFileAttributeView(DosFileAttributeView.class))
                {
                    // Set read-only
                    Files.setAttribute(entropyFilePath, "dos:readonly", true);
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

    private byte[] readPool()
    {
        byte[] result = null;
        byte[] b = null;
        byte[] hash = null;
        try
        {
            // file contains entropy plus a digest of entropy
            b = Files.readAllBytes(entropyFilePath);
            if (b.length != (entropyBytes + digestBytes) * 2)
            {
                throw new SecurityException("entropy pool size incorrect");
            }
            // convert file from hex encoding to binary. Not worried about
            // the immutable String as these bytes are only part of the 
            // key material needed to build a key.
            b = DatatypeConverter.parseHexBinary(new String(b, "UTF-8"));

            // check file integrity
            hash = calcPoolDigest(b);
            if (!Arrays.equals(hash, Arrays.copyOfRange(b, entropyBytes, b.length)))
            {
                throw new SecurityException("entropy pool fails integrity check");
            }
            result = new byte[entropyBytes];
            System.arraycopy(b, 0, result, 0, entropyBytes);
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            throw new SecurityException("problem reading entropy pool", e);
        }
        finally
        {
            wipe(b);
            wipe(hash);
        }
        return result;
    }

    private byte[] createPoolData()
    {
        byte[] b = new byte[entropyBytes];
        byte[] hash = null;
        byte[] result = new byte[entropyBytes + digestBytes];

        try
        {
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(b);
            hash = calcPoolDigest(b);

            System.arraycopy(b, 0, result, 0, entropyBytes);
            System.arraycopy(hash, 0, result, entropyBytes, digestBytes);
        }
        catch (Throwable t)
        {
            throw new SecurityException("problem creating entropy pool", t);
        }
        finally
        {
            wipe(b);
            wipe(hash);
        }
        return result;
    }

    private byte[] calcPoolDigest(byte[] message) throws IOException, NoSuchAlgorithmException
    {
        byte[] result = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(notAPwd.getBytes("UTF-8"));
            md.update(message, 0, entropyBytes);
            result = md.digest();
        }
        catch (NoSuchAlgorithmException | IOException e)
        {
            throw new SecurityException("problem generating pool digest");
        }
        return result;
    }

    private byte[] getStampBytes()
    {
        try
        {
            BasicFileAttributes attr = Files.readAttributes(entropyFilePath, BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            long crtTime = attr.creationTime().toMillis();
            // convert file creation time stamp to bytes
            byte[] result = new byte[Long.SIZE / Byte.SIZE];
            for (int i = result.length - 1; i >= 0; i--)
            {
                result[i] = (byte) (crtTime & 0xFF);
                crtTime >>= Byte.SIZE;
            }
            return result;
        }
        catch (Exception e)
        {
            throw new SecurityException("problem reading entropy pool");
        }
    }

    private SecretKey generateKEK(int keylen)
    {
        byte[] b = null;
        byte[] s = null;
        MessageDigest md = null;
        SecretKey k = null;
        try
        {
            b = readPool();
            s = getStampBytes();

            // TODO do key expansion with stamp and entropy
            // Just doing a simple digest for the moment.

            md = MessageDigest.getInstance("SHA-256");
            md.update(s);
            s = md.digest(b);

            // truncate key bits to specified size
            int keyBytes = keylen / 8;
            if (keyBytes > s.length)
            {
                throw new IllegalArgumentException("unsupported key length");
            }
            k = new SecretKeySpec(s, 0, keyBytes, "AES");
        }
        catch (SecurityException x)
        {
            throw x;
        }
        catch (Exception e)
        {
            throw new SecurityException("problem reading entropy pool", e);
        }
        finally
        {
            if (null != md)
                md.reset();
            wipe(b);
            wipe(s);
        }
        return k;
    }

    private static void wipe(byte[] b)
    {
        if (null != b)
        {
            Arrays.fill(b, (byte) 0);
        }
    }

    /**
     * Convert byte array to a string consisting of the uppercase hexadecimal encoding of the bytes. <br/>
     * <strong>Warning!</strong> Do not convert sensitive data into String. String is immutable and cannot be zeroed.
     * 
     * @param bytes
     *            byte array to be converted to a string
     * @return String containing the hexadecimal encoding of the byte array
     */
    public static String bytesToHex(byte[] bytes)
    {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
