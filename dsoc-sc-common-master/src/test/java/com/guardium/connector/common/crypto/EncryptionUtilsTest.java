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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import com.guardium.connector.common.crypto.EncryptionUtils;
import com.guardium.connector.common.exceptions.EncryptionException;

class EncryptionUtilsTest extends com.guardium.test.helpers.TestWithLog {
    @Test
    void testInstance() {
        try {
            EncryptionUtils v = EncryptionUtils.instance();
            assertNotNull(v);
        } catch (EncryptionException e) {
            fail("Exception thrown during instance()");
        }
    }

    @Test
    void testGetKeys() throws EncryptionException {
        EncryptionUtils v = EncryptionUtils.instance();
        KeyPair keys = v.getKeys();
        assertNotNull(keys);
        assertNotNull(keys.getPublic());
        assertNotNull(keys.getPrivate());
    }

    @Test
    void testSymmetricEncryption() throws EncryptionException, NoSuchAlgorithmException {
        EncryptionUtils v = EncryptionUtils.instance();

        // control - test same string every test run
        assertTrue(v.decrypt(v.encrypt("asdf")).equals("asdf"));

        // variation - test a random string every test run
        String randomString = RandomStringUtils.randomAlphanumeric(16);
        assertTrue(v.decrypt(v.encrypt(randomString)).equals(randomString));
    }

    @Test
    void testAsymmetricEncryption() throws EncryptionException {
        EncryptionUtils v = EncryptionUtils.instance();

        // control - test same string every test run
        assertTrue(v.decryptWithKey(v.encryptWithKey("asdf")).equals("asdf"));

        // variation - test a random string every test run
        String randomString = RandomStringUtils.randomAlphanumeric(16);
        assertTrue(v.decryptWithKey(v.encryptWithKey(randomString)).equals(randomString));
    }

    @Test
    void testAESGCMEncryption()
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {
        String secretKey = new String("secret key for encryption and decryption");
        String aad = "aslnvakn3oirn3qo";
        String input = "some text to encrypt";
        assertTrue(EncryptionUtils.decryptAESGCM(secretKey, aad, EncryptionUtils.encryptAESGCM(secretKey, aad, input))
                .equals(input));
    }

    @Test
    void testVerify() throws EncryptionException {
        EncryptionUtils v = EncryptionUtils.instance();

        // control - test same string every test run
        assertTrue(v.verify(v.sign("asdf"), "asdf"));

        // variation - test a random string every test run
        String randomString = RandomStringUtils.randomAlphanumeric(16);
        assertTrue(v.verify(v.sign(randomString), randomString));
    }
}
