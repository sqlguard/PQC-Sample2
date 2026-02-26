/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

/**
 *
 * @author dtoland on Mar 7, 2007 at 1:53:02 PM
 */
public class SqlTimeoutException extends Exception {

	/**
	 *
	 */
	public SqlTimeoutException() {
		super();
	}

	/**
	 * @param message
	 */
	public SqlTimeoutException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public SqlTimeoutException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public SqlTimeoutException(String message, Throwable cause) {
		super(message, cause);

	}

}
