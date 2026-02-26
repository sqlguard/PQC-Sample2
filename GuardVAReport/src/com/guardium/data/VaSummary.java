/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.Date;

import com.guardium.map.AvailableTestMap;
import com.guardium.map.VaSummaryMap;

public class VaSummary {

	// constructor
	public VaSummary () {
		
	}
	
	public VaSummary(long vaSummaryId, String dataSourceHash, int testId,
			String testDesc, String dbType, String serviceName, int dbPort,
			String dbHost, Date timestamp, Date firstExecution,
			Date lastExecution, Date firstFail, Date lastFail, Date firstPass,
			Date lastPass, int currentScore, Date currentScoreSince,
			int cumulativeFailAge, int cumulativePassAge, String datasourceName) {
		super();
		this.vaSummaryId = vaSummaryId;
		this.dataSourceHash = dataSourceHash;
		this.testId = testId;
		this.testDesc = testDesc;
		this.dbType = dbType;
		this.serviceName = serviceName;
		this.dbPort = dbPort;
		this.dbHost = dbHost;
		this.timestamp = timestamp;
		this.firstExecution = firstExecution;
		this.lastExecution = lastExecution;
		this.firstFail = firstFail;
		this.lastFail = lastFail;
		this.firstPass = firstPass;
		this.lastPass = lastPass;
		this.currentScore = currentScore;
		this.currentScoreSince = currentScoreSince;
		this.cumulativeFailAge = cumulativeFailAge;
		this.cumulativePassAge = cumulativePassAge;
		this.datasourceName = datasourceName;
	}



	long vaSummaryId;
	String dataSourceHash;
	int testId;
	String testDesc;
	String dbType;
	String serviceName;
	int dbPort;
	String dbHost;
	Date timestamp;
	Date firstExecution;
	Date lastExecution;

	Date firstFail;
	Date lastFail;
	Date firstPass;
	Date lastPass;
	int currentScore;
	Date currentScoreSince;
	int cumulativeFailAge;
	int cumulativePassAge;
	String datasourceName;
	public long getVaSummaryId() {
		return vaSummaryId;
	}
	public void setVaSummaryId(long vaSummaryId) {
		this.vaSummaryId = vaSummaryId;
	}
	public String getDataSourceHash() {
		return dataSourceHash;
	}
	public void setDataSourceHash(String dataSourceHash) {
		this.dataSourceHash = dataSourceHash;
	}
	public int getTestId() {
		return testId;
	}
	public void setTestId(int testId) {
		this.testId = testId;
	}
	public String getTestDesc() {
		return testDesc;
	}
	public void setTestDesc(String testDesc) {
		this.testDesc = testDesc;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public int getDbPort() {
		return dbPort;
	}
	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}
	public String getDbHost() {
		return dbHost;
	}
	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Date getFirstExecution() {
		return firstExecution;
	}
	public void setFirstExecution(Date firstExecution) {
		this.firstExecution = firstExecution;
	}
	public Date getLastExecution() {
		return lastExecution;
	}
	public void setLastExecution(Date lastExecution) {
		this.lastExecution = lastExecution;
	}
	public Date getFirstFail() {
		return firstFail;
	}
	public void setFirstFail(Date firstFail) {
		this.firstFail = firstFail;
	}
	public Date getLastFail() {
		return lastFail;
	}
	public void setLastFail(Date lastFail) {
		this.lastFail = lastFail;
	}
	public Date getFirstPass() {
		return firstPass;
	}
	public void setFirstPass(Date firstPass) {
		this.firstPass = firstPass;
	}
	public Date getLastPass() {
		return lastPass;
	}
	public void setLastPass(Date lastPass) {
		this.lastPass = lastPass;
	}
	public int getCurrentScore() {
		return currentScore;
	}
	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}
	public Date getCurrentScoreSince() {
		return currentScoreSince;
	}
	public void setCurrentScoreSince(Date currentScoreSince) {
		this.currentScoreSince = currentScoreSince;
	}
	public int getCumulativeFailAge() {
		return cumulativeFailAge;
	}
	public void setCumulativeFailAge(int cumulativeFailAge) {
		this.cumulativeFailAge = cumulativeFailAge;
	}
	public int getCumulativePassAge() {
		return cumulativePassAge;
	}
	public void setCumulativePassAge(int cumulativePassAge) {
		this.cumulativePassAge = cumulativePassAge;
	}
	public String getDatasourceName() {
		return datasourceName;
	}
	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}
	
	
	public void dump() {
		System.out.println("va summary id: " + this.getVaSummaryId());
		System.out.println("va summary test id: " + this.getTestId());
		System.out.println("va summary test desc: " + this.getTestDesc());
	}
	
	VaSummaryMap VaSummaryPeer = VaSummaryMap.getVaSummaryMapObject();
			
	public void save() {
		VaSummaryPeer.add(this);
		return;
	}
	
/*
 * 

mysql> desc VA_SUMMARY;
+---------------------+--------------+------+-----+---------------------+-----------------------------+
| Field               | Type         | Null | Key | Default             | Extra                       |
+---------------------+--------------+------+-----+---------------------+-----------------------------+
| VA_SUMMARY_ID       | bigint(20)   | NO   | PRI | NULL                | auto_increment              |
| DATA_SOURCE_HASH    | varchar(40)  | NO   | MUL |                     |                             |
| TEST_ID             | int(11)      | NO   |     | 0                   |                             |
| TEST_DESC           | varchar(150) | NO   |     | N/A                 |                             |
| DB_TYPE             | varchar(40)  | YES  |     | NULL                |                             |
| SERVICE_NAME        | varchar(40)  | YES  |     |                     |                             |
| DB_PORT             | int(11)      | YES  |     | NULL                |                             |
| DB_HOST             | varchar(255) | YES  |     |                     |                             |
| TIMESTAMP           | timestamp    | NO   |     | CURRENT_TIMESTAMP   | on update CURRENT_TIMESTAMP |
| FIRST_EXECUTION     | datetime     | NO   |     | 0000-00-00 00:00:00 |                             |
| LAST_EXECUTION      | datetime     | NO   |     | 0000-00-00 00:00:00 |                             |
| FIRST_FAIL          | datetime     | YES  |     | NULL                |                             |
| LAST_FAIL           | datetime     | YES  |     | NULL                |                             |
| FIRST_PASS          | datetime     | YES  |     | NULL                |                             |
| LAST_PASS           | datetime     | YES  |     | NULL                |                             |
| CURRENT_SCORE       | int(11)      | NO   |     | -999                |                             |
| CURRENT_SCORE_SINCE | datetime     | NO   |     | 0000-00-00 00:00:00 |                             |
| CUMULATIVE_FAIL_AGE | int(11)      | NO   |     | 0                   |                             |
| CUMULATIVE_PASS_AGE | int(11)      | NO   |     | 0                   |                             |
| DATASOURCE_NAME     | varchar(50)  | YES  |     |                     |                             |
+---------------------+--------------+------+-----+---------------------+-----------------------------+
20 rows in set (0.00 sec)

mysql> 


 */
	
}
