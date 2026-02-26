/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class GroupType {

	public GroupType () {
		
	}



	public GroupType(int groupTypeId, String typeDescription, Date timestamp,
			boolean tupleFlag, boolean allowRegex) {
		super();
		this.groupTypeId = groupTypeId;
		this.typeDescription = typeDescription;
		this.timestamp = timestamp;
		this.tupleFlag = tupleFlag;
		this.allowRegex = allowRegex;
	}

	private int groupTypeId;
	private String typeDescription;
	private Date timestamp;
	private boolean tupleFlag;
	private boolean allowRegex;

	public int getGroupTypeId() {
		return groupTypeId;
	}
	public void setGroupTypeId(int groupTypeId) {
		this.groupTypeId = groupTypeId;
	}
	public String getTypeDescription() {
		return typeDescription;
	}
	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isTupleFlag() {
		return tupleFlag;
	}
	public void setTupleFlag(boolean tupleFlag) {
		this.tupleFlag = tupleFlag;
	}
	public boolean isAllowRegex() {
		return allowRegex;
	}
	public void setAllowRegex(boolean allowRegex) {
		this.allowRegex = allowRegex;
	}
	


/*
mysql> desc GROUP_TYPE;
+------------------+--------------+------+-----+-------------------+-----------------------------+
| Field            | Type         | Null | Key | Default           | Extra                       |
+------------------+--------------+------+-----+-------------------+-----------------------------+
| GROUP_TYPE_ID    | int(11)      | NO   | PRI | NULL              | auto_increment              |
| TYPE_DESCRIPTION | varchar(150) | NO   |     |                   |                             |
| TIMESTAMP        | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| TUPLE_FLAG       | int(11)      | NO   |     | 1                 |                             |
| ALLOW_REGEX      | int(11)      | NO   |     | 0                 |                             |
+------------------+--------------+------+-----+-------------------+-----------------------------+
5 rows in set (0.00 sec)

 */
}
