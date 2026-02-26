/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.util.regex.Matcher;

/**
 * A pattern for a resource on the file system
 */
public class ResourcePattern extends WordPattern {

	 @Override
	protected String getNotFound(String variable, String original) {
    	outputDebugVariableNotFound(variable);
    	return original;
    }
	
    public String parse(String cmd) {
    	Matcher matcher = pattern.matcher(cmd);
		if(matcher.find()) {
			for(Word word : wordAlternatives) {
				String string = matcher.group(word.groupNumber);
				if(string != null) {
					if(word.encloser != singleQuote) {
						string = replaceEnvVariables(string);
					}
					return string;
				}
			}
		}
		return null;
    }
}