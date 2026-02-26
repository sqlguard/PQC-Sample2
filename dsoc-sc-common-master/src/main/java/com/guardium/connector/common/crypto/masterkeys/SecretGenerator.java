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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.xml.bind.DatatypeConverter;

/**
 * Static methods used to generate secret key material and passwords.
 * </p>
 * All methods require the caller supply a 'Key Encrypting Key' (KEK), also called a master key. The methods also
 * require an 'alias' string. The alias is used so that a program can generate the same key material or the same
 * password as often as is needed, by providing the same meaningful symbolic name and the same master key. This prevents
 * the need to save the key or password in a file between runs of the program.
 * </p>
 * Note that the <code>alias</code> parameter given to <code>genPassword</code> will be altered internally so that if
 * the same alias is also given to <code>genKeyMaterial</code>, it will result in different data being generated. This
 * prevents 'decoding' a password obtained using a given alias to obtain key material generated with the same alias
 * value. This also means you can use the same alias for generating key material and a password with no concern about
 * conflict.
 * </p>
 * <strong>Example Use:</strong></br>
 * If you wanted to save a private key in a Java KeyStore (JKS) file, you could make these calls:
 * </p>
 * 
 * <pre>
 * // Generate a private key for encrypting check requests
 * public static final char[]    CHECK_REQ_KEY = "Check Request Key".toCharArray();
 * public static final int    CHECK_REQ_KEYLEN = 512; //bits
 * . . .
 * // Obtain the master key somehow (perhaps using MasterKey singleton)
 * SecretKey masterKey = MasterKey.getInstance().getMasterKey();
 * . . .
 * // Create 512 bits of key material from the alias and master key
 * byte[] ckBytes = SecretGenerator.genKeyMaterial(
 *                        CHECK_REQ_KEY,  // alias 
 *                            masterKey,  // program's master key
 *                     CHECK_REQ_KEYLEN); // number of bits we need
 * // Create secret key from the generated bits
 * SecretKey checkKey = new SecretKeySpec(ckBytes, 0, CHECK_REQ_KEYLEN / 8, "AES");
 * 
 * // Create a password to secure the secret key in a JKS file
 * char[] checkKeyPassword = SecretGenerator.genPassword(
 *                        CHECK_REQ_KEY,  // alias (internally 'PasswordAlias:Check Request Key')
 *                            masterKey,  // program's master key
 *                     CHECK_REQ_KEYLEN,  // want a password with 512 bits of entropy
 *                                   -1); // let method determine password length
 * . . .
 * // make KeyStore calls to save key, and specify the 
 * // char array checkKeyPassword to encrypt it in the key store
 * . . .
 * </pre>
 * 
 * @author roncraig
 *
 */
public class SecretGenerator
{
    public static String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";
    public static int ITERATION_COUNT = 1003;
    public static final char[] PASSWORD_PREFIX = "PasswordAlias:".toCharArray();

    /**
     * Use a data encryption key and alias name to generate a new encryption key. Uses PBKDF2 to generate a key of the
     * requested length. The <code>bitLength</code> parameter <strong>must</strong> be a multiple of 8. The same alias
     * and DEK will produce the same returned encryption key.
     * 
     * @param alias
     *            a name associated with the returned key
     * @param dek
     *            the Data Encryption Key
     * @param bitLength
     *            the length of the returned key, in bits
     * @return a byte array of key material, with <code>(bitLength / 8)</code> bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalArgumentException
     *             if any parameter is null, empty, or illegal length
     */
    public static byte[] genKeyMaterial(char[] alias, SecretKey dek, int bitLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] dekBytes = null;
        if (null == alias || alias.length < 1)
        {
            throw new IllegalArgumentException("alias null or zero length");
        }
        if (null != dek)
        {
            dekBytes = dek.getEncoded();
        }
        if (null == dek || null == dekBytes || dekBytes.length < 1)
        {
            throw new IllegalArgumentException("data encryption key null or zero bytes");
        }
        if (bitLength / 8 < 1 || bitLength % 8 != 0)
        {
            throw new IllegalArgumentException("bitLength too short or not multiple of 8 bits");
        }

        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(alias, dekBytes, ITERATION_COUNT, bitLength);
        SecretKey sk = skf.generateSecret(spec);

