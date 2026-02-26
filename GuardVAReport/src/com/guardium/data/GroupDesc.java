/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

public class GroupDesc {


	public GroupDesc () {
		
	}

	public GroupDesc(int groupId, int groupTypeId, int applicationId,
			String groupDescription, String groupSubtype, String categoryName,
			String classificationName, Date timestamp, String groupContentType) {
		super();
		this.groupId = groupId;
		this.groupTypeId = groupTypeId;
		this.applicationId = applicationId;
		this.groupDescription = groupDescription;
		this.groupSubtype = groupSubtype;
		this.categoryName = categoryName;
		this.classificationName = classificationName;
		this.timestamp = timestamp;
		this.groupContentType = groupContentType;
	}

	private int groupId;
	private int groupTypeId;
	private int applicationId;
	private String groupDescription;
	private String groupSubtype;
	private String categoryName;
	private String classificationName;
	private Date timestamp;
	private String groupContentType;
		
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupTypeId() {
		return groupTypeId;
	}

	public void setGroupTypeId(int groupTypeId) {
		this.groupTypeId = groupTypeId;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public String getGroupSubtype() {
		return groupSubtype;
	}

	public void setGroupSubtype(String groupSubtype) {
		this.groupSubtype = groupSubtype;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getClassificationName() {
		return classificationName;
	}

	public void setClassificationName(String classificationName) {
		this.classificationName = classificationName;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getGroupContentType() {
		return groupContentType;
	}

	public void setGroupContentType(String groupContentType) {
		this.groupContentType = groupContentType;
	}

	public void dump() {
        System.out.println("group desc group id: " + this.getGroupId());
        System.out.println("group desc type id: " + this.getGroupTypeId());
        System.out.println("group desc appl id: " + this.getApplicationId());
        System.out.println("group desc group desc: " + this.getGroupDescription());
        System.out.println("group desc group subtype: " + this.getGroupSubtype());
        System.out.println("group desc category name: " + this.getCategoryName());
        System.out.println("group desc classification name: " + this.getClassificationName());
        System.out.println("group desc group content type: " + this.getGroupContentType());
        
	}
	
/*
mysql> mysql> desc GROUP_DESC;
+---------------------+--------------+------+-----+-------------------+-----------------------------+
| Field               | Type         | Null | Key | Default           | Extra                       |
+---------------------+--------------+------+-----+-------------------+-----------------------------+
| GROUP_ID            | int(11)      | NO   | PRI | NULL              | auto_increment              |
| GROUP_TYPE_ID       | int(11)      | NO   |     | 0                 |                             |
| APPLICATION_ID      | int(11)      | NO   |     | 0                 |                             |
| GROUP_DESCRIPTION   | varchar(150) | NO   |     |                   |                             |
| GROUP_SUBTYPE       | varchar(20)  | YES  |     | NULL              |                             |
| CATEGORY_NAME       | varchar(60)  | YES  |     | NULL              |                             |
| CLASSIFICATION_NAME | varchar(60)  | YES  |     | NULL              |                             |
| TIMESTAMP           | timestamp    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| GROUP_CONTENT_TYPE  | varchar(1)   | NO   |     | M                 |                             |
+---------------------+--------------+------+-----+-------------------+-----------------------------+
9 rows in set (0.00 sec)

*/ 
	
}
