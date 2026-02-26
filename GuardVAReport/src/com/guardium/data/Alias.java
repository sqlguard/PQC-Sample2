/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class Alias {
	
	public Alias () {
		
	}

	public Alias(int aliasId, int groupTypeId, String dbValue, String aliasValue) {
		super();
		this.aliasId = aliasId;
		this.groupTypeId = groupTypeId;
		this.dbValue = dbValue;
		this.aliasValue = aliasValue;
	}

	private int aliasId;

	private int groupTypeId;
	
	private String dbValue;
	
	private String aliasValue;

	public int getAliasId() {
		return aliasId;
	}

	public void setAliasId(int aliasId) {
		this.aliasId = aliasId;
	}

	public int getGroupTypeId() {
		return groupTypeId;
	}

	public void setGroupTypeId(int groupTypeId) {
		this.groupTypeId = groupTypeId;
	}

	public String getDbValue() {
		return dbValue;
	}

	public void setDbValue(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getAliasValue() {
		return aliasValue;
	}

	public void setAliasValue(String aliasValue) {
		this.aliasValue = aliasValue;
	}
	


	/*
	mysql> 

	ysql> desc ALIAS;
	+---------------+--------------+------+-----+---------+----------------+
	| Field         | Type         | Null | Key | Default | Extra          |
	+---------------+--------------+------+-----+---------+----------------+
	| ALIAS_ID      | int(11)      | NO   | PRI | NULL    | auto_increment |
	| GROUP_TYPE_ID | int(11)      | NO   |     | 0       |                |
	| DB_VALUE      | varchar(150) | NO   |     |         |                |
	| ALIAS_VALUE   | varchar(150) | NO   | MUL |         |                |
	+---------------+--------------+------+-----+---------+----------------+
	4 rows in set (0.00 sec)

	 */

}
