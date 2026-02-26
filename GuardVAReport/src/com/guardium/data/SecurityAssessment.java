/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.data;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.guardium.map.SecurityAssessmentMap;
import com.guardium.utils.WriteResult;

public class SecurityAssessment {
	
	SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
	
	public SecurityAssessment (int id, String desc, int ttype, String from_d, String to_d, boolean modified, String client_ip, String server_ip){
		assessment_id = id;
		assessment_desc = desc;
		test_type = ttype;
		from_date = from_d;
		to_date = to_d;
		modified_flag = modified;
		filter_client_ip = client_ip;
		filter_server_ip = server_ip;
	}
	
	
	private int assessment_id;
	private String assessment_desc;
	private int test_type;   // 1 for QUERY_BASED test, 2 for CVE test
	private String from_date;
	private String to_date;
	private boolean modified_flag;
	private String filter_client_ip;
	private String filter_server_ip;
	
	private int [] test_summary;  // 
	
	public int getAssessmentId() {
		return assessment_id;
	}
	public void setAssessmentId(int assessment_id) {
		this.assessment_id = assessment_id;
	}
	public String getAssessmentDesc() {
		return assessment_desc;
	}
	public void setAssessmentDesc(String assessment_desc) {
		this.assessment_desc = assessment_desc;
	}
	public int getTestType () {
		return test_type;
	}
	public void setTestType (int t) {
		this.test_type = t;
	}
	public String getFromDate() {
		return from_date;
	}
	public void setFromDate(String from_date) {
		this.from_date = from_date;
	}
	public String getToDate() {
		return to_date;
	}
	public void setToDate(String do_date) {
		this.to_date = do_date;
	}
	public boolean isModifiedFlag() {
		return modified_flag;
	}
	public void setModifiedFlag(boolean modified_flag) {
		this.modified_flag = modified_flag;
	}
	public String getFilterClientIp() {
		return filter_client_ip;
	}
	public void setFilterClientIp(String filter_client_ip) {
		this.filter_client_ip = filter_client_ip;
	}
	public String getFilterServerIp() {
		return filter_server_ip;
	}
	public void setFilterServerIp(String filter_server_ip) {
		this.filter_server_ip = filter_server_ip;
	}
	
	public int [] getTestSummary () {
		return test_summary;
	}
	public void setTestSummary (int [] t) {
		test_summary = t;
	}
	
	public void dump() {
		//
		WriteResult.writeOutput("Assessment ID: " + this.getAssessmentId());
		WriteResult.writeOutput("Assessment Desc: " + this.getAssessmentDesc());
		//WriteResult.writeOutput("assessment test type: " + this.getTestType());
		String tmpstr = "";
		if (this.getTestType() == 1) {
			tmpstr = "QUERY_BASED Test";
		}
		else if (this.getTestType() == 1) {
			tmpstr = "CVE Test";
		}
		WriteResult.writeOutput("Assessment Test Type: " + tmpstr);
		
		WriteResult.writeOutput("Assessment Test List Size: " + this.getAssessmentTests().size());
		WriteResult.writeOutput("Assessment Datasource List Size: " + this.getDatasources().size());
		Datasource ds = this.getDatasource(0);
		WriteResult.writeOutput("Assessment Datasource Type ID: " + ds.getDatasourceTypeId());
		WriteResult.writeOutput("Assessment Datasource Type Name: " + ds.getTypeName());
		WriteResult.writeOutput("Assessment Datasource Name: " + ds.getName());
		WriteResult.writeOutput("");
	}
	
	public void dumpResult(Writer wr) {
		//
		List <String> slist = new ArrayList<String>();
		String str = "";
		
		// WriteResult.writeOutput
		//str = "Test Result ID: " + this.getTestResultId();
		str = "TestResult Summary:";
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment ID: " + this.getAssessmentId();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Desc: " + this.getAssessmentDesc();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		//WriteResult.writeOutput("assessment test type: " + this.getTestType());
		
		String tmpstr = "";
		if (this.getTestType() == 1) {
			tmpstr = "QUERY_BASED Test";
		}
		else if (this.getTestType() == 1) {
			tmpstr = "CVE Test";
		}
		str = "Assessment Test List Size: " + this.getAssessmentTests().size();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Datasource List Size: " + this.getDatasources().size();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		Datasource ds = this.getDatasource(0);
		str = "Assessment Datasource Type Id: " + ds.getDatasourceTypeId();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Datasource Type Name: " + ds.getTypeName();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Datasource Name: " + ds.getName();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Datasource Major Version: " + ds.getMajorVersion();
		WriteResult.writeOutput(str);
		slist.add(str);		
		
		str = "Assessment Datasource Minor Version: " + ds.getMinorVersion();
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Assessment Datasource Patch: " + ds.getPatchLevel();
		WriteResult.writeOutput(str);
		slist.add(str);	
		// get result
		slist.addAll(this.dumpResultSummary());
		WriteResult.writeToLogFile(slist, wr);
        return;
	}
	
	public void save () {
		SecurityAssessmentPeer.add(this);
		return;
	}
	
	private List<AssessmentTest> aTestList = new ArrayList<AssessmentTest>();
	
