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

package com.guardium.connector.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.guardium.connector.common.properties.SecureConnectorDefaultProperties;

public class Utils
{
	private static final int MAX_PRINT_BUFFER_SIZE = 2000;
	
    public static Properties loadPopertiesFile(File fileName)
    {
        Properties prop = new Properties();

        InputStream is = null;
        try
        {
            is = new FileInputStream(fileName);
            prop.load(is);
        }
        catch (Exception e)
        {
            // do nothing... the caller should check if Properties is empty
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception b)
                {
                }
            }
        }

        return prop;
    }

    public static String toPrettyFormat(Object obj)
    {
        return toPrettyFormat(obj, false);
    }

    public static String toPrettyFormat(Object obj, boolean disableEscaping)
    {
        Gson gson = null;
        if (disableEscaping)
        {
            gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        }
        else
        {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        String toPrint = gson.toJson(obj);
		if(toPrint.length() > MAX_PRINT_BUFFER_SIZE)
			return toPrint.substring(0,(int)(MAX_PRINT_BUFFER_SIZE/2))+"..."+toPrint.substring(toPrint.length()-(int)(MAX_PRINT_BUFFER_SIZE/2),toPrint.length()-1);
		else 
			return toPrint;
    }

    public static String toPrettyFormat(JsonObject json)
    {
        return toPrettyFormat(json, false);
    }

    public static String toPrettyFormat(JsonObject json, boolean disableEscaping)
    {
        Gson gson = null;
        if (disableEscaping)
        {
            gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        }
        else
        {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        
        String toPrint = gson.toJson(json);
		if(toPrint.length() > MAX_PRINT_BUFFER_SIZE)
			return toPrint.substring(0,(int)(MAX_PRINT_BUFFER_SIZE/2))+"..."+toPrint.substring(toPrint.length()-(int)(MAX_PRINT_BUFFER_SIZE/2),toPrint.length()-1);
		else 
			return toPrint;
		
    }
        
    public static List<Properties> loadProperties(Map<String, String> elements) throws IOException
    {
    	JarFile jarFile = null;
    	List<Properties> ret = new ArrayList<Properties>();
    	for (Map.Entry<String, String> entry : elements.entrySet())
    	{
    		for(String fileX: entry.getValue().split("\\|")) {


    			InputStream inputStream = null;
    			if (((String) entry.getKey()).endsWith(".jar"))
    			{
    				jarFile = new JarFile(entry.getKey());
    				inputStream = jarFile.getInputStream(jarFile.getEntry(fileX));
    			}
    			else
    			{
    				inputStream = Utils.class.getClassLoader().getResourceAsStream(fileX);
    			}

    			if (inputStream != null)
    			{
    				Properties properties = new Properties();
    				properties.load(inputStream);
    				ret.add(properties);
    			}
    			else
    			{
    				// logger.error("[Utils::loadProperties] Can't get input stream from " + entry.getValue());
    			}
    			if (null != jarFile)
    			{
    				try
    				{
    					jarFile.close();
    				}
    				catch (IOException e)
    				{
    				}
    			}

    			if (inputStream != null)
    			{
    				try
    				{
    					inputStream.close();
    				}
    				catch (IOException e)
    				{
    				}
    			}
    		}
    	}
    	return ret;
    }
    
    private static final int BUFFER_SIZE = 4096;

	public static void unzipFromStream(InputStream inputStream, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		try(ZipInputStream zipIn = new ZipInputStream(inputStream)) {
			ZipEntry entry = zipIn.getNextEntry();

			while (entry != null) {
				String filePath = destDirectory + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					extractFile(zipIn, filePath);
				} else {
					File dir = new File(filePath);
					dir.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		}
	}

	public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			bos.close();
		}
    }
    
	public  static <T extends OutputStream> T writeToTarget(InputStream in, T out)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		for (int r = in.read(buffer); r != -1; r = in.read(buffer)) {
			out.write(buffer, 0, r);
		}
		return out;
	}
	
	
	public static boolean isChksumVerified(byte [] bytes, String checksum) {
		MessageDigest md = null;
		String version = SecureConnectorDefaultProperties.getDownloadHashVersion();
		try {
			if (version.equals("v1"))
				md = MessageDigest.getInstance("MD5");
			else
				md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
		
	    md.update(bytes);
	    byte[] digest = md.digest();
	    String myChecksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
	         
	    return myChecksum.equals(checksum.toUpperCase());
	}
	
	public static boolean deleteDirectory(File dirToBeDeleted) {
	    File[] allContents = dirToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return dirToBeDeleted.delete();
	}
}
