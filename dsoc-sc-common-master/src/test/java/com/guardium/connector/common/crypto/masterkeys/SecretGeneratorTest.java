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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class SecretGeneratorTest extends MasterKeyTest
{
    @Test
    public void testGenKeyMaterialLength(TestInfo testInfo) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalStateException, IOException
    {
        // for testing, the master key will serve as a DEK
        SecretKey mk = new MasterKey(dbPath.resolve("keyfile")).getMasterKey();

        // test SecretGenerator.genKeyMaterial
        char[] alias = getTestPath(testInfo).toString().toCharArray();
        byte[] sg1 = SecretGenerator.genKeyMaterial(alias, mk, 256);
        assertTrue(sg1.length == 256 / 8);
    }

    @Test
    public void testGenPasswordLength(TestInfo testInfo) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalStateException, IOException
    {
        SecretKey mk = new MasterKey(dbPath.resolve("keyfile")).getMasterKey();
        char[] alias = getTestPath(testInfo).toString().toCharArray();
        char[] pwd = SecretGenerator.genPassword(alias, mk, 15);
        assertTrue(pwd.length == 15);

        pwd = SecretGenerator.genPassword(alias, mk, 30);
        assertTrue(pwd.length == 30);
    }

    @Test
    public void testGenPasswordMinimumEntropy(TestInfo testInfo) throws NoSuchAlgorithmException,
            InvalidKeySpecException, IllegalStateException, IOException
    {
        SecretKey mk = new MasterKey(dbPath.resolve("keyfile")).getMasterKey();
        char[] alias = getTestPath(testInfo).toString().toCharArray();
        char[] pwd = SecretGenerator.genPassword(alias, mk, 15);

        int bits = 0;
        try
        {
            for (bits = 1; bits < 555; bits++)
            {
                pwd = SecretGenerator.genPassword(alias, mk, bits, 7);
                assertTrue(bits <= 42 ? pwd.length == 7 : false);
            }
        }
        catch (IllegalArgumentException i)
        {
            // pop out of for loop when requested entropy bits exceeds pwd length
        }
        assertTrue(bits == 43);
    }

    @Test
    public void testGenPasswordMinimumLength(TestInfo testInfo) throws NoSuchAlgorithmException,
            InvalidKeySpecException, IllegalStateException, IOException
    {
        SecretKey mk = new MasterKey(dbPath.resolve("keyfile")).getMasterKey();
        char[] alias = getTestPath(testInfo).toString().toCharArray();
        char[] pwd = SecretGenerator.genPassword(alias, mk, 15);
        for (int bits = 1; bits < 555; bits++)
        {
            pwd = SecretGenerator.genPassword(alias, mk, bits, -1); // min pwd length
            if (bits % 3 == 0 || bits < 7 || bits > 550)
            {
                assertTrue(pwd.length == (int) Math.ceil((double) bits / 6));
            }
        }
    }
}