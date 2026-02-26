/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.guardium.assessment.tests.TestScore;
import com.guardium.map.TestResultMap;
import com.guardium.utils.WriteResult;

public class TestResult {

	TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
	
	/*
	 * 
mysql> desc TEST_RESULT;
+---------------------------------+--------------+------+-----+---------+----------------+
| Field                           | Type         | Null | Key | Default | Extra          |
+---------------------------------+--------------+------+-----+---------+----------------+
| TEST_RESULT_ID                  | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| ASSESSMENT_RESULT_ID            | bigint(20)   | NO   |     | 0       |                |
| TEST_ID                         | int(11)      | NO   |     | 0       |                |
| ASSESSMENT_TEST_ID              | int(11)      | NO   |     | 0       |                |
| TEST_SCORE                      | int(11)      | NO   |     | -1      |                |
| REPORT_RESULT_ID                | bigint(20)   | NO   |     | 0       |                |
| PARAMETER_MODIFIED_FLAG         | int(11)      | NO   |     | 0       |                |
| RESULT_TEXT                     | mediumtext   | YES  |     | NULL    |                |
| TEST_DESC                       | varchar(150) | NO   |     |         |                |
| RECOMMENDATION_TEXT             | mediumtext   | NO   |     | NULL    |                |
| SCORE_DESC                      | varchar(255) | NO   |     |         |                |
| THRESHOLD_STRING                | varchar(200) | YES  |     | NULL    |                |
| SEVERITY                        | varchar(60)  | NO   |     | INFO    |                |
| CATEGORY_NAME                   | varchar(60)  | YES  |     |         |                |
| ASSESSMENT_RESULT_DATASOURCE_ID | bigint(20)   | NO   |     | -1      |                |
| DETAIL                          | mediumtext   | YES  |     | NULL    |                |
| SHORT_DESCRIPTION               | mediumtext   | NO   |     | NULL    |                |
| EXTERNAL_REFERENCE              | varchar(255) | NO   |     |         |                |
| EXCEPTION_GROUP_DESCRIPTION     | varchar(150) | YES  |     | NULL    |                |
| SQL_STMT_SENT                   | mediumtext   | YES  |     | NULL    |                |
| DATA_SOURCE_HASH                | varchar(40)  | YES  |     | NULL    |                |
+---------------------------------+--------------+------+-----+---------+----------------+
21 rows in set (0.00 sec)

	 */
	public TestResult () {
		
	}
	
	public TestResult(long test_result_id, long assessment_result_id,
			int test_id, int assessment_test_id, TestScore test_score,
			long report_result_id, boolean para_modified_flag,
			String result_text, String test_desc, String recomm_text,
			String score_desc, String threshold_string, String severity,
			String category_name, long assessment_result_ds_id, String detail,
			String short_desc, String external_ref,
			String exception_group_desc, String sql_stmt_sent,
			String data_source_hash,
			String s_ref, String s_severity, String s_iacontrols, String s_srg) {
		super();
		this.test_result_id = test_result_id;
		this.assessment_result_id = assessment_result_id;
		this.test_id = test_id;
		this.assessment_test_id = assessment_test_id;
		this.test_score = test_score;
		this.report_result_id = report_result_id;
		this.para_modified_flag = para_modified_flag;
		this.result_text = result_text;
		this.test_desc = test_desc;
		this.recomm_text = recomm_text;
		this.score_desc = score_desc;
		this.threshold_string = threshold_string;
		this.severity = severity;
		this.category_name = category_name;
		this.assessment_result_ds_id = assessment_result_ds_id;
		this.detail = detail;
		this.short_desc = short_desc;
		this.external_ref = external_ref;
		this.exception_group_desc = exception_group_desc;
		this.sql_stmt_sent = sql_stmt_sent;
		this.data_source_hash = data_source_hash;
	    this.stig_ref = s_ref;
		this.stig_severity = s_severity;
		this.stig_iacontrols = s_iacontrols;
		this.stig_srg = s_srg;	    
	    
	}



	long test_result_id;
	long assessment_result_id;
	int test_id;
	int assessment_test_id;
	//int test_score;
	TestScore test_score;
	long report_result_id;
	boolean para_modified_flag;
	String result_text;
	String test_desc;
	String recomm_text;
	String score_desc;
	String threshold_string;
	String severity;
	String category_name;
	long assessment_result_ds_id;
	String detail;
	String short_desc;
	String external_ref;
	String exception_group_desc;
	String sql_stmt_sent;
	String data_source_hash;

	String datasourceType;
	String datasourceDesc;
	String datasourceVersion;
	String testStartTime;
	String testEndTime;
	String stig_ref;
	String stig_severity; 
	String stig_iacontrols;
	String stig_srg;
	
	public String getThreshold_string() {
		return threshold_string;
	}

	public void setThreshold_string(String threshold_string) {
		this.threshold_string = threshold_string;
	}

	public String getDatasourceType() {
		return datasourceType;
	}

	public void setDatasourceType(String datasourceType) {
		this.datasourceType = datasourceType;
	}

	public String getDatasourceDesc() {
		return datasourceDesc;
	}

	public void setDatasourceDesc(String datasourceDesc) {
		this.datasourceDesc = datasourceDesc;
	}

	public String getDatasourceVersion() {
		return datasourceVersion;
	}

	public void setDatasourceVersion(String datasourceVersion) {
		this.datasourceVersion = datasourceVersion;
	}

	public String getTestStartTime() {
		return testStartTime;
	}

	public void setTestStartTime(String testStartTime) {
		this.testStartTime = testStartTime;
	}

	public String getTestEndTime() {
		return testEndTime;
	}

	public void setTestEndTime(String endTime) {
		this.testEndTime = endTime;
	}
	
	
	public long getTestResultId() {
		return test_result_id;
	}
	public void setTestResultId(long test_result_id) {
		this.test_result_id = test_result_id;
	}
	public long getAssessmentResultId() {
		return assessment_result_id;
	}
	public void setAssessmentResultId(long assessment_result_id) {
		this.assessment_result_id = assessment_result_id;
	}
	public int getTestId() {
		return test_id;
	}
	public void setTestId(int test_id) {
		this.test_id = test_id;
	}
	public int getAssessmentTestId() {
		return assessment_test_id;
	}
	public void setAssessmentTestId(int assessment_test_id) {
		this.assessment_test_id = assessment_test_id;
	}
	public TestScore getTestScore() {
		return test_score;
	}
	public void setTestScore(TestScore test_score) {
		this.test_score = test_score;
	}
	public long getReportResultId() {
		return report_result_id;
	}
	public void setReportResultId(long report_result_id) {
		this.report_result_id = report_result_id;
	}
	
	public boolean isParameterModifiedFlag() {
		return para_modified_flag;
	}
	
	public boolean getParameterModifiedFlag() {
		return para_modified_flag;
	}
	
	public void setParameterModifiedFlag(boolean para_modified_flag) {
		this.para_modified_flag = para_modified_flag;
	}
	public String getResultText() {
		return result_text;
	}
	public void setResultText(String result_text) {
		this.result_text = result_text;
	}
	public String getTestDesc() {
		return test_desc;
	}
	public void setTestDesc(String test_desc) {
		this.test_desc = test_desc;
	}
	public String getRecommendationText() {
		return recomm_text;
	}
	public void setRecommendationText(String recomm_text) {
		this.recomm_text = recomm_text;
	}
	public String getScoreDesc() {
		return score_desc;
	}
	public void setScoreDesc(String score_desc) {
		this.score_desc = score_desc;
	}
	
	public String getThresholdString() {
		return threshold_string;
	}
	public void setThresholdString(String threshold_string) {
		this.threshold_string = threshold_string;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getCategoryName() {
		return category_name;
	}
	public void setCategoryName(String category_name) {
		this.category_name = category_name;
	}
	public long getAssessmentResultDsId() {
		return assessment_result_ds_id;
	}
	public void setAssessment_resultDsId(long assessment_result_ds_id) {
		this.assessment_result_ds_id = assessment_result_ds_id;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getShortDescription() {
		return short_desc;
	}
	public void setShortDescription(String short_desc) {
		this.short_desc = short_desc;
	}
	public String getExternalReference() {
		return external_ref;
	}
	public void setExternalReference(String external_ref) {
		this.external_ref = external_ref;
	}
	public String getExceptionGroupDescription() {
		return exception_group_desc;
	}
	public void setExceptionGroupDescription(String exception_group_desc) {
		this.exception_group_desc = exception_group_desc;
	}
	public String getSqlStmtSent() {
		return sql_stmt_sent;
	}
	public void setSqlStmtSent(String sql_stmt_sent) {
		this.sql_stmt_sent = sql_stmt_sent;
	}
	public String getDataSourceHash() {
		return data_source_hash;
	}
	public void setDataSourceHash(String data_source_hash) {
		this.data_source_hash = data_source_hash;
	}
	
    public String getStig_ref() {
		return stig_ref;
	}

	public void setStig_ref(String stig_ref) {
		this.stig_ref = stig_ref;
	}

	public String getStig_severity() {
		return stig_severity;
	}

	public void setStig_severity(String stig_severity) {
		this.stig_severity = stig_severity;
	}

	public String getStig_iacontrols() {
		return stig_iacontrols;
	}

	public void setStig_iacontrols(String stig_iacontrols) {
		this.stig_iacontrols = stig_iacontrols;
	}

	public String getStig_srg() {
		return stig_srg;
	}

	public void setStig_srg(String stig_srg) {
		this.stig_srg = stig_srg;
	}

	
	
	public List<String> dump () {
		
		List <String> slist = new ArrayList<String>();
		String str = "";
		
		// WriteResult.writeOutput
		str = "Test Result ID: " + this.getTestResultId();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		int testid = this.getTestId();
		boolean fullFlag = false;
		if (testid >= 1000 && testid < 2000) {
			// CVE test - do we need full?
			fullFlag = true;
		}
		else if (testid >= 2000) {
			// it is query based test, check the list to see if we need to give more information
			
			
			/*
			    D.NAME = 'DB2' AND A.TEST_ID IN (2086,2111,2113,2114,2115,2198,2201,2251,2258,2259)
			 OR D.NAME = 'INFORMIX' AND A.TEST_ID IN (2273,2276,2278,2279,2280,2281,2282,2284,2286,2308)
			 OR D.NAME = 'MS SQL SERVER' AND A.TEST_ID IN (2013,2011,2009,2004,2194,2289,2296,2298,2301,2313)
			 OR D.NAME = 'NETEZZA' AND A.TEST_ID IN (2053,2052,2051,2050,2049)
			 OR D.NAME = 'ORACLE' AND A.TEST_ID IN (2022,2021,2015,2016,2311,2312,2373,2378,2381,2453)
			 OR D.NAME = 'SYBASE' AND A.TEST_ID IN (2062,2063,2067,2068,2069,2070,2072,2075,2079,2081)
			 OR D.NAME = 'SYBASE IQ' AND A.TEST_ID IN (2214,2215,2216,2217,2219,2220,2222,2224,2227,2229)
			 OR D.NAME = 'TERADATA' AND A.TEST_ID IN (2048,2046,2036,2035,2032,2034,2029,2026,2023,2024)
			)
			*/
			
			if( Arrays.asList(2086,2111,2113,2114,2115,2198,2201,2251,2258,2259, 
					          2273,2276,2278,2279,2280,2281,2282,2284,2286,2308,
					          2013,2011,2009,2004,2194,2289,2296,2298,2301,2313,
					          2053,2052,2051,2050,2049,
					          2022,2021,2015,2016,2311,2312,2373,2378,2381,2453,
					          2062,2063,2067,2068,2069,2070,2072,2075,2079,2081,
					          2214,2215,2216,2217,2219,2220,2222,2224,2227,2229,
					          2048,2046,2036,2035,2032,2034,2029,2026,2023,2024
					).contains(testid) ) {
				fullFlag = true;
			}
		}
		
		//WriteResult.writeOutput("test result assseement result id: " + this.getAssessmentResultId());
		str = "Test ID: " + this.getTestId();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Test Desc: " + this.getTestDesc();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Test Score: " + this.getTestScore();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		if (fullFlag) {
			str = "Test Result Text: " + this.getResultText();
			WriteResult.writeOutput(str);
			slist.add(str);
			
			str = "Test Result Recomm: " + this.getRecommendationText();
			WriteResult.writeOutput(str);
			slist.add(str);
		
			str = "Test Result Severity: " + this.getSeverity();
			WriteResult.writeOutput(str);
			slist.add(str);
		
			str = "Test Result Category: " + this.getCategoryName();
			WriteResult.writeOutput(str);
			slist.add(str);
		
			str = "Test Result Short Desc: " + this.getShortDescription();
			WriteResult.writeOutput(str);
			slist.add(str);
		
			str = "Test Result External Reference: " + this.getExternalReference();
			WriteResult.writeOutput(str);
			slist.add(str);
		
			str = "Test Result STIG Reference: " + this.getStig_ref();
			WriteResult.writeOutput(str);
			slist.add(str);
			
			//str = "Test Result Detail: " + this.getDetail();
			str = "Test Result Detail: " + "Result detail is only available in the full version of Guardium vulnerability assessment.";
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		// get datasourcr infor
		str = "Test Result Datasource Desc: " + this.getDatasourceDesc();
		WriteResult.writeOutput(str);
		slist.add(str);		
		
		str = "Test Result Datasource Type: " + this.getDatasourceType();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Test Result Datasource Version: " + this.getDatasourceVersion();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		// get time ???
		
		str = "";
		WriteResult.writeOutput(str);
		slist.add(str);
		
		return slist;
	}
	
	private AssessmentResultHeader aResultHeader;
	
	public AssessmentResultHeader getAssessmentResultHeader () {
		return aResultHeader;
	}
	
	public void setAssessmentResultHeader(AssessmentResultHeader resultHeader) {
		aResultHeader = resultHeader;
		return;
	}
	
	public void save () {
		TestResultPeer.add(this);
		return;
	}

	
}
