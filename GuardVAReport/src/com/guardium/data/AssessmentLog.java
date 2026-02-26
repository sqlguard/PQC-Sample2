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
//import com.guardium.data.AssessmentLogType;
import com.guardium.map.AssessmentLogMap;

public  class AssessmentLog

{
	
/*

mysql> desc ASSESSMENT_LOG;
+------------------------+--------------+------+-----+-------------------+-----------------------------+
| Field                  | Type         | Null | Key | Default           | Extra                       |
+------------------------+--------------+------+-----+-------------------+-----------------------------+
| ASSESSMENT_LOG_ID      | bigint(20)   | NO   | PRI | NULL              | auto_increment              |
| ASSESSMENT_LOG_TYPE_ID | bigint(20)   | NO   |     | 0                 |                             |
| ASSESSMENT_RESULT_ID   | bigint(20)   | NO   |     | 0                 |                             |
| MESSAGE                | varchar(255) | NO   |     |                   |                             |
| DETAILS                | mediumtext   | NO   |     | NULL              |                             |
| TIMESTAMP              | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
+------------------------+--------------+------+-----+-------------------+-----------------------------+
6 rows in set (0.00 sec)

mysql> 


	
 */
	public AssessmentLog () {
		
	}
	
	public AssessmentLog(long assessmentLogId, long assessmentLogTypeId,
			long assessmentResultId, String message, String details,
			Date timestamp) {
		super();
		this.assessmentLogId = assessmentLogId;
		this.assessmentLogTypeId = assessmentLogTypeId;
		this.assessmentResultId = assessmentResultId;
		this.message = message;
		this.details = details;
		this.timestamp = timestamp;
	}

	private long assessmentLogId;

	private long assessmentLogTypeId;
	
    private long assessmentResultId;

    private String message;
    
    private String details;
    
    private Date timestamp;

	public long getAssessmentLogId() {
		return assessmentLogId;
	}

	public void setAssessmentLogId(long assessmentLogId) {
		this.assessmentLogId = assessmentLogId;
	}

	public long getAssessmentLogTypeId() {
		return assessmentLogTypeId;
	}

	public void setAssessmentLogTypeId(long assessmentLogTypeId) {
		this.assessmentLogTypeId = assessmentLogTypeId;
	}

	public long getAssessmentResultId() {
		return assessmentResultId;
	}

	public void setAssessmentResultId(long assessmentResultId) {
		this.assessmentResultId = assessmentResultId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
             
	public void dump () {
        System.out.println("log id: " + this.getAssessmentLogId());
        System.out.println("log type id: " + this.assessmentLogTypeId);
        System.out.println("log result id: " + this.getAssessmentResultId());
        System.out.println("log details: " + this.getDetails());
        System.out.println("log message: " + this.getMessage());
	}
	
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	
	public void save () {
		//AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
		AssessmentLogPeer.add(this);
		return;
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

mysql> select * from ASSESSMENT_LOG_TYPE;
+------------------------+-----------------------+----------+---------------------+
| ASSESSMENT_LOG_TYPE_ID | NAME                  | SEVERITY | TIMESTAMP           |
+------------------------+-----------------------+----------+---------------------+
|                      1 | Debug                 |        1 | 2014-05-30 10:45:09 |
|                      2 | Info                  |        2 | 2014-05-30 10:45:09 |
|                      3 | Warning               |        3 | 2014-05-30 10:45:09 |
|                      4 | Error                 |        4 | 2014-05-30 10:45:09 |
|                      5 | Fatal                 |        5 | 2014-05-30 10:45:09 |
|                    201 | Assessment Start      |        3 | 2014-05-30 10:45:09 |
|                    202 | Datasource Statistics |        2 | 2014-05-30 10:45:09 |
|                    203 | Assessment Complete   |        3 | 2014-05-30 10:45:09 |
|                    205 | Assessment Halted     |        5 | 2014-05-30 10:45:09 |
|                    206 | Timeout               |        5 | 2014-05-30 10:45:09 |
+------------------------+-----------------------+----------+---------------------+
10 rows in set (0.02 sec)

mysql> 

	 */
}
