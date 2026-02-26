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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyRep;
import java.security.KeyRep.Type;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.guardium.connector.common.crypto.masterkeys.MasterKey;
import com.guardium.connector.common.exceptions.EncryptionException;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

/**
 * Note well: This class requires the unrestricted encryption jars to support AES/CGM key size of 256. See:
 * https://www.ibm.com/support/knowledgecenter/en/SSYKE2_7.1.0/com.ibm.java.security.component.71.doc/security-component/sdkpolicyfiles.html
 * 
 * @author abecher
 *
 */
public class EncryptionUtils
{
    //    private static byte[] SALT =
    //    {
    //            72, 117, 75, 52, 110, 52, 48, 116,
    //            97, 109, 66, 51, 122, 48, 49, 72,
    //            51, 109, 72, 97, 121, 117, 77, 49,
    //            51, 48, 116, 66, 104, 48, 49
    //    };

    private static byte[] IV =
    {
            0x0f, 0x01, 0x03, 0x02, 0x04,
            0x05, 0x06, 0x0e, 0x08, 0x09,
            0x0a, 0x0b, 0x0c, 0x0d, 0x07,
            0x01
    };

    private static final int AES_KEY_SIZE = 256; // in bits
    private static final int GCM_NONCE_LENGTH = 12; // in bytes
    private static final int GCM_TAG_LENGTH = 16; // in bytes

    private KeyPair keyPair;
    private Cipher aes_cbc_encrypt;
    private Cipher aes_cbc_decrypt;
    private Cipher rsaEncCipher;
    private Cipher rsaDecCipher;

    private static EncryptionUtils instance = null;

    private EncryptionUtils() throws EncryptionException
    {
        initAESCBCEncryptionCiphers();
        keyPair = initAsymmetricEncryptionCiphers();
    }

    private EncryptionUtils(KeyPair keys) throws EncryptionException
    {
        initAESCBCEncryptionCiphers();
        keyPair = keys;
    }

    private void initAESCBCEncryptionCiphers() throws EncryptionException
    {
        try
        {
            SecretKey key = new MasterKey(SecureConnectorDefaultProperties.getDBDir().resolve(SecureConnectorDefaultProperties
                    .getEntropyFileName())).getMasterKey();
            IvParameterSpec ivSpec = new IvParameterSpec(IV);

            // initialize the symmetric encryption cipher
            aes_cbc_encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes_cbc_encrypt.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            // initialize the symmetric decryption cipher
            aes_cbc_decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aes_cbc_decrypt.init(Cipher.DECRYPT_MODE, key, ivSpec);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e)
        {
            throw new EncryptionException("Could not initialize cipher " + e.getMessage());
        }
    }

