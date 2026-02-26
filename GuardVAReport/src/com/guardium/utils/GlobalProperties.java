/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.guardium.utils;



import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.log4j.Category;


/**
 * @author guy
 *
 *
 */
public final class GlobalProperties 
{
	public static final String SUN_ARCH_DATA_MODEL = System.getProperty("sun.arch.data.model");
	public static final boolean IS_64BIT_OS = SUN_ARCH_DATA_MODEL != null && SUN_ARCH_DATA_MODEL.indexOf("64") >= 0;
	  
	private static GlobalProperties gb = new GlobalProperties();
	private String dbPassword = null;
	private String sharedSecret = null;
	private static String appRootPath = null;
	/*
	 * This constant is hardcoded representation of the code version 
	 * and must match the value in ADMINCONSOLE_PARAMETER.SQLGUARD_VERSION.
	 * It must be manually updated for every version.
	 */
    public static final String SQLGUARD_CODE_VERSION = "Harrier";
    
    public static boolean isAkitaVersion()
    {
    	return SQLGUARD_CODE_VERSION .equals("Akita");
    }
	
	
	/**
	 * 
	 */
    
	private GlobalProperties() 
	{
		try {
			dbPassword = sharedSecret = FileUtils.getPd();
		} catch (InterruptedException | IOException e) {
			/* During upgrade to Log4j2, change to write to AdHocLogger
			Category cat =Category.getInstance("syslog");
			cat.info("**************************************************************");
			cat.error("Guardium Fatal Error: Guardium keys were not found!");
			cat.info("**************************************************************");
			*/
			
			AdHocLogger.logInfo("**************************************************************");
			AdHocLogger.logError("Guardium Fatal Error: Guardium keys were not found!");
			AdHocLogger.logInfo("**************************************************************");
			
			System.exit(-1);
		}
	}    
    
	/*
	private GlobalProperties() 
	{
		
		super();
				
		try
		{
			FileReader fileReader = new FileReader(FileUtils.getEtc("guardkeys.properties"));
			LineNumberReader lr = new LineNumberReader(fileReader);
			String s = lr.readLine();
			dbPassword =sharedSecret = s.substring(s.indexOf("=")+1).trim();
			lr.close(); 
		}
		catch (Exception e)
		{
			Category cat =Category.getInstance("syslog");
			cat.info("**************************************************************");
			cat.error("Guardium Fatal Error: Guardium keys were not found!");
			cat.info("**************************************************************");
			System.exit(-1);
		}
	}
	*/

	/**
	 * @return
	 */
	public static GlobalProperties getGlobalProperties() 
	{
		return gb;
	}

		
	/**
	 * @return
	 */
	public String getDbPassword() {
		return dbPassword;
	}

	/**
	 * @return
	 */
	public String getSharedSecret() {
		return sharedSecret;
	}


	public static void setAppRootPath(String path) 
	{
		appRootPath = path;
		
	}


	public static String getAppRootPath() 
	{
		return appRootPath;
	}
	public static String getAppRoot() 
	{
		if(appRootPath==null)
			return FileUtils.getTomcatRoot("webapps/ROOT");
		else
			return appRootPath;
	}
	public static Boolean getIs64bit() {
		return IS_64BIT_OS;
	}

		
}
