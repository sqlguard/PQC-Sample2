/*
* �� Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material
* may not be copied, modified, altered, published, distributed, or otherwise
* displayed without the express written consent of Guardium, Inc.
*/

package com.guardium.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Date;

public  class CveFix

{
	// constructor
	public CveFix () {
		
	}

	public CveFix(int cveFixId, int availableTestId, String version,
			String patch) {
		super();
		this.cveFixId = cveFixId;
		this.availableTestId = availableTestId;
		this.version = version;
		this.patch = patch;
		this.patch_to = "";
	}


	private int cveFixId;
	private int availableTestId;
    private String version;
    private String patch;
    private String patch_to;
    

	public int getCveFixId() {
		return cveFixId;
	}

	public void setCveFixId(int cveFixId) {
		this.cveFixId = cveFixId;
	}

	public int getAvailableTestId() {
		return availableTestId;
	}

	public void setAvailableTestId(int availableTestId) {
		this.availableTestId = availableTestId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}
	
	public String getPatchTo() {
		return patch_to;
	}

	public void setPatchTo(String patchto) {
		this.patch_to = patchto;
	}
	
	public void dump () {
        System.out.println("cve fix id: " + this.getCveFixId());
        System.out.println("available test id: " + this.getAvailableTestId());
        System.out.println("version " + this.getVersion());
        System.out.println("patch: " + this.getPatch());
        System.out.println("patch_to: " + this.getPatchTo());
	}
	
	/*

mysql> desc CVE_FIX;
+-------------------+--------------+------+-----+---------+----------------+
| Field             | Type         | Null | Key | Default | Extra          |
+-------------------+--------------+------+-----+---------+----------------+
| CVE_FIX_ID        | int(11)      | NO   | PRI | NULL    | auto_increment |
| AVAILABLE_TEST_ID | int(11)      | NO   |     | 0       |                |
| VERSION           | varchar(10)  | NO   |     |         |                |
| PATCH             | varchar(100) | NO   |     |         |                |
+-------------------+--------------+------+-----+---------+----------------+
4 rows in set (0.00 sec)


	 */
}
