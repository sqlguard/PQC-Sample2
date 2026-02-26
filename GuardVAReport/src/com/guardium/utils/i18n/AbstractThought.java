/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils.i18n;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;

import com.guardium.utils.Check;
import com.guardium.utils.Regexer;

/**
 * Statically manages resource bundles and provides a facility for symbolic literal replacement
 * within the String resources to aid in I18n of messages presented to the user.
 * All symbolic replacements should always be derived using dynamic data that is provided at run
 * time from some source beyond the scope of translation such as database data or
 * thrown messages by external libraries.  String literal values should never be passed because
 * These will not be translated for the language of the locale.
 * @author dtoland on Jul 31, 2006 at 3:33:57 PM
 */
public abstract class AbstractThought {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(AbstractThought.class);

	/** Constant for common properties file  */
	public static final transient String THIS_PROPS = "com.guardium.utils.i18n.CommonResources";

	/** Constant arrow */
	public static final transient String ARROW = "->";
	/** Constant colon */
	public static final transient String CLN = ": ";
	/** Constant comma */
	public static final transient String COMMA = ",";
	/** Constant dot */
	public static final transient String DASH = "-";
	/** Constant dot */
	public static final transient String DOT = ".";
	/** Constant for an elipsis */
	public static final transient String ELIPSIS = "...";
	/** Constant empty String */
	public static final transient String EMPTY = "";
	/** Constant equals sign */
	public static final transient String EQUALS = "=";
	/** Constant "Not Applicable" */
	public static final transient String NA = what(THIS_PROPS, "info.not.applicable");
  /** Constant new line for current system */
  public static final transient String NL = System.getProperty("line.separator");
  /** Constant new line for MS */
  public static final transient String NLMS = "\r\n";
  /** Constant new line for UNIX / LINUX */
  public static final transient String NLNX =  "\n";
	/** Constant Space */
	public static final transient String QT1 = "'";
	/** Constant Space */
	public static final transient String QT2 = "\"";
	/** Constant Space */
	public static final transient String SP = " ";
	/** Constant Slash */
	public static final transient String SLASH = "/";
	/** Constant value for Unknown values */
	public static final transient String UNKNOWN = what(THIS_PROPS, "info.unknown");

	/** Constant for a symbol to separate lists of items */
	public static final transient String LIST_SEP = what(
			THIS_PROPS, InformationalMessages.INFO_LIT_LIST_SEP
	);

	/**
	 * @param resourceName The name of the resource bundle properties.
	 *  For example: \"com.guardium.classifier.classifier-resources\"
	 * @return The resource bundle for the name.  Will be created and cached if it does not yet exist.
	 *  Null if cannot be found in file system.
	 */
	protected static ResourceBundle findBundle(String resourceName) {
		ResourceBundle bundle = null;
		try {
			Locale locale = getLocale();
			bundle = !"ww".equalsIgnoreCase(locale.getLanguage())? ResourceBundle.getBundle(resourceName) : ResourceBundle.getBundle(resourceName, locale);

		// couldn't find the bundle in the file system
		} catch (MissingResourceException e) {
			//LOG.error("Could not load resource: '" + resourceName + "'.");
			String msg = "Could not load resource: '" + resourceName + "'.";
		}
		return bundle;
	}
	
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
	 * @param resourceName The name of the resource bundle properties.
	 * @param key The key to the value in the resources file.
	 * @return The value for the key in the resources file.
	 */
	protected static String what(String resourceName, String key) {
		ResourceBundle bundle = findBundle(resourceName);

		if (bundle==null) {
			// avoid a loop
			if ( THIS_PROPS.equals(resourceName) ) {
				return "Could not load common resources: " + THIS_PROPS + " for: " + key;
			}

			// resource describes problem loading key
			return AbstractThought.what(
					THIS_PROPS, PropertyMessages.PROP_MSG_NO_PROPS_FOR_KEY,
					PropertyMessages.PROP_SUB_FILE_NAME, resourceName,
					PropertyMessages.PROP_SUB_KEY, key
			);
		}

		try {
			String value = bundle.getString(key);
			return value;
		} catch (MissingResourceException e1) {

			return AbstractThought.what(
					THIS_PROPS, PropertyMessages.PROP_MSG_NO_KEY,
					PropertyMessages.PROP_SUB_FILE_NAME, resourceName,
					PropertyMessages.PROP_SUB_KEY, key
			);
		}
	}

	/**
	 * @param resourceName The name of the resource bundle properties.
	 * @param key The key to the value in the resources file.
	 * @return The value for the key in the resources file if found. If not found, return null.
	 */
	protected static String whatNull(String resourceName, String key) {
		try {
			return whatException(resourceName, key);
		} catch (MissingResourceException e1) {
			return null;
		}		
	}

	/**
	 * @param resourceName The name of the resource bundle properties.
	 * @param key The key to the value in the resources file.
	 * @return The value for the key in the resources file. If not found, then throw an exception
	 */
	protected static String whatException(String resourceName, String key) throws MissingResourceException {
		ResourceBundle bundle = findBundle(resourceName);

		if (bundle==null) {
			// avoid a loop
			if ( THIS_PROPS.equals(resourceName) ) {
				return "Could not load common resources: " + THIS_PROPS + " for: " + key;
			}

			// resource describes problem loading key
			return AbstractThought.what(
					THIS_PROPS, PropertyMessages.PROP_MSG_NO_PROPS_FOR_KEY,
					PropertyMessages.PROP_SUB_FILE_NAME, resourceName,
					PropertyMessages.PROP_SUB_KEY, key
			);
		}

		try {
			String value = bundle.getString(key);
			return value;
		} catch (MissingResourceException e1) {
			throw e1;
		}
	}

	/**
	 * Uses the symbolics map to replace all instances of a string with a dynamic value
	 * passed in the map.  The order of the list does not matter.
	 * Static values should not be passed as symbolics to the string, they will be impossible to
	 * translate.  Symbolics in the resource string will be delimited by a starting value of
	 * Message.OPEN and an ending value of Message.CLOSE.
	 * @param resourceName The name of the resource bundle properties.
	 * @param key The key to the value in the resources file.
	 * @param symbolics A map of values to replace in the resource string.
	 * @return The value for the key in the resources file.
	 */
	protected static String what(String resourceName, String key, Map<String,String> symbolics) {
		String message = what(resourceName, key);
		if ( Check.isEmpty(message) ) return message;

		Iterator<String> it = symbolics.keySet().iterator();
		while ( it.hasNext() ) {
			String symbol = it.next();
			String replacement = symbolics.get( symbol);
			message = Regexer.replaceSymbolic(message, symbol, replacement);
		}
		return message;
	}

	protected static String what(String resourceName, String key, String ... args) {
		String result = what(resourceName, key);
		
		if ( Check.isEmpty(result) || args.length < 1) {
			return result;
		}
		
		assert(args.length % 2 == 0);
		
		for (int ia = 0; ia < args.length; ia += 2) {
			result = Regexer.replaceSymbolic(result, args[ia], args[ia+1]);
		}

		return result;
	}
}
