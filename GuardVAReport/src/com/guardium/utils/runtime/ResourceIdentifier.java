/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.utils.runtime;

import java.io.File;

public class ResourceIdentifier {
	private static ResourcePattern resourcePattern = new ResourcePattern();
	private final String original;
	private final String parsed;
	private File file;
	
	public ResourceIdentifier(String identifier) {
		this.parsed = resourcePattern.parse(identifier);
		this.original = identifier;
	}
	
	public String getParsed() {
		return parsed;
	}
	
	public String getOriginal() {
		return original;
	}
    
	public File toFile() {
    	if(file == null) {
    		file = new File(parsed);
    	}
    	return file;
    }
	
	public File toFile(String child) {
    	return new File(toFile(), child);
    }
    
    @Override
	public String toString() {
		return parsed;
	}
    
    public static void main(String[] args) {
		ResourceIdentifier clp = new ResourceIdentifier("xxxx$USERNAME.yyy${USERNAME}zzz bla \" \" '' ");
		String result = clp.getParsed();
		System.out.println(result);
		
		System.out.println(new ResourceIdentifier("${GUARD}/licenseInfo.html").getParsed());
		System.out.println(new ResourceIdentifier("${GUARD_LOG_DIR}/licenseInfo.html").getParsed());
		System.out.println(new ResourceIdentifier("${GUARD_HOME}/scripts/GDM_REDO_FEED.pks").getParsed());
		System.out.println(new ResourceIdentifier("${GUARD_HOME}/scripts/GDM_REDO_FEED.pkb").getParsed());
	}
 
}
