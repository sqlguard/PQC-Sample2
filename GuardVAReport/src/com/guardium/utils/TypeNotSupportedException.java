/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.utils;

/**
 * @author dtoland on May 26, 2006 at 11:42:44 AM
 */
public class TypeNotSupportedException extends Exception {

	/**
	 * 
	 */
	public TypeNotSupportedException() {
		super();
	}

	/**
	 * @param message
	 */
	public TypeNotSupportedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public TypeNotSupportedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TypeNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
