/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.data;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;

import java.util.Date;

public  class AssessmentLogType

{
	// constructor
	public AssessmentLogType () {
		
	}

	public AssessmentLogType(long assessmentLogTypeId, String name,
			int severity, Date timestamp) {
		super();
		this.assessmentLogTypeId = assessmentLogTypeId;
		this.name = name;
		this.severity = severity;
		this.timestamp = timestamp;
	}

	private long assessmentLogTypeId;
	
    private String name;
    
    private int severity;
    
    private Date timestamp;
	
    
    
	public long getAssessmentLogTypeId() {
		return assessmentLogTypeId;
	}

	public void setAssessmentLogTypeId(long assessmentLogTypeId) {
		this.assessmentLogTypeId = assessmentLogTypeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void dump () {
        System.out.println("log type id: " + this.getAssessmentLogTypeId());
        System.out.println("log name: " + this.getName());
        System.out.println("log severity: " + this.getSeverity());
        System.out.println("log timestamp: " + this.getTimestamp());
	}
	
	/*

mysql> desc ASSESSMENT_LOG_TYPE;
+------------------------+-------------+------+-----+-------------------+-----------------------------+
| Field                  | Type        | Null | Key | Default           | Extra                       |
+------------------------+-------------+------+-----+-------------------+-----------------------------+
| ASSESSMENT_LOG_TYPE_ID | bigint(20)  | NO   | PRI | NULL              | auto_increment              |
| NAME                   | varchar(60) | NO   |     |                   |                             |
| SEVERITY               | int(11)     | NO   |     | 0                 |                             |
| TIMESTAMP              | timestamp   | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
+------------------------+-------------+------+-----+-------------------+-----------------------------+
4 rows in set (0.00 sec)

	 */
}