    public static SecretKey createPBKDF2Key(String secret, byte[] salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        // get the key from password and salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keyspec = new PBEKeySpec(secret.toCharArray(), salt, 65536, AES_KEY_SIZE);
        SecretKey tmp = factory.generateSecret(keyspec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private KeyPair initAsymmetricEncryptionCiphers() throws EncryptionException
    {
        // see this fun stuff: https://www.ibm.com/developerworks/community/forums/html/topic?id=77777777-0000-0000-0000-000014555794#77777777-0000-0000-0000-000014555794
        // so according to us, one can only decrypt data with a private key :(
        try
        {
            KeyPair keys = generateKeys();

            rsaEncCipher = Cipher.getInstance("RSA");
            rsaEncCipher.init(Cipher.ENCRYPT_MODE, keys.getPublic());

            rsaDecCipher = Cipher.getInstance("RSA");
            rsaDecCipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());

            return keys;
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            throw new EncryptionException("Could not initialize cipher " + e.getMessage());
        }
    }

    public static EncryptionUtils instance() throws EncryptionException
    {
        if (null == instance)
            instance = new EncryptionUtils();
        return instance;
    }

    public static EncryptionUtils instance(KeyPair keys) throws EncryptionException
    {
        if (null == instance)
            instance = new EncryptionUtils(keys);
        return instance;
    }

    public KeyPair getKeys()
    {
        return keyPair;
    }

    public PublicKey getPublicKey()
    {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey()
    {
        return keyPair.getPrivate();
    }

    //    private Key createAES256BitKey() throws NoSuchAlgorithmException, InvalidKeySpecException, EncryptionException
    //    {
    //        PBEKeySpec pbeKeySpec;
    //        try
    //        {
    //            pbeKeySpec = new PBEKeySpec(Vault.getSecret().toCharArray(), SALT, 50, AES_KEY_SIZE);
    //        }
    //        catch (VaultException e)
    //        {
    //            throw new EncryptionException("encryption.key.error" + e.getMessage());
    //        }
    //
    //        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
    //        return new SecretKeySpec(keyFactory.generateSecret(pbeKeySpec).getEncoded(), "AES");
    //    }

    public static String encryptAESGCM(String secret, String aad, String value) throws IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IOException
    {
        return base64Encode(encryptAESGCM(secret, aad, value.getBytes("UTF-8")));
    }

    public static byte[] encryptAESGCM(String secret, String aad, byte[] value) throws IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IOException
    {
        // generate some random salt
        final byte[] salt = new byte[64];
        SecureRandom random = SecureRandom.getInstanceStrong();
        random.nextBytes(salt);

        // get the key from the secret and salt
        SecretKey key = createPBKDF2Key(secret, salt);

        // generate the iv (called nonce in the case of AES/CGM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final byte[] nonce = new byte[GCM_NONCE_LENGTH];
        random.nextBytes(nonce);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        // add additional authenication data
        byte[] aadBytes = aad.getBytes();
        cipher.updateAAD(aadBytes);

        // encrypte the data
        byte[] cipherText = cipher.doFinal(value);

        // Return the encrypted Text
        // N.B. this is non-standard format -- eventually CMS will fix that but for now
        // we have our own format where we put tag at the end to be more compatible with the way the Java
        // cipher works - which automatically puts the auth tag at the end of the cipher text
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(nonce);
        outputStream.write(cipherText);  // Java puts the auth tag as the last 16 bytes after the cipherText
        return outputStream.toByteArray();
    }

    public static String decryptAESGCM(String secret, String aad, String encodedStr)
            throws UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
    {
        byte[] decrypted = decryptAESGCM(secret, aad, base64Decode(encodedStr));
        return new String(decrypted, 0, decrypted.length, "UTF-8");
    }

    public static byte[] decryptAESGCM(String secret, String aad, byte[] value) throws NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(value);

        // see the encryptAESGCM method for the format of the value

        // get the salt bytes
        byte[] salt = new byte[64];
        inputStream.read(salt, 0, 64);

        // get the iv (nonce)
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        inputStream.read(nonce, 0, GCM_NONCE_LENGTH);

        // get the cipherText (which includes the auth tag at the end)
        byte[] cipherText = new byte[value.length - (64 + 12)];
        inputStream.read(cipherText, 0, value.length - (64 + 12));

        // get the key from the secret and salt
        SecretKey key = createPBKDF2Key(secret, salt);

        // setup the cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        // set the aad for authentication
        cipher.updateAAD(aad.getBytes());

        // decrypt the data
        byte[] plainText = cipher.doFinal(cipherText);

        return plainText;
    }

    public synchronized String encrypt(String value) throws EncryptionException, NoSuchAlgorithmException
    {
        try
        {
            return base64Encode(encrypt(value.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.encrypt.error", e.getMessage());
        }
    }

    public synchronized byte[] encrypt(byte[] value) throws EncryptionException, NoSuchAlgorithmException
    {
        try
        {
            // generate some random salt
            final byte[] salt = new byte[16];
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
            byte[] concat = new byte[salt.length + value.length];
            System.arraycopy(salt, 0, concat, 0, salt.length);
            System.arraycopy(value, 0, concat, salt.length, value.length);
            byte[] encrypted = new byte[aes_cbc_encrypt.getOutputSize(concat.length)];
            int len = aes_cbc_encrypt.update(concat, 0, concat.length, encrypted, 0);
            len += aes_cbc_encrypt.doFinal(encrypted, len);
            byte[] finalValue = new byte[len];
            System.arraycopy(encrypted, 0, finalValue, 0, len);
            return finalValue;
        }
        catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e)
        {
            throw new EncryptionException("encryption.encrypt.error" + e.getMessage());
        }
    }

    public synchronized String decrypt(String encodedStr) throws EncryptionException
    {
        try
        {
            byte[] decrypted = decrypt(base64Decode(encodedStr));
            return new String(decrypted, 0, decrypted.length, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.encrypt.error", e.getMessage());
        }
    }

    public synchronized byte[] decrypt(byte[] value)
    {
        try
        {
            final byte[] salt = new byte[16];
            byte[] decrypted = new byte[aes_cbc_decrypt.getOutputSize(value.length)];
            int len = aes_cbc_decrypt.update(value, 0, value.length, decrypted, 0);
            len += aes_cbc_decrypt.doFinal(decrypted, len);
            byte[] finalValue = new byte[len - salt.length];
            System.arraycopy(decrypted, salt.length, finalValue, 0, len - salt.length);
            return finalValue;
        }
        catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e)
        {
            throw new RuntimeException("Data encryption failed. " + e.getMessage(), e);
        }
    }

    private static synchronized String base64Encode(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static synchronized byte[] base64Decode(String property)
    {
        return Base64.getDecoder().decode(property);
    }

    private KeyPair generateKeys() throws EncryptionException
    {
        KeyPairGenerator keyGen;
        try
        {
            keyGen = KeyPairGenerator.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException e)
        {
            // should never get this
            throw new EncryptionException("encryption.key.error", e.getMessage());
        }

        keyGen.initialize(2048);
        return keyGen.generateKeyPair();

        // PrivateKey priv = keyPair.getPrivate();
        // PublicKey pub = keyPair.getPublic();
        // String privateKey = new String(Base64.encode(priv.getEncoded(), 0, priv.getEncoded().length, Base64.NO_WRAP));
        // String publicKey1 = new String(Base64.encode(pub.getEncoded(), 0, pub.getEncoded().length, Base64.NO_WRAP));
        // String publicKey = new String(Base64.encode(publicKey1.getBytes(), 0, publicKey1.getBytes().length, Base64.NO_WRAP));
    }

    public String sign(String value) throws EncryptionException
    {
        try
        {
            return base64Encode(sign(value.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.sign.error", e.getMessage());
        }
    }

    public byte[] sign(byte[] value) throws EncryptionException
    {
        try
        {
            Signature rsa = Signature.getInstance("SHA512withRSA");
            rsa.initSign(getKeys().getPrivate());
            rsa.update(value);
            return rsa.sign();
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e)
        {
            throw new EncryptionException("encryption.sign.error", e.getMessage());
        }
    }

    public boolean verify(String signature, String toBeVerified) throws EncryptionException
    {
        try
        {
            return verify(signature, toBeVerified.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.verify.error", e.getMessage());
        }
    }

    /*
     * Verifies (base64 encoded) signed content (with Public Key using a SHA256withRSA signature) Returns true if verifies. otherwise false
     */
    public boolean verify(String signature, byte[] toBeVerified) throws EncryptionException
    {
        try
        {
            Signature rsa = Signature.getInstance("SHA512withRSA");
            rsa.initVerify(keyPair.getPublic());
            rsa.update(toBeVerified);
            return rsa.verify(base64Decode(signature));
        }
        catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e)
        {
            throw new EncryptionException("encryption.verify.error", e.getMessage());
        }
    }

    public String encryptWithKey(String value) throws EncryptionException
    {
        try
        {
            return base64Encode(encryptWithKey(value.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.encrypt.error", e.getMessage());
        }
    }

    /*
     * encrypts a byte[] then signs it with the public key
     */
    public byte[] encryptWithKey(byte[] bytes) throws EncryptionException
    {
        try
        {
            return rsaEncCipher.doFinal(bytes);
        }
        catch (BadPaddingException | IllegalBlockSizeException e)
        {
            throw new EncryptionException("encryption.encrypt.error", e.getMessage());
        }
    }

    public String decryptWithKey(String encodedStr) throws EncryptionException
    {
        try
        {
            byte[] decrypted = decryptWithKey(base64Decode(encodedStr));
            return new String(decrypted, 0, decrypted.length, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new EncryptionException("encryption.decrypt.error", e.getMessage());
        }
    }

    /*
     * takes a signed (with public key) content in the form of byte array and decrypts it
     */
    public byte[] decryptWithKey(byte[] bytes) throws EncryptionException
    {
        try
        {
            return rsaDecCipher.doFinal(bytes);
        }
        catch (BadPaddingException | IllegalBlockSizeException e)
        {
            throw new EncryptionException("encryption.decrypt.error", e.getMessage());
        }
    }

    public String getPublicKeyAsString() throws IOException
    {
        return getKeyAsString(getPublicKey(), Type.PUBLIC);
    }

    public String getPublicKeyModulus()
    {
        byte[] modBuf = ((RSAPublicKey) getPublicKey()).getModulus().toByteArray();
        return Base64.getEncoder().encodeToString(modBuf);
    }

    public String getPublicKeyExponent()
    {
        byte[] expBuf = ((RSAPublicKey) getPublicKey()).getPublicExponent().toByteArray();
        return Base64.getEncoder().encodeToString(expBuf);
    }

    public String getPrivateKeyAsString() throws IOException
    {
        return getKeyAsString(getPrivateKey(), Type.PRIVATE);
    }

    public static String getKeyAsString(Key key, java.security.KeyRep.Type type) throws IOException
    {
        KeyRep keyRep = new KeyRep(type, key.getAlgorithm(), key.getFormat(), key.getEncoded());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(keyRep);
        oos.close();

        // encode the string using base 64
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    public static Key getKeyFromString(String keyString) throws IOException, ClassNotFoundException
    {
        // reconstruct the public key
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(keyString.getBytes()));
        ObjectInputStream ois = new ObjectInputStream(bis);
        Key key = (Key) ois.readObject();
        ois.close();
        return key;
    }
}
