/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Date;

public  class CveReference

{
	// constructor
	public CveReference () {
		
	}

	public CveReference(long cveRerenceId, Date timestamp, int testId,
			String cveReferenceSource, String cveReferenceType,
			String cveReferenceHref) {
		super();
		this.cveRerenceId = cveRerenceId;
		this.timestamp = timestamp;
		this.testId = testId;
		this.cveReferenceSource = cveReferenceSource;
		this.cveReferenceType = cveReferenceType;
		this.cveReferenceHref = cveReferenceHref;
	}



	private long cveRerenceId;
	private Date timestamp;
	private int testId;
    private String cveReferenceSource;
    private String cveReferenceType;
    private String cveReferenceHref;
    

    
	public long getCveRerenceId() {
		return cveRerenceId;
	}

	public void setCveRerenceId(long cveRerenceId) {
		this.cveRerenceId = cveRerenceId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public String getCveReferenceSource() {
		return cveReferenceSource;
	}

	public void setCveReferenceSource(String cveReferenceSource) {
		this.cveReferenceSource = cveReferenceSource;
	}

	public String getCveReferenceType() {
		return cveReferenceType;
	}

	public void setCveReferenceType(String cveReferenceType) {
		this.cveReferenceType = cveReferenceType;
	}

	public String getCveReferenceHref() {
		return cveReferenceHref;
	}

	public void setCveReferenceHref(String cveReferenceHref) {
		this.cveReferenceHref = cveReferenceHref;
	}

	public void dump () {
        System.out.println("cve reference id: " + this.cveRerenceId);
        System.out.println("available test id: " + this.getTestId());
        System.out.println("reference source: " + this.getCveReferenceSource());
        System.out.println("reference type: " + this.getCveReferenceType());
        System.out.println("reference href: " + this.getCveReferenceHref());
	}
	
	/*


mysql> desc CVE_REFERENCE;
+----------------------+--------------+------+-----+-------------------+-----------------------------+
| Field                | Type         | Null | Key | Default           | Extra                       |
+----------------------+--------------+------+-----+-------------------+-----------------------------+
| CVE_REFERENCE_ID     | bigint(20)   | NO   | PRI | NULL              | auto_increment              |
| TIMESTAMP            | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| TEST_ID              | int(11)      | NO   |     | 0                 |                             |
| CVE_REFERENCE_SOURCE | varchar(255) | YES  |     | NULL              |                             |
| CVE_REFERENCE_TYPE   | varchar(255) | YES  |     | NULL              |                             |
| CVE_REFERENCE_HREF   | varchar(255) | YES  |     | NULL              |                             |
+----------------------+--------------+------+-----+-------------------+-----------------------------+
6 rows in set (0.00 sec)


	 */
}
