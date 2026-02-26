/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.tests;

/**
 *
 * @author dtoland on Jun 4, 2007 at 12:54:54 PM
 */
public interface Score {

	/**
	 * @return The Test Score raw score value as found on the database.
	 */
	public int getScoreValue();

	/**
	 * @return The Test Score Definition Description of this Test Score from the database.
	 */
	public String getDescription();

}
