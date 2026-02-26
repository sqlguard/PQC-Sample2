/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.tests;

/**
 *
 * @author dtoland on Nov 12, 2007 at 2:36:35 PM
 */
public class QueryBasedException extends Exception {

	/**
	 * @param message
	 * @param cause
	 */
	public QueryBasedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public QueryBasedException(String message) {
		super(message);
	}
}
