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

import com.guardium.map.DatasourceVersionHistoryMap;

public  class DatasourceVersionHistory

{
	
	DatasourceVersionHistoryMap DatasourceVersionHistoryPeer = DatasourceVersionHistoryMap.getDatasourceVersionHistoryMapObject();
	
	// constructor
	public DatasourceVersionHistory () {
		
	}

	public DatasourceVersionHistory(String datasourceVersionHistoryId,
			int datasourceId, String versionLevel, String patchLevel,
			Date verified, Date detected, String datasourceDescription,
			String datasourceTypeDescription, String fullVersionInfo,
			String versionType, String originalTimezone, int collectorId) {
		super();
		this.datasourceVersionHistoryId = datasourceVersionHistoryId;
		this.datasourceId = datasourceId;
		this.versionLevel = versionLevel;
		this.patchLevel = patchLevel;
		this.verified = verified;
		this.detected = detected;
		this.datasourceDescription = datasourceDescription;
		this.datasourceTypeDescription = datasourceTypeDescription;
		this.fullVersionInfo = fullVersionInfo;
		this.versionType = versionType;
		this.originalTimezone = originalTimezone;
		this.collectorId = collectorId;
	}

	// return the history
	//DatasourceVersionHistory history = new DatasourceVersionHistory(datasource, fullInfo, patch, version, datasource.getVersionType());
	
	public DatasourceVersionHistory(Datasource ds, String fullInfo, String patch, String version, String versionType) {
		super();
		
		this.datasourceId = ds.getDatasourceId();
		this.versionLevel = version;
		this.patchLevel = patch;
		this.verified = new Date();
		this.detected = new Date();
		this.datasourceDescription = ds.getDescription();
		this.datasourceTypeDescription = ds.getVersionType();
		this.fullVersionInfo = fullInfo;
		this.versionType = versionType;
		//this.originalTimezone = originalTimezone;
		//this.collectorId = collectorId;
	}

	private String datasourceVersionHistoryId;
	private int datasourceId;
	private String versionLevel;
	private String patchLevel;
	private Date verified;
	private Date detected;
	private String datasourceDescription;
	private String datasourceTypeDescription;
	private String fullVersionInfo;
	private String versionType;
	private String originalTimezone;
	private int collectorId;
	
	
	public String getDatasourceVersionHistoryId() {
		return datasourceVersionHistoryId;
	}

	public void setDatasourceVersionHistoryId(String datasourceVersionHistoryId) {
		this.datasourceVersionHistoryId = datasourceVersionHistoryId;
	}

	public int getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(int datasourceId) {
		this.datasourceId = datasourceId;
	}

	public String getVersionLevel() {
		return versionLevel;
	}

	public void setVersionLevel(String versionLevel) {
		this.versionLevel = versionLevel;
	}

	public String getPatchLevel() {
		return patchLevel;
	}

	public void setPatchLevel(String patchLevel) {
		this.patchLevel = patchLevel;
	}

	public Date getVerified() {
		return verified;
	}

	public void setVerified(Date verified) {
		this.verified = verified;
	}

	public Date getDetected() {
		return detected;
	}

	public void setDetected(Date detected) {
		this.detected = detected;
	}

	public String getDatasourceDescription() {
		return datasourceDescription;
	}

	public void setDatasourceDescription(String datasourceDescription) {
		this.datasourceDescription = datasourceDescription;
	}

	public String getDatasourceTypeDescription() {
		return datasourceTypeDescription;
	}

	public void setDatasourceTypeDescription(String datasourceTypeDescription) {
		this.datasourceTypeDescription = datasourceTypeDescription;
	}

	public String getFullVersionInfo() {
		return fullVersionInfo;
	}

	public void setFullVersionInfo(String fullVersionInfo) {
		this.fullVersionInfo = fullVersionInfo;
	}

	public String getVersionType() {
		return versionType;
	}

	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	public String getOriginalTimezone() {
		return originalTimezone;
	}

	public void setOriginalTimezone(String originalTimezone) {
		this.originalTimezone = originalTimezone;
	}

	public int getCollectorId() {
		return collectorId;
	}

	public void setCollectorId(int collectorId) {
		this.collectorId = collectorId;
	}

	public void save() {
		DatasourceVersionHistoryPeer.add(this);
		return;
	}
	
	public void dump () {
		
        System.out.println("id: " + this.getDatasourceVersionHistoryId());
        System.out.println("datasource id: " + this.getDatasourceId());
        System.out.println("version level: " + this.getVersionLevel());
        System.out.println("patch level: " + this.getPatchLevel());
        System.out.println("datasource type desc: " + this.getDatasourceTypeDescription());
        System.out.println("full version info: " + this.getFullVersionInfo());
        System.out.println("version type: " + this.getVersionType());
	}
	
	/*

mysql> mysql> desc DATASOURCE_VERSION_HISTORY;
+-------------------------------+--------------+------+-----+---------------------+-----------------------------+
| Field                         | Type         | Null | Key | Default             | Extra                       |
+-------------------------------+--------------+------+-----+---------------------+-----------------------------+
| DATASOURCE_VERSION_HISTORY_ID | varchar(40)  | NO   | PRI |                     |                             |
| DATASOURCE_ID                 | int(11)      | NO   |     | 0                   |                             |
| VERSION_LEVEL                 | varchar(100) | NO   |     |                     |                             |
| PATCH_LEVEL                   | varchar(255) | YES  |     | NULL                |                             |
| VERIFIED                      | timestamp    | NO   | PRI | CURRENT_TIMESTAMP   | on update CURRENT_TIMESTAMP |
| DETECTED                      | timestamp    | NO   |     | 0000-00-00 00:00:00 |                             |
| DATASOURCE_DESCRIPTION        | varchar(255) | YES  |     | NULL                |                             |
| DATASOURCE_TYPE_DESCRIPTION   | varchar(255) | YES  |     | NULL                |                             |
| FULL_VERSION_INFO             | mediumtext   | YES  |     | NULL                |                             |
| VERSION_TYPE                  | varchar(30)  | NO   |     | N/A                 |                             |
| ORIGINAL_TIMEZONE             | varchar(8)   | YES  |     | -04:00              |                             |
| COLLECTOR_ID                  | int(11)      | NO   |     | -1                  |                             |
+-------------------------------+--------------+------+-----+---------------------+-----------------------------+
12 rows in set (0.00 sec)

	 */
}