	public void setAssessmentTests(List<AssessmentTest> v) {
		aTestList = v;
		return;
	}
	
	public List<AssessmentTest> getAssessmentTests () {
		return aTestList;
	}
	
	private List <Datasource> dslist = new ArrayList <Datasource>();
	
	public List<Datasource> listDatasources() {
		return dslist;
	}
	
	public List<Datasource> getDatasources() {
		return dslist;
	}
	
	public void setDatasources(List <Datasource> v) {
		dslist = v;
		return;
	}	
	
	public void addDatasources(Datasource v) {
		dslist.add(v);
		return;
	}		
	
	public int getTotalTest () {
		return aTestList.size();
	}
	
	public Datasource getDatasource (int idx) {
		return dslist.get(idx);
	}
	
	
	public List<String> dumpResultSummary () {
		int [] anArray = getTestSummary();
		
		int passCount = anArray[0];
		int failCount = anArray[1];
		int errorCount = anArray[2];
		int noCasCount = anArray[3];
		int noReportCount = anArray[4];
		int unSupportDBCount = anArray[5];
		int unSupportOSCount = anArray[6];
		int specialErrorCount = anArray[7];
		int casCollectErrorCount = anArray[8];
		int obsParaCount = anArray[9];
		int depParaCount = anArray[10];
		int cveNotCount = anArray[11];
		int noUserDataCount = anArray[12];
		int moduleNotCount = anArray[13];
		int PreTestFailedCount = anArray[14];
		int ExecTestFailedCount = anArray[15];
		
		List <String> slist = new ArrayList<String>();
		String str = "";
		
		// write result
		
		str = "Tests PASS: " + passCount;
		WriteResult.writeOutput(str);
		slist.add(str);
		
		str = "Tests FAIL: " + failCount;
		WriteResult.writeOutput(str);
		slist.add(str);
	
		if (errorCount > 0) {
			str = "Tests ERROR: " + errorCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (noCasCount > 0) {
			str = "Tests NO_CAS_DATA:	" + noCasCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (noReportCount > 0) {
			str = "Tests NO_REPORT_DATA: " + noReportCount;	
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (unSupportDBCount > 0) {
			str = "Tests UNSUPPORTED_DB_VERSION: " +  unSupportDBCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (unSupportOSCount > 0) {
			str = "Tests UNSUPPORTED_OS_VERSION: " + unSupportOSCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
	
		if (specialErrorCount > 0) {
			str = "Tests SPECIAL_ERROR: " + specialErrorCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (casCollectErrorCount > 0) {
			str = "Tests CAS_DATA_COLLECTION_ERROR: " + casCollectErrorCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
	
		if (obsParaCount > 0) {
			str = "Tests OBSOLETE_PARAMETER: " + obsParaCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (depParaCount > 0) {
			str = "Tests DEPRECATED_PARAMETER: " + depParaCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (cveNotCount > 0) {
			str = "Tests CVE_NOT_REPORTED: " + cveNotCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (noUserDataCount > 0) {
			str = "Tests NO_USER_DATA: " + noUserDataCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (moduleNotCount > 0) {		
			str = "Tests MODULES_NOT_PRESENT: " + moduleNotCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (PreTestFailedCount > 0) {
			str = "Tests PRE_TEST_CHECK_FAILED: " + PreTestFailedCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		if (ExecTestFailedCount > 0) {
			str = "Tests EXECUTION_TEST_ROUTINE_CHECK_FAILED: " + ExecTestFailedCount;
			WriteResult.writeOutput(str);
			slist.add(str);
		}
		
		str = "Total Tests: " + this.getTotalTest();
		WriteResult.writeOutput(str);
		slist.add(str);
	
		return slist;
	}
	
/*
mysql> desc SECURITY_ASSESSMENT;
+------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| ASSESSMENT_ID    | int(11)      | NO   | PRI | NULL    | auto_increment |
| ASSESSMENT_DESC  | varchar(150) | NO   |     |         |                |
| FROM_DATE        | varchar(30)  | NO   |     |         |                |
| TO_DATE          | varchar(30)  | NO   |     |         |                |
| MODIFIED_FLAG    | int(11)      | NO   |     | 0       |                |
| FILTER_CLIENT_IP | varchar(50)  | YES  |     | NULL    |                |
| FILTER_SERVER_IP | varchar(50)  | YES  |     | NULL    |                |
+------------------+--------------+------+-----+---------+----------------+
7 rows in set (0.00 sec)

mysql> select * from  SECURITY_ASSESSMENT;
+---------------+-----------------+------------+---------+---------------+------------------+------------------+
| ASSESSMENT_ID | ASSESSMENT_DESC | FROM_DATE  | TO_DATE | MODIFIED_FLAG | FILTER_CLIENT_IP | FILTER_SERVER_IP |
+---------------+-----------------+------------+---------+---------------+------------------+------------------+
|         20000 | aaa             | NOW -1 DAY | NOW     |             0 | NULL             | NULL             |
+---------------+-----------------+------------+---------+---------------+------------------+------------------+
1 row in set (0.00 sec)
 */
}
