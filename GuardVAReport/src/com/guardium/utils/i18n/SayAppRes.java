/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.i18n;

import java.util.MissingResourceException;

import com.guardium.utils.Regexer;

public class SayAppRes extends AbstractThought {
	private static final transient String PROPS = "com.guardium.portal.admin.ApplicationResources";
	
	/**
	 * @param key
	 * @return the key in PROPS if found, o/w throw an exception
	 */
	public static String sayException(String key) throws MissingResourceException {
		return whatException(PROPS, key);
	}

	/**
	 * @param key
	 * @return the key in PROPS if found, o/w returns null
	 */
	public static String sayNull(String key) {
		return whatNull(PROPS, key);
	}
	
	/**
	 * @param value Typically a value returned from say
	 * @param args variable number strings to be replaced: e.g., {0}, {1}, {2} ...
	 * @return value with args replaced
	 * 
	 * 2010-10-17 sbuschman 22384 Added for performance
	 */
	public static String replace(String value, String ... args) {
		String result = value;
		
		for (int ia = 0; ia < args.length; ++ia) {
			result = result.replaceAll("\\{" + ia + "\\}", Regexer.escapeMetacharacterForReg(args[ia]));
		}
		
		return result;
	}
		
	public static String sayRes(String bundle, String key, String ... args) {
		return replace(what(bundle, key), args);
	}
	
	public static String say(String key, String ... args) {
		return sayRes(PROPS, key, args);
	}
	
	public static String say(String key, Object ... args) {
		String stringArgs[] = new String[args.length];
		int i = 0;
		for(Object o : args) {
			stringArgs[i++] = o.toString();
		}
		return say(key, stringArgs);
	}
	
	/**
	 * Technically this isn't needed, but it looks like it fails because of reflection.
	 */
	public static String say(String key) {
		return what(PROPS, key);
	}
	
	public static String what(String key, String ... args) {
		return what(PROPS, key, args);
	}	
}
