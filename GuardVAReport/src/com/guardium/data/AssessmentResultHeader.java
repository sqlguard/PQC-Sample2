/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.guardium.data;

//import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;

import java.util.Date;

import com.guardium.map.AssessmentResultHeaderMap;

public  class AssessmentResultHeader

{
	
/*
mysql> desc ASSESSMENT_RESULT_HEADER;
+-------------------------+--------------+------+-----+---------------------+----------------+
| Field                   | Type         | Null | Key | Default             | Extra          |
+-------------------------+--------------+------+-----+---------------------+----------------+
| ASSESSMENT_RESULT_ID    | bigint(20)   | NO   | PRI | NULL                | auto_increment |
| ASSESSMENT_ID           | int(11)      | NO   |     | 0                   |                |
| TASK_ID                 | bigint(20)   | NO   |     | 0                   |                |
| PARAMETER_MODIFIED_FLAG | int(11)      | NO   |     | 0                   |                |
| EXECUTION_DATE          | datetime     | NO   |     | 0000-00-00 00:00:00 |                |
| RECEIVED_BY_ALL         | int(11)      | NO   |     | 0                   |                |
| OVERALL_SCORE           | float        | NO   |     | 0                   |                |
| FROM_DATE               | datetime     | NO   |     | 0000-00-00 00:00:00 |                |
| TO_DATE                 | datetime     | NO   |     | 0000-00-00 00:00:00 |                |
| ASSESSMENT_DESC         | varchar(150) | NO   |     |                     |                |
| FILTER_CLIENT_IP        | varchar(50)  | YES  |     | NULL                |                |
| FILTER_SERVER_IP        | varchar(50)  | YES  |     | NULL                |                |
| RECOMMENDATION_TEXT     | mediumtext   | NO   |     | NULL                |                |
| TOTAL_CIS_TESTS         | int(11)      | NO   |     | 0                   |                |
| TOTAL_CIS_PASS          | int(11)      | NO   |     | 0                   |                |
| TOTAL_STIG_TESTS        | int(11)      | NO   |     | 0                   |                |
| TOTAL_STIG_PASS         | int(11)      | NO   |     | 0                   |                |
| TOTAL_CVE_TESTS         | int(11)      | NO   |     | 0                   |                |
| TOTAL_CVE_PASS          | int(11)      | NO   |     | 0                   |                |
+-------------------------+--------------+------+-----+---------------------+----------------+
19 rows in set (0.00 sec)

	
 */
	public AssessmentResultHeader () {
		
	}
	
	public AssessmentResultHeader(long assessmentResultId, int assessmentId,
			long taskId, boolean parameterModifiedFlag, Date executionDate,
			boolean receivedByAll, double overallScore, Date fromDate,
			Date toDate, String assessmentDesc, String filterClientIp,
			String filterServerIp, String recommendationText,
			int totalCisTests, int totalCisPass, int totalStigTests,
			int totalStigPass, int totalCveTests, int totalCvePass) {
		super();
		this.assessmentResultId = assessmentResultId;
		this.assessmentId = assessmentId;
		this.taskId = taskId;
		this.parameterModifiedFlag = parameterModifiedFlag;
		this.executionDate = executionDate;
		this.receivedByAll = receivedByAll;
		this.overallScore = overallScore;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.assessmentDesc = assessmentDesc;
		this.filterClientIp = filterClientIp;
		this.filterServerIp = filterServerIp;
		this.recommendationText = recommendationText;
		this.totalCisTests = totalCisTests;
		this.totalCisPass = totalCisPass;
		this.totalStigTests = totalStigTests;
		this.totalStigPass = totalStigPass;
		this.totalCveTests = totalCveTests;
		this.totalCvePass = totalCvePass;
	}




	private long assessmentResultId;

    private int assessmentId;

    private long taskId;
 
    private boolean parameterModifiedFlag = false;
          
    private Date executionDate;
             
    private boolean receivedByAll = false;
 
    private double overallScore = 0.0;
    
    private Date fromDate;
    
    private Date toDate;
    
    private String assessmentDesc;
    
    private String filterClientIp;
    
