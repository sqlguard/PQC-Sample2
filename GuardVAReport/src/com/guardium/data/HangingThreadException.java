/*
* © Copyright 2002-2007, Guardium, Inc.  All rights reserved.  This material
* may not be copied, modified, altered, published, distributed, or otherwise
* displayed without the express written consent of Guardium, Inc.
*/
package com.guardium.data;

/**
 *
 * @author dtoland on Oct 4, 2007 at 4:57:14 PM
 */
public class HangingThreadException extends Exception {

	/**
	 * @param message
	 * @param cause
	 */
	public HangingThreadException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 */
	public HangingThreadException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public HangingThreadException(Throwable cause) {
		super(cause);

	}

}
