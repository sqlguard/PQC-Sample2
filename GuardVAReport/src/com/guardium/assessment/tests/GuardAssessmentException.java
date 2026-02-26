/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/**
 * Created on Feb 27, 2003
 *
 * To change this generated comment edit the template variable "filecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of file comments go to
 * Window>Preferences>Java>Code Generation.
 */
package com.guardium.assessment.tests;

import java.util.Locale;
import java.util.ResourceBundle;

import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;

/**
 * @author dario
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GuardAssessmentException extends Exception {
/* Message Id:
 * 		600 - General exception
 * 		601 - Can not retrieve report Header for assessment test.
 * 		602 - Report Error No records found
 * 		603 - General Guard reporting Error
 * 		604 - Can Not instantiate test
 * 		605 - Can not initialize Assessment Result Header
 * 		606 - Invalid Query
 * 		700 - ASsessment result set received by all failed on save
 * 		800 - Can Not instantiate customer test
 */
	final public static int LOGGING_ERROR = 607;
	final public static int INSTANTIATE_TEST_ERROR = 604;
	
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
	private int messageId = 0;
	/**
	 * Constructor for GuardAuditException.
	 */
	public GuardAssessmentException() {
		super();
	}

	/**
	 * Constructor for GuardAuditException.
	 * @param message
	 */
	public GuardAssessmentException(String message) {
		super(message);
	}

	/**
	 * Constructor for GuardAuditException.
	 * @param message
	 * @param message Id
	 */
	public GuardAssessmentException(String message, int mId) {
		super(message);
		messageId = mId;
	}

	/**
	 * Constructor for GuardAuditException.
	 * @param message
	 * @param cause
	 */
	public GuardAssessmentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for GuardAuditException.
	 * @param message
	 * @param cause
	 * @param message Id
	 */
	public GuardAssessmentException(String message, Throwable cause, int mId) {
		super(message, cause);
		messageId = mId;
	}

	/**
	 * Constructor for GuardAuditException.
	 * @param cause
	 */
	public GuardAssessmentException(Throwable cause) {
		super(cause);
	}
	
	public GuardAssessmentException(Throwable cause, int mId) {
		super(cause);
		messageId = mId;
	}

	public String toString() {
		String errString = "Guard Assessment Runner Error: ";
		errString = errString.concat(this.getMessage());
		return errString;
	}

	public int getMessageId() {
		return messageId;
	}	
	
	public static String assembleMsg(Throwable cause, int mId){		
		String d = (String) errorDescriptions.getString("assessment.error."+mId);
		if(d==null)
			d="";
		return d+cause;		
	}
	
	public String getMsgWithType(){
		String d = null;
		try{
			d = (String) errorDescriptions.getString("assessment.error."+getMessageId());
		}catch(Exception e){
			AdHocLogger.logException(e);
		}
		if (d != null)			
			return d+" : "+super.getMessage();
		else
			return super.getMessage();
	}

	/*
	public String getMessage(){
		return getMsgWithType();
	}*/
}