        spec.clearPassword();
        wipe(dekBytes);
        byte[] result = sk.getEncoded().clone();
        try
        {
            sk.destroy();
        }
        catch (DestroyFailedException e)
        {
            // just continue. best effort
        }
        return result;
    }

    /**
     * Generate a password of URL-safe, filename-safe Base64 characters with a given length. The same alias and DEK will
     * produce the same returned password.
     * </p>
     * The characters '+' and '/' normally found in Base64 alphabet are replaced by the characters '-' and '_'
     * respectively so they are safe for use in filenames and in URLs without needing to hex encode them.
     * 
     * @param alias
     *            a name associated with the returned key
     * @param dek
     *            the Data Encryption Key
     * @param pwdLength
     *            password length desired
     * @return array of characters from the Base64 alphabet representing the password
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static char[] genPassword(char[] alias, SecretKey dek, int pwdLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        if (null == alias || alias.length < 1)
        {
            throw new IllegalArgumentException("alias null or zero length");
        }
        if (pwdLength < 1)
        {
            throw new IllegalArgumentException("pwdLength less than 1");
        }
        int bitLen = base64CharsToByteCount(pwdLength) * 8;
        // Decorate password alias so that if you both get key material 
        // and also get password using the same alias string, the password won't 
        // be a Base64 encoding of that same key material.  <wink>
        char[] pwdAlias = new char[PASSWORD_PREFIX.length + alias.length];
        System.arraycopy(PASSWORD_PREFIX, 0, pwdAlias, 0, PASSWORD_PREFIX.length);
        System.arraycopy(alias, 0, pwdAlias, PASSWORD_PREFIX.length, alias.length);
        byte[] b = genKeyMaterial(pwdAlias, dek, bitLen);
        char[] result = Arrays.copyOf(DatatypeConverter.printBase64Binary(b).replace('+', '-').replace('/', '_')
                .toCharArray(), pwdLength);
        wipe(b);
        wipe(pwdAlias);
        return result;
    }

    /**
     * Generate a password from Base64 alphabet with a miniumum strength, measured in bits of entropy.</br>
     * If <code>pwdLength</code> is <code>-1</code>, this will return the shortest password that provides at least
     * <code>minEntropyBits</code> of entropy.</br>
     * If <code>pwdLength</code> is zero or greater, this will return a password of the specified length
     * <strong>if</strong> that length contains at least <code>minEntropyBits</code> of entropy.</br>
     * The same alias and DEK will produce the same returned password.
     * 
     * @param alias
     *            a string associated with the returned key.
     * @param dek
     *            the Data Encryption Key
     * @param minEntropyBits
     *            minimum bits of entropy in the returned password
     * @param pwdLength
     *            password length or <code>-1</code>
     * @return array of characters in Base64 alphabet representing the password
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalArgumentException
     *             if specified password length is too small to contain the minimum specified bits of entropy
     * @throws IllegalArgumentException
     *             if specified password length is less than 1 and not -1
     * @throws IllegalArgumentException
     *             if minimum specified entropy is less than 1 bit
     */
    public static char[] genPassword(char[] alias, SecretKey dek, int minEntropyBits, int pwdLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int charCnt = bitsToBase64CharCount(minEntropyBits);
        if (pwdLength != -1 && pwdLength < 1)
        {
            throw new IllegalArgumentException("pwdLength illegal");
        }
        if (minEntropyBits < 1)
        {
            throw new IllegalArgumentException("illegal value for minEntropyBits");
        }
        if (pwdLength != -1 && pwdLength < charCnt)
        {
            throw new IllegalArgumentException("pwdLength too short to provide requested entropy");
        }
        charCnt = Math.max(pwdLength, charCnt);
        return genPassword(alias, dek, charCnt);
    }

    /**
     * Returns the number of pseudorandom Base64 alphabet characters that are needed to provide the given number of bits
     * of entropy.
     * 
     * @param bits
     *            amount of entropy needed
     * @return the number of characters needed
     */
    public static int bitsToBase64CharCount(int bits)
    {
        return (int) Math.ceil((double) bits / 6);
    }

    /**
     * Returns the number of bytes of pseudorandom data needed to be encoded with the Base64 alphabet to generate the
     * given number of output characters.
     * 
     * @param base64CharCount
     *            the number of Base64 characters you want
     * @return bytes you must encode with Base64 to generate the given number of characters
     */
    public static int base64CharsToByteCount(int base64CharCount)
    {
        return (int) (3 * Math.ceil((double) base64CharCount / 4));
    }

    /**
     * Returns the entropy in a properly generated password of Base64 alphabet characters.<br/>
     * This assumes the password is generated with a proper key stretching algorithm (like PBKDF2) to produce a
     * pseudorandom password.
     * 
     * @param base64CharacterCount
     *            number of characters in the password
     * @return entropy, in bits
     */
    public static int base64Entropy(int base64CharacterCount)
    {
        return base64CharacterCount * 6;
    }

    private static void wipe(byte[] b)
    {
        if (null != b)
        {
            Arrays.fill(b, (byte) 0);
        }
    }

    private static void wipe(char[] b)
    {
        if (null != b)
        {
            Arrays.fill(b, (char) '\0');
        }
    }
}
