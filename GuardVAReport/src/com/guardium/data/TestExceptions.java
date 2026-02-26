/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

import com.guardium.map.TestExceptionsMap;
import com.guardium.map.TestResultMap;

public class TestExceptions {

	TestExceptionsMap TestExceptionsPeer = TestExceptionsMap.getTestExceptionsMapObject();
	
	/*
mysql> desc TEST_EXCEPTIONS;
+--------------------+--------------+------+-----+---------------------+-----------------------------+
| Field              | Type         | Null | Key | Default             | Extra                       |
+--------------------+--------------+------+-----+---------------------+-----------------------------+
| TEST_EXCEPTIONS_ID | bigint(20)   | NO   | PRI | NULL                | auto_increment              |
| TIMESTAMP          | timestamp    | NO   |     | CURRENT_TIMESTAMP   | on update CURRENT_TIMESTAMP |
| APPROVER           | varchar(100) | NO   |     |                     |                             |
| FROM_DATE          | datetime     | NO   |     | 0000-00-00 00:00:00 |                             |
| TO_DATE            | datetime     | NO   |     | 0000-00-00 00:00:00 |                             |
| TEST_ID            | int(11)      | NO   |     | 0                   |                             |
| DATASOURCE_ID      | int(11)      | NO   |     | 0                   |                             |
| EXPLANATION        | mediumtext   | NO   |     | NULL                |                             |
+--------------------+--------------+------+-----+---------------------+-----------------------------+
8 rows in set (0.00 sec)


	 */
	public TestExceptions () {
		
	}
	
	public TestExceptions(long testExceptionsId, Date timestamp,
			String approver, Date fromDate, Date toDate, int testId,
			int datasourceId, String explanation) {
		super();
		this.testExceptionsId = testExceptionsId;
		this.timestamp = timestamp;
		this.approver = approver;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.testId = testId;
		this.datasourceId = datasourceId;
		this.explanation = explanation;
	}


	private long testExceptionsId;
	private Date timestamp;
	private String approver;
	private Date fromDate;
	private Date toDate;
	private int testId;
	private int datasourceId;
	private String explanation;

	
	public long getTestExceptionsId() {
		return testExceptionsId;
	}

	public void setTestExceptionsId(long testExceptionsId) {
		this.testExceptionsId = testExceptionsId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public int getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(int datasourceId) {
		this.datasourceId = datasourceId;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public void save () {
		TestExceptionsPeer.add(this);
		return;
	}

	
}
