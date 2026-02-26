/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.gui;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;

public class Crypto {

	static final String key = "@#" + "Guardium" + "1qa!QA";
	
	private static Cipher ecipher;
	private static Cipher dcipher;
	
	/*
	SecretKey key = KeyGenerator.getInstance("DES").generateKey();
	AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
	ecipher = Cipher.getInstance("DES/CBC/PKCSPadding");
	dcipher = Cipher.getInstance("DES/CBC/PKCSPadding");
	ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
	dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
	*/

	public static void init () throws GeneralSecurityException {
		byte[] raw = key.getBytes(Charset.forName("UTF-8"));
		if (raw.length != 16) {
			throw new IllegalArgumentException("Invalid key size.");
		}
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ecipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
		
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			dcipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return;
	}


    public static void main(String[] args) {
        String teststr = "Today is Monday 1234567890";
        try {
        	init();
        	System.out.println ("orignal: " + teststr);
        	String encstr = encrypt(teststr);
        	System.out.println ("encrypt: " + encstr);
        	String decstr = decrypt(encstr);
        	System.out.println ("decrypt: " + decstr);
        }
        catch (Exception e) {
			System.out.println("Invalid Key:" + e.getMessage());
			return;        	
        }
        
        String filepath = "/var/tmp/dump/";
        ArrayList <String> filelist = new ArrayList<String> ();
        filelist.add(filepath + "alias.dump");
        filelist.add(filepath + "avail_test.dump");
        filelist.add(filepath + "cve_fix.dump");
        filelist.add(filepath + "cve_reference.dump");
        filelist.add(filepath + "dbdriver.dump");
        filelist.add(filepath + "group_desc.dump");
        filelist.add(filepath + "group_member.dump");
        filelist.add(filepath + "group_type.dump");
        filelist.add(filepath + "sqlbased.dump");
        
        // test for file
        for (String name : filelist) {
        	String inputFile = name;
        	String encFile = inputFile + "_enc";
        	String decFile = inputFile + "_dec";
        	String newinputFile = inputFile + "_new";
        	try {
        		encrypt(new FileInputStream(inputFile), new FileOutputStream(encFile));
        		decrypt(new FileInputStream(encFile), new FileOutputStream(newinputFile));

        	}
        	catch (FileNotFoundException e) {
        		System.out.println("File Not Found:" + e.getMessage());
        		return;
        	}
        	catch (Exception e) {
        		System.out.println("Invalid Key:" + e.getMessage());
        		return;
        	}
        }
    	
    }

    public static String encrypt(String value)
            throws GeneralSecurityException {

         byte[] enc = ecipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
         //return new sun.misc.BASE64Encoder().encode(enc);
         return DatatypeConverter.printBase64Binary(enc);
    }

    public static String decrypt(String encrypted)
            throws GeneralSecurityException {
    	
        byte []  dec;
        byte [] original;
        try {
            //dec = new sun.misc.BASE64Decoder().decodeBuffer(encrypted);
            dec = DatatypeConverter.parseBase64Binary(encrypted);
            original = dcipher.doFinal(dec);
        }
        catch ( Exception e ) {
            return "Invalid Encoded string";
        }

        return new String(original, Charset.forName("UTF-8"));
    }
    
	public static void encrypt(InputStream is, OutputStream os) {
		try {
			byte[] buf = new byte[1024];
			// bytes at this stream are first encoded
			os = new CipherOutputStream(os, ecipher);
			// read in the clear text and write to out to encrypt
			int numRead = 1024;
			while ((numRead = is.read(buf)) >= 0) {
				os.write(buf, 0, numRead);
			}
			// close all streams
			os.close();
		}

		catch (IOException e) {
			System.out.println("I/O Error:" + e.getMessage());
		}

	}

	public static void decrypt(InputStream is, OutputStream os) {
		try {
			byte[] buf = new byte[1024];
			// bytes read from stream will be decrypted
			CipherInputStream cis = new CipherInputStream(is, dcipher);
			// read in the decrypted bytes and write the clear text to out
			int numRead = 0;
			while ((numRead = cis.read(buf)) >= 0) {
				os.write(buf, 0, numRead);
			}
			
			// close all streams
			cis.close();
			is.close();
			os.close();
		}

		catch (IOException e) {
			System.out.println("I/O Error:" + e.getMessage());
		}
	}
	
	

}