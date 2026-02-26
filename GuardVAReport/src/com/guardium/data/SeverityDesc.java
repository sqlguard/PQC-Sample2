/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

//import java.util.Date;

public class SeverityDesc {
	
	public SeverityDesc ( int type_id, String desc) {
		severity_id = type_id;
		severity_desc = desc;
	}		
	
	/**
     * The value for the datasource_type_id field
     */
    private int severity_id;
          
    /**
     * The value for the name field
     */
    private String severity_desc;
          
    public int getSeverityId() {
		return severity_id;
	}

	public void setSeverityId(int type_id) {
		this.severity_id = type_id;
	}

	public String getSeverityDesc() {
		return severity_desc;
	}

	public void setseverityDesc(String name) {
		this.severity_desc = name;
	}
	
	/*
	 * 
	 * 
mysql> desc SEVERITY_DESC;
+-------------+-------------+------+-----+---------+-------+
| Field       | Type        | Null | Key | Default | Extra |
+-------------+-------------+------+-----+---------+-------+
| SEVERITY    | int(11)     | NO   | PRI | 0       |       |
| DESCRIPTION | varchar(20) | YES  |     | NULL    |       |
+-------------+-------------+------+-----+---------+-------+
2 rows in set (0.01 sec)

mysql> select * from SEVERITY_DESC;
+----------+-------------+
| SEVERITY | DESCRIPTION |
+----------+-------------+
|       10 | HIGH        |
|        5 | MED         |
|        1 | LOW         |
|        0 | INFO        |
|        2 | NONE        |
+----------+-------------+
5 rows in set (0.00 sec)

mysql> 

	 * 
	 */
}