    private String filterServerIp;
    
    private String recommendationText;
    
    private int totalCisTests = 0;
    private int totalCisPass = 0;
    private int totalStigTests = 0;
    private int totalStigPass = 0;
    private int totalCveTests = 0;
    private int totalCvePass = 0;
    
    
	public long getAssessmentResultId() {
		return assessmentResultId;
	}
	public void setAssessmentResultId(long assessmentResultId) {
		this.assessmentResultId = assessmentResultId;
	}
	public int getAssessmentId() {
		return assessmentId;
	}
	public void setAssessmentId(int assessmentId) {
		this.assessmentId = assessmentId;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public boolean isParameterModifiedFlag() {
		return parameterModifiedFlag;
	}
	public void setParameterModifiedFlag(boolean parameterModifiedFlag) {
		this.parameterModifiedFlag = parameterModifiedFlag;
	}
	public Date getExecutionDate() {
		return executionDate;
	}
	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}
	public boolean isReceivedByAll() {
		return receivedByAll;
	}
	public void setReceivedByAll(boolean receivedByAll) {
		this.receivedByAll = receivedByAll;
	}
	public double getOverallScore() {
		return overallScore;
	}
	public void setOverallScore(double overallScore) {
		this.overallScore = overallScore;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public String getAssessmentDesc() {
		return assessmentDesc;
	}
	public void setAssessmentDesc(String assessmentDesc) {
		this.assessmentDesc = assessmentDesc;
	}
	public String getFilterClientIp() {
		return filterClientIp;
	}
	public void setFilterClientIp(String filterClientIp) {
		this.filterClientIp = filterClientIp;
	}
	public String getFilterServerIp() {
		return filterServerIp;
	}
	public void setFilterServerIp(String filterServerIp) {
		this.filterServerIp = filterServerIp;
	}
	public String getRecommendationText() {
		return recommendationText;
	}
	public void setRecommendationText(String recommendationText) {
		this.recommendationText = recommendationText;
	}
	public int getTotalCisTests() {
		return totalCisTests;
	}
	public void setTotalCisTests(int totalCisTests) {
		this.totalCisTests = totalCisTests;
	}
	public int getTotalCisPass() {
		return totalCisPass;
	}
	public void setTotalCisPass(int totalCisPass) {
		this.totalCisPass = totalCisPass;
	}
	public int getTotalStigTests() {
		return totalStigTests;
	}
	public void setTotalStigTests(int totalStigTests) {
		this.totalStigTests = totalStigTests;
	}
	public int getTotalStigPass() {
		return totalStigPass;
	}
	public void setTotalStigPass(int totalStigPass) {
		this.totalStigPass = totalStigPass;
	}
	public int getTotalCveTests() {
		return totalCveTests;
	}
	public void setTotalCveTests(int totalCveTests) {
		this.totalCveTests = totalCveTests;
	}
	public int getTotalCvePass() {
		return totalCvePass;
	}
	public void setTotalCvePass(int totalCvePass) {
		this.totalCvePass = totalCvePass;
	}
    
	public void dump () {
		System.out.println("assessment result header id: " + this.getAssessmentResultId());
		System.out.println("assessment result header test id: " + this.getAssessmentId());
		System.out.println("assessment result header desc: " + this.getAssessmentDesc());
		System.out.println("assessment result header task id: " + this.getTaskId());
		
	}
	// new add
	private SecurityAssessment securityAssessment;


	public SecurityAssessment getSecurityAssessment() {
		return securityAssessment;
	}

	public void setSecurityAssessment(SecurityAssessment securityAssessment) {
		this.securityAssessment = securityAssessment;
	}
	
	private List <Datasource> aDatasourceList;
	
	public void addAssessmentResultDatasource (List <Datasource> ds) {
		aDatasourceList = ds;
	}
	
	private Datasource aDatasource;
	
	public String getDatasourceNames() {
		String str = "";
		// TODO - later
		// get datasource info
		if (aDatasource != null )
			str = aDatasource.getName();
		
		return str;
	}
	
	AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
	
	public void save () {
		AssessmentResultHeaderPeer.add(this);
		return;
	}
}
