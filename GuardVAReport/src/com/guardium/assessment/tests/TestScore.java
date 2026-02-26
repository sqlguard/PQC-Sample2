/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.tests;

import com.guardium.map.TestScoreDefinitionMap;


//import com.guardium.assessment.TestScoreDefinition;
//import com.guardium.assessment.TestScoreDefinitionPeer;

/**
 * Test Score values to return from the tests
 * @author dtoland on May 31, 2007 at 9:59:14 AM
 */
public enum TestScore implements Score {
	/** The Test Passed. */
	PASS(1),
	/** The Test Failed. */
	FAIL(0),
	/** Unspecified Error. */
	ERROR(-1),
	/** CAS data is not available to evaluate this test and needs to be run. */
	NO_CAS_DATA(-2),
	/** Report data is not available to evaluate this test and needs to be run. */
	NO_REPORT_DATA(-3),
	/** The database version does not support this test */
	UNSUPPORTED_DB_VERSION(-4),
	/** The OS version does not support this test */
	UNSUPPORTED_OS_VERSION(-5),
	/** Unknown */
	SPECIAL_ERROR(-6),
	/** Error in collecting CAS data **/
	CAS_DATA_COLLECTION_ERROR(-7),
	/** Obsolete Database Version **/
	OBSOLETE_PARAMETER(-8),
	/** Deprecated Database Version **/
	DEPRECATED_PARAMETER(-9),
	/** CVE not reported for the database version **/
	CVE_NOT_REPORTED(-10),
	/** user data not set in GDMMONITOR.OS_USER and GDMMONITOR.OS_GROUP tables **/
	NO_USER_DATA(-11),
	/** Modules listed for PTF not present in DB2 MEPL **/
	MODULES_NOT_PRESENT(-12),
	/** The Pre Test Check Failed Test Not Executed. */ 
	PRE_TEST_CHECK_FAILED(-13),
	/** The calculateScore routine not defined in the test */
	EXECUTION_TEST_ROUTINE_CHECK_FAILED(-14),
	/** Unsupported CVE database patch detected **/
	UNSUPPORT_CVE_PATCH_DETECTED(-15),
		
	/** Unsupported Security PTF patch detected **/
	UNSUPPORT_PTF_PATCH_DETECTED(-16);
	/** The Test Score Definition ID of this Test Score. */
	private final int scoreValue;

	//TestScoreDefinitionMap = new TestScoreDefinitionMap();
	
	/**
	 * @param that
	 * @return Whether the incoming TestScore is equal to this TestScore
	 */
	public boolean equals(TestScore that) {
		return this.equals(that.scoreValue);
	}

	/**
	 * @param scoreValue
	 * @return Whether the incoming raw score is equal to this TestScore
	 */
	public boolean equals(int scoreValue) {
		return this.scoreValue == scoreValue;
	}

	/**
	 * Constructor
	 * @param scoreValue The raw score value as found on the database.
	 */
	private TestScore(int scoreValue) {
		this.scoreValue = scoreValue;
	}

	/**
	 * @return The Test Score raw score value as found on the database.
	 */
	public int getScoreValue() {
		return this.scoreValue;
	}

	/**
	 * @return The Test Score raw score value as found on the database.
	 */
	public double getDoubleValue() {
		return new Double(this.scoreValue);
	}

	/**
	 * @return The Test Score Definition Description of this Test Score from the database.
	 */
	public String getDescription() {
		
		TestScoreDefinitionMap tmap = new TestScoreDefinitionMap();
		tmap.initMap();
		
		String description;
		TestScoreDefinition def = tmap.getTestScoreDefinition(this.scoreValue);
		description = def.getDescription();

		return description;
	}

	@Override
	public String toString() {
		return this.getDescription();
	}

	
	/**
	 * @param scoreValue The raw score value as found on the database.
	 * @return A Test Score for the score value
	 */
	public static TestScore findTestScore(int scoreValue) {
		for ( TestScore testScore : values() ) {
			if (testScore.scoreValue==scoreValue) { return testScore; }
		}
		return null;
	}
}
