/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.assessment.i18n;

/**
 *
 * @author dtoland on Jun 11, 2007 at 11:35:01 AM
 */
public interface CustomAssesmentMessages {

	/**
	 * Constant for Custom Test Backstop Exception message.
	 * @see #ASMT_SUB_TESTCLASS
	 */
	public String ASMT_MSG_CUST_BACKSTOP = "asmt.cust.backstop";

	/**
	 * Constant for Custom Test SQL Exception message.
	 * @see #ASMT_SUB_TESTCLASS
	 */
	public String ASMT_MSG_CUST_SQL_FAIL = "asmt.cust.sql.failure";

	/**
	 * Constant for Custom Test SQL Exception message.
	 * @see #ASMT_SUB_TESTCLASS
	 */
	public String ASMT_MSG_CUST_REPORT_FAIL = "asmt.cust.report.failure";

	/**
	 * Constant for Custom Proxy Creation Failure message.
	 * @see #ASMT_SUB_TESTCLASS
	 */
	public String ASMT_MSG_PROXY_CREATE_FAIL = "asmt.cust.proxy.creation.failure";

	/**
	 * Constant for Custom Proxy Method Failure message.
	 * @see #ASMT_SUB_METHOD
	 */
	public String ASMT_MSG_PROXY_METHOD_FAIL = "asmt.cust.proxy.method.failure";

	/** Constant for the Method Symbol */
	public String ASMT_SUB_METHOD = "method";

	/** Constant for the Test Class Symbol */
	public String ASMT_SUB_TESTCLASS = "testclass";

}
