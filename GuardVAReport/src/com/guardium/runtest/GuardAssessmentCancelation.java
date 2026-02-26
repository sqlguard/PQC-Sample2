/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;

/**
 *
 * @author dtoland on Feb 12, 2008 at 9:50:41 AM
 */
public class GuardAssessmentCancelation extends Exception {

	/**
	 * @param message
	 * @param cause
	 */
	public GuardAssessmentCancelation(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public GuardAssessmentCancelation(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public GuardAssessmentCancelation(Throwable cause) {
		super(cause);
	}
}
