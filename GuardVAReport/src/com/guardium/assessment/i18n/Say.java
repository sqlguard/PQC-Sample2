/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.assessment.i18n;

import java.util.Map;

/**
 * @author dtoland on Aug 1, 2006 at 9:18:21 AM
 */
public class Say
extends com.guardium.utils.i18n.AbstractThought
implements AssessmentMessages, CustomAssesmentMessages
{

	/** Constant for the properties file for the resources  */
	private static final transient String PROPS = "com.guardium.assessment.i18n.AssessmentResources";
    public static final String DETAIL_SEP = "\n";

	/**
	 * @param key The key to the value in the resources file.
	 * @return The value for the key in the resources file.
	 */
	public static String what(String key) {
		return what(PROPS, key);
	}

	/**
	 * Uses the symbolics map to replace all instances of a string with a dynamic value
	 * passed in the map.
	 * Static values should not be passed as symbolics to the string, they will be impossible to
	 * translate.  Symbolics in the resource string will be delimited by a starting value of
	 * Message.OPEN and an ending value of Message.CLOSE.
	 * @param key The key to the value in the resources file.
	 * @param symbolics A map of values to replace in the resource string.
	 * @return The value for the key in the resources file.
	 */
	public static String what(String key, Map<String,String> symbolics) {
		return what(PROPS, key, symbolics);
	}

	/**
	 * Convenience method substitutes pair of symbol replacements
	 * @param key The key to the value in the resources file.
	 * @param symbol The symbol to replace in the resource string.
	 * @param replacement The value to replace the symbol in the resource string.
	 * @return The value for the key in the resources file.
	 */
	public static String what(String key, String symbol, String replacement) {
		return what(PROPS, key, symbol, replacement);
	}

	/**
	 * Convenience method substitutes pairs of symbol replacements
	 * @param key The key to the value in the resources file.
	 * @param firstSymbol The symbol to replace in the resource string.
	 * @param firstReplacement The value to replace the symbol in the resource string.
	 * @param secondSymbol The symbol to replace in the resource string.
	 * @param secondReplacement The value to replace the symbol in the resource string.
	 * @return The value for the key in the resources file.
	 */
	public static String what(
			String key,
			String firstSymbol, String firstReplacement,
			String secondSymbol, String secondReplacement
	) {
		return what(
				PROPS, key,
				firstSymbol, firstReplacement,
				secondSymbol, secondReplacement
		);
	}

	/**
	 * Convenience method substitutes pairs of symbol replacements
	 * @param key The key to the value in the resources file.
	 * @param firstSymbol The symbol to replace in the resource string.
	 * @param firstReplacement The value to replace the symbol in the resource string.
	 * @param secondSymbol The symbol to replace in the resource string.
	 * @param secondReplacement The value to replace the symbol in the resource string.
	 * @param thirdSymbol The symbol to replace in the resource string.
	 * @param thirdReplacement The value to replace the symbol in the resource string.
	 * @return The value for the key in the resources file.
	 */
	public static String what(
			String key,
			String firstSymbol, String firstReplacement,
			String secondSymbol, String secondReplacement,
			String thirdSymbol, String thirdReplacement
	) {
		return what(
				PROPS, key,
				firstSymbol, firstReplacement,
				secondSymbol, secondReplacement,
				thirdSymbol, thirdReplacement
		);
	}

	/**
	 * Convenience method substitutes pairs of symbol replacements
	 * @param key The key to the value in the resources file.
	 * @param firstSymbol The symbol to replace in the resource string.
	 * @param firstReplacement The value to replace the symbol in the resource string.
	 * @param secondSymbol The symbol to replace in the resource string.
	 * @param secondReplacement The value to replace the symbol in the resource string.
	 * @param thirdSymbol The symbol to replace in the resource string.
	 * @param thirdReplacement The value to replace the symbol in the resource string.
	 * @param forthSymbol The symbol to replace in the resource string.
	 * @param forthReplacement The value to replace the symbol in the resource string.
	 * @return The value for the key in the resources file.
	 */
	public static String what(
			String key,
			String firstSymbol, String firstReplacement,
			String secondSymbol, String secondReplacement,
			String thirdSymbol, String thirdReplacement,
			String forthSymbol, String forthReplacement
	) {
		return what(
				PROPS, key,
				firstSymbol, firstReplacement,
				secondSymbol, secondReplacement,
				thirdSymbol, thirdReplacement,
				forthSymbol, forthReplacement
		);
	}
}
