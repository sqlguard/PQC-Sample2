/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * This class will parse command lines into the individual args to be passed to Runtime.exec(String []);
 * At this time it can handle quoted or double-quoted arguments as well as environment variables.
 */
public class CommandLinePattern extends WordPattern {

    public String[] parse(String cmd) {
    	ArrayList<String> list = new ArrayList<String>();
		Matcher matcher = pattern.matcher(cmd);
		while(matcher.find()) {
			for(Word word : wordAlternatives) {
				String string = matcher.group(word.groupNumber);
				if(string != null) {
					if(word.encloser != singleQuote) {
						string = replaceEnvVariables(string);
					}
					list.add(string);
					break;
				}
			}
		}
		return list.toArray(new String[list.size()]);
    }
    
    @Override
	protected String getNotFound(String variable, String original) {
    	outputDebugVariableNotFound(variable);
    	return original;
    }
    
	public static void main(String[] args) {
		CommandLinePattern clp = new CommandLinePattern();
		String result[] = clp.parse("xxxx$USERNAME.yyy${USERNAME}zzz \"${I_am_not_a_var}$NotAVarEither\" bla \" \" '' ");
		for(int i=0; i<result.length; i++) {
			System.out.println(i);
			System.out.println(result[i]);
		}
		
		 //should show the user name replacement from the environment
		System.out.println("result length: " + result.length); //should be 4
	}
    
}