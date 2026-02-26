/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guardium.utils.AdHocLogger;

/**
 * This class will parse command lines into the individual args to be passed to Runtime.exec(String []);
 * At this time it can handle quoted or double-quoted arguments as well as environment variables.
 */
public class WordPattern {
	static final char doubleQuote = '"';
	static final char singleQuote = '\'';
	
	protected final Pattern pattern;
	protected final Word wordAlternatives[];
	
	private final Pattern variablePattern;
	private final RegexAlternative varAlternatives[];
    
    public WordPattern() {
    	wordAlternatives = new Word[] {
			new Word(1, doubleQuote), //the group numbering is 1-based, not 0-based
			new Word(2, singleQuote),
			new Word(3)
		};
    	pattern = Pattern.compile(createFullPattern(wordAlternatives));
		
    	varAlternatives = new RegexAlternative[] {
			new RegexAlternative(1, "\\$\\{(\\w+)}"), 
			new RegexAlternative(2, "\\$(\\w+)")
		};
		variablePattern = Pattern.compile(createFullPattern(varAlternatives));
    }
    
    /**
    Override this to determine behaviour when a variable is not found
    
    Generally, the options might be:
    1. replace with empty string
    2. replace it - with replacement
    3. replace it - with original 
    4. replace it - with original var name
    5. leave as is
    6. Search for the variable somewhere else
    
    The default behaviour is option 1.
    */
    String getNotFound(String variable, String original) {
    	return "";
    }
    
    private String createFullPattern(RegexAlternative alternatives[]) {
    	StringBuilder regex = new StringBuilder();
		for(RegexAlternative alternative : alternatives) {
			if(regex.length() > 0) {
				regex.append('|');
			}
			regex.append(alternative.pattern);
		}
		return regex.toString();
    }
    
    boolean isReplaceableVariable(String variable) {
    	if(variable == null || variable.length() == 0) {
    		return false;
    	}
    	//be specific about which variables we are interested in
    	return variable.toUpperCase().startsWith("GUARD");
    }
    
    String replaceEnvVariables(String value) {
    	StringBuffer builder = null;
		Matcher matcher = variablePattern.matcher(value);
		while(matcher.find()) {
			if(builder == null) {
				builder = new StringBuffer();
			}
			for(RegexAlternative alternative : varAlternatives) {
				String string = matcher.group(alternative.groupNumber);
				if(string != null) {
					String replacement;
					if(isReplaceableVariable(string)) {
						replacement = System.getenv(string);
						if(replacement == null) {
							String original = value.substring(matcher.start(), matcher.end());
							replacement = getNotFound(string, original);
						}
					} else {
						String original = value.substring(matcher.start(), matcher.end());
						replacement = original;
					}
					replacement = Matcher.quoteReplacement(replacement);
					matcher.appendReplacement(builder, replacement);
					break;
				}
			}
		}
		if(builder != null) {
			matcher.appendTail(builder);
			value = builder.toString();
		}
		return value;
	}
    
    protected void outputDebugVariableNotFound(String variableNotFound) {
    	if(isReplaceableVariable(variableNotFound)) {
	    	String message = "Guardium environment variable " + variableNotFound + " not found";
	    	AdHocLogger.logDebug(message, AdHocLogger.LOG_DEBUG);
	    	System.err.println(message);
	    	Map<String, String> allVars = System.getenv();
	    	Set<Map.Entry<String, String>> varSet = allVars.entrySet();
	    	Iterator<Map.Entry<String, String>> iterator = varSet.iterator();
	    	boolean found = false;
	    	while(iterator.hasNext()) {
	    		Map.Entry<String, String> next = iterator.next();
	    		String var = next.getKey();
	    		if(var.contains("GUARD")) {
	    			found = true;
	    			String value = next.getValue();
	    			message = "Found Guardium variable " + var + " defined as: " + value;
	    			AdHocLogger.logDebug(message, AdHocLogger.LOG_DEBUG);
	    	    	System.err.println(message);
	    		}
	    	}
	    	if(!found) {
	    		message = "No Guardium location variables are defined";
				AdHocLogger.logDebug(message, AdHocLogger.LOG_DEBUG);
		    	System.err.println(message);
	    	}
    	}
    }
}