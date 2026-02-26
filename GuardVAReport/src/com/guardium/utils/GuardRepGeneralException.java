/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/**
 * Created on Feb 27, 2003
 */
package com.guardium.utils;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author dario
 */
public class GuardRepGeneralException extends Exception {
/* Message Id:
 * 		200 - Error In SQL statement.
 * 		201 - Error Retrieving Records from DB.
 * 		202 - SQL Invalid Or not defined.
 * 		203 - Error Retrrieveng SQL.
 * 		204 - Error Replacing Parameters.
 * 		205 - Error Retrieving presentation Data.
 * 		206 - Failed to Retrieve Query.
 * 		207 - Failed to Generate Drill Downs List
 * 		208 - Wrong number of tokens in NOW +/-N DAY/MINUTE/HOUR/MONTH String for date parameter
 * 		209 - Maximum Number of Records allowed for display in a Graphical Report Exceeded
 * 		210 - Can Not Retrieve Group Information (Group Desc record removed from DB)
 * 		211 - Additional Where Clause not for Record Details
 * 		212 - Parameter containing ' " or ; characters, do not allowed to avoid SQL injection.
 * 		213 - Bad relative date format
 * 		214 - Failed to convert relative date to real date
 * 		215 - Additional Where Clause not for ShowSQL drill down
 * 		216 - Error replacing Alises
 * 		217 - Query has been modified, psml parameters differ from report parameters, psml should be re-created.
 * 		218 - Query From Date must be smaller than To date
 * 		220 - Failed to retrieve One Record fromQuery Grouped SQLs for Query ID / Group TYpe ID
 * 		300 - Failed to retrieve access Map parameters
 * 		301 - Access Map Parameters empty
 * 		302 - Failed to execute access map report
 * 		303 - Access Map Report Not Found
 * 		304 - Failed to get from or To date for access map
 * 		305 - Failed to retrieve access Group filter information
 * 		306 - Failed to Translate Aliases
 * 		307 - Failed to Get Column Types
 * 		308 - Falied to Get Additional where Clause for drill down from Access Map 
 * 		700 - Failed to save assessment result header, on setReceivedByAll
 * 		800 - Failed to generate SQL for show SQL drill down
 * 		900 - There is too much data to be fetched; please modify the date-from/date-to and/or other parameters to retrieve a smaller data set.
*/
   // 2010-10-05 sbuschman 22036
	static public final int SqlErrorNanny = 1053,
                           SqlErrorSort = 1028,
	                        SqlErrorNoBaseTable = 1146,
	                        SqlErrorRegExp = 1139;
	
	private int messageId = 0;
	private int sqlErrorCode = -1;
	
	private static Locale locale = getLocale();
	private static ResourceBundle errorDescriptions = !"ww".equalsIgnoreCase(locale.getLanguage()) ? ResourceBundle.getBundle("com.guardium.portal.admin.ApplicationResources") : ResourceBundle.getBundle("com.guardium.portal.admin.ApplicationResources", locale);
	
	private static Locale getLocale() {
		
		try {
			Locale.Builder wwLocaleBuilder = new Locale.Builder();
			wwLocaleBuilder.setLanguage("ww");
			wwLocaleBuilder.setRegion("CN");
			
			final String fname = "com.guardium.portal.admin.InstallationLanguage";
			ResourceBundle res = ResourceBundle.getBundle(fname, wwLocaleBuilder.build());
			
			if (res != null) {
				String country = res.getString("locale.country") ;
				String language = res.getString("locale.language") ;
				
				Locale.Builder builder = new Locale.Builder();
				if (!Check.isEmpty(language)) {
					language = language.trim();
					builder.setLanguage(language);
				}
				if (!Check.isEmpty(country)) {
					country = country.trim();
					builder.setRegion(country);
				}
				
				Locale aLocale = builder.build();
				return aLocale ;
			}
		} catch (Exception e) {
			//Do nothing
		}
		
		//Default to en-US
		return new Locale("en", "US");
	}
	/**
	 * Constructor for GuardReportException.
	 */
	public GuardRepGeneralException() {
		super();
	}

	/**
	 * Constructor for GuardReportException.
	 * @param message
	 */
	public GuardRepGeneralException(String message) {
		super(message);
	}

	/**
	 * Constructor for GuardReportException.
	 * @param message
	 * @param message Id
	 */
	public GuardRepGeneralException(String message, int mId) {
		super(message);
		messageId = mId;
	}

	/**
	 * Constructor for GuardReportException.
	 * @param message
	 * @param cause
	 */
	public GuardRepGeneralException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for GuardReportException.
	 * @param message
	 * @param cause
	 * @param message Id
	 */
	public GuardRepGeneralException(String message, Throwable cause, int mId) {
		super(message, cause);
		messageId = mId;
	}

	/**
	 * Constructor for GuardReportException.
	 * @param cause
	 */
	public GuardRepGeneralException(Throwable cause) {
		super(cause);
	}


	/**
	 * @param sqlEx and SQLException as a consequence of a sql command
	 * @return GuardRepGeneralException populated appropriately
	 * Store the sql error code so it's not lost. See TabularAjaxRec#processQuery.
	 * <p>2010-10-05 sbuschman 22036
	 */
	public static GuardRepGeneralException sqlException(SQLException sqlEx)  {
		int errorCode = sqlEx.getErrorCode();
		int messageId = 0;
		
		switch (errorCode) {
		case SqlErrorNanny:       messageId = 900; break;
		case SqlErrorNoBaseTable: messageId = 901; break;
		case SqlErrorRegExp:      messageId = 1139; break;
		}
		
		GuardRepGeneralException ex;
		
		if (messageId > 0) {
			ex = new GuardRepGeneralException(messageId);
		}
		else  {
			String message = sqlEx.getMessage();
			
			if (message == null) {
				message = sqlEx.getClass().getName();				
			}

			ex = new GuardRepGeneralException(message, sqlEx.getCause(), 200);			
		}
		
		ex.setSqlErrorCode(errorCode);
		return ex;
	}
	
	public GuardRepGeneralException(int mId)
	{
		super(errorDescriptions.getString("report.error."+mId));
		messageId = mId;
	}

	public String toString() {
		String errString = "Guard Report Generator Error: ";
		errString = errString.concat(this.getMessage());
		return errString;
	}

	public int getMessageId() {
		return messageId;
	}
	
	public String getErrorDescription() {
		String errorDesc = "Error in generating report/monitor: <br>";
		String d = (String) errorDescriptions.getString("report.error."+getMessageId());
		if (d != null && !d.trim().equals(""))
			errorDesc = errorDesc + " " + d;
		return errorDesc;	
	}

   // 2010-10-05 sbuschman 22036
	public void setSqlErrorCode(int sqlErrorCode) {
		this.sqlErrorCode = sqlErrorCode;
	}

	public int getSqlErrorCode() {
		return sqlErrorCode;
	}
	
	public boolean isSqlException() {
		return sqlErrorCode > 0;
	}
}
