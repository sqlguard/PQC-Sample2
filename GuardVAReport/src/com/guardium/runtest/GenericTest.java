/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
/*
 * Created on Nov 5, 2003
 *
 * ?? Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material may not
 * be copied, modified, altered, published, distributed, or otherwise displayed without the
 * express written consent of Guardium, Inc.
 *
 */
package com.guardium.runtest;

import java.security.MessageDigest;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


import com.guardium.assessment.i18n.Say;
import com.guardium.assessment.tests.Constants;
import com.guardium.assessment.tests.GuardAssessmentException;
import com.guardium.assessment.tests.TestScore;
//import com.guardium.datamodel.adminconsole.AdminconsoleParameter;
//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
//import com.guardium.datamodel.adminconsole.GuardParametersPeer;
import com.guardium.map.AssessmentLogMap;
import com.guardium.map.AssessmentResultHeaderMap;
import com.guardium.map.ReportHeaderMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.AvailableTest;
import com.guardium.map.AvailableTestMap;
import com.guardium.data.VaSummary;
import com.guardium.map.VaSummaryMap;
/*
import com.guardium.datamodel.assessment.CveReference;
import com.guardium.datamodel.assessment.CveReferencePeer;
import com.guardium.datamodel.assessment.CvssInfo;
import com.guardium.datamodel.assessment.CvssInfoPeer;
*/
import com.guardium.map.RecommendationTextMap;
//import com.guardium.datamodel.assessment.ResultCveReference;
//import com.guardium.datamodel.assessment.ResultCvssInfo;
import com.guardium.data.SqlbasedAssessmentDefinition;
import com.guardium.map.SqlbasedAssessmentDefinitionMap;
import com.guardium.data.TestExceptions;
import com.guardium.map.TestExceptionsMap;
import com.guardium.data.TestResult;
import com.guardium.map.TestResultMap;
import com.guardium.data.VaSummary;
import com.guardium.map.VaSummaryMap;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
import com.guardium.data.GroupMember;
import com.guardium.map.GroupMemberMap;
//import com.guardium.datamodel.report.GuardRepGeneralException;
//import com.guardium.datamodel.report.GuardRepInvalidQueryException;
//import com.guardium.datamodel.report.GuardRepNotFoundException;
//import com.guardium.datamodel.report.GuardRepTableNotExistException;
import com.guardium.data.ReportHeader;
import com.guardium.map.ReportHeaderMap;
//import com.guardium.datamodel.report.RunTimeParameter;
//import com.guardium.data.ReportResultHeader;
//import com.guardium.map.ReportResultHeaderMap;
import com.guardium.data.DataSourceConnectException;
//import com.guardium.presentation.util.ReportGenerator;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.utils.JdbcUtils;
import com.guardium.utils.Stringer;
import com.guardium.utils.Utils;
import com.mongodb.MongoClient;

/**
 * @author dario
 */
public abstract class GenericTest {
	// get all maps
	
	AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
	SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionPeer = SqlbasedAssessmentDefinitionMap.getSqlbasedAssessmentDefinitionMapObject();
	
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
	TestExceptionsMap TestExceptionsPeer = TestExceptionsMap.getTestExceptionsMapObject();
	
	AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
	
	ReportHeaderMap ReportHeaderPeer = ReportHeaderMap.getReportHeaderMapObject();
	VaSummaryMap VaSummaryPeer = VaSummaryMap.getVaSummaryMapObject();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	
	//public VaSummaryMap VaSummaryPeer = new VaSummaryMap();
	
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(GenericTest.class);

	/** Constant for the connection name to use when generating a report */
	private static final String TORQUE_REPORT_CONNECTION = "gdm_audit";

	/** The Assessment Result Header */
	private AssessmentResultHeader resultHeader;
	/** The Assessment Test */
	protected AssessmentTest assessmentTest;
	/** The Report Header */
	private ReportHeader repHeader = null;
	/** The Available Test Test ID */
	protected int testId;

	/** Map passing data between tests of the same datasource in the same run */
	protected Map cachedParameterMap = null;
	protected boolean parameterRetreivalFailed = false;
	protected boolean configrRetreivalFailed = false;
		
	public boolean isParameterRetreivalFailed() {
		return parameterRetreivalFailed;
	}

	public void setParameterRetreivalFailed(boolean parameterRetreivalFailed) {
		this.parameterRetreivalFailed = parameterRetreivalFailed;
	}
	private int removedDetailCount;
	
	public Map getCachedParameterMap() {
		return cachedParameterMap;
	}

	public void setCachedParameterMap(Map cachedParameterMap) {
		this.cachedParameterMap = cachedParameterMap;
	}
	/** convenience reference to the test result */
	private TestResult testResult = null;

	/** The datasource for JDBC or CAS tests */
	private Datasource datasource = null;

	private List<String> detail = null;
	
	private String detailStr = null;
	
	private StringBuilder passResultText = new StringBuilder();

	/** Stores the connection injection for the call to calculate Score */
	protected Connection connection = null;
	
	protected MongoClient mconnection = null;
	
	private static String dataSourceHashKey = null;

	/**
	 * @param datasource
	 */
	private void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	protected void setPassResultText(String text) {
		this.passResultText = new StringBuilder(text);
	}

	public String getPassResultText() {
		return this.passResultText.toString();
	}
	
	protected void appendPassResultText(String value) {
		this.passResultText.append(value);
	}

	protected void appendPassResultTextListSep(String value) {
		Stringer.listSep(this.passResultText).append(value);
	}
	
	
	
	/**
	 * @return the datasource
	 */
	protected Datasource getDatasource() {
		return this.datasource;
	}
	
	/**
	 * @return the datasource Hostname
	 *  added for mongodb 
	 */
	protected String getHost() {
		return datasource.getHost();
	}
	
	/**
	 * @return the datasource type
	 *  *  added for mongodb 
	 */
	protected Boolean isSqltype() {
		return datasource.isSqltype();
	}
		
	/**
	 * @return the datasource user name
	 *  *  added for mongodb 
	 */
	protected String getUserName() {
		if ( datasource.getPasswordStored() ) {
			return datasource.getUserName();
		}
		else
			return datasource.getScreenUserName();
	}
	
	/**
	 * @return the datasource password 
	 * @throws DataSourceConnectException 
	 *  *  added for mongodb 
	 */
	protected String getPassword() throws DataSourceConnectException {
		if ( datasource.getPasswordStored() ) {
			// use the stored versions			
			return datasource.getDecryptedPassword();
		}
		else{
			// get the password
			return datasource.getScreenPassword();
		} 
	}
	
	/**
	 * @return the datasource user name
	 * @throws TorqueException 
	 *  *  added for mongodb 
	 */
	protected int getPort() {
		int port = datasource.getPort();
	
		if(port == 0)
		{
			DatasourceType type;			
			type = datasource.getDatasourceType();
			port = type.getDefaultPort();
			//port = 27017;
		}
		return port;
	}
	

	/**
	 * Executes the test on concrete descendant classes
	 * The descendant is responsible for creating the test results.
	 * This can be done with <code>recordResults(String result, int Score)</code>
	 * @param assessmentFromDate
	 * @param assessmentToDate
	 * @param filterClientIP
	 * @param filterServerIP
	 * @param timeFactor
	 * @return The result of the test from one of constant values on TestScoreDefinitionPeer
	 */
	public abstract TestScore executeTest(
				String assessmentFromDate, String assessmentToDate,
				String filterClientIP, String filterServerIP,
				double timeFactor
	);

	/**
	 * Factory method reads the Available Test on the database to get the concrete class name and instantiate.
	 * @param assessmentTest
	 * @param resultHeader
	 * @param datasource
	 * @return The instantiated concrete test.
	 * @throws GuardAssessmentException
	 */
	public static GenericTest getTest(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, Datasource datasource
	) throws GuardAssessmentException {

		GenericTest test;
		String className = null;
		try {
			AvailableTest availableTest = assessmentTest.getAvailableTest();
			className = availableTest.getClassName();

			Class<? extends GenericTest> testClass;
			//AvailableTest.Type testType = availableTest.getType();
			
			testClass = (Class<GenericTest>) Class.forName(className);
			test = testClass.newInstance();
	
			// set the data fields
			test.setAssessmentTest(assessmentTest);
			test.setResultHeader(resultHeader);
			test.setDatasource(datasource);
			return test;

		} catch (ClassNotFoundException e) {
			// should not happen if the metadata is correct
			String msg =
				"Could not create the test class: '" + className + "' for: '" + assessmentTest + "'"
			;
			// LOG.warn(msg, e);
			throw new GuardAssessmentException(msg, e);

		} catch (InstantiationException e) {
			// Should not happen if metadata is correct
			String msg =
				"Could not instantiate the test class: '" + className + "' for: '" + assessmentTest + "'"
			;
			// LOG.warn(msg, e);
			throw new GuardAssessmentException(msg, e);

		} catch (IllegalAccessException e) {
			// Should not happen unless there is a java security problem
			String msg =
				"Did not have security to instantiate the test class: '" + className
				+ "' for: '" + assessmentTest + "'"
			;
			// LOG.warn(msg, e);
			throw new GuardAssessmentException(msg, e);

		} catch (Throwable t) {
			// Should not happen unless there is a bug
			String msg =
				"Unexpected exception trying to instantiate the test class: '" + className
				+ "' for: '" + assessmentTest + "'"
			;
			// LOG.warn(msg, t);
			throw new GuardAssessmentException(msg, t);
		}
	}

	/**
	 * @return Whether a report is associated with this test.
	 */
	protected boolean hasAssociatedReport() {
		int id = this.getAvailableTest().getReportId();
		return id > 0;
	}

	/**
	 * @return returns the Report Header for this test.
	 */
	protected ReportHeader getRepHeader() {
		if (this.repHeader==null && this.hasAssociatedReport() ) {

			int id = this.getAvailableTest().getReportId();
							
			List<ReportHeader> rpts = ReportHeaderPeer.getReportHeaderList(id);			
			if ( !Check.isEmpty(rpts)) {
				this.repHeader = rpts.get(0);
			}
		}
		return this.repHeader;
	}

	/**
	 * @param assessmentFromDate
	 * @param assessmentToDate
	 * @param filterClientIP
	 * @param filterServerIP
	 * @return The report result header for a freshly run report, if there is a report associated
	 *  with this Assessment test.  Null if none is associated, or there is a report failure.
	 * @throws GuardAssessmentException
	 */
	
	// TODO - later
	/*
	protected final ReportResultHeader generateReport(
			String assessmentFromDate, String assessmentToDate,
			String filterClientIP, String filterServerIP
	) throws GuardAssessmentException {

		ReportResultHeader result = null;

		// check for a report association
		if ( this.hasAssociatedReport() ) {

			// set the report parameters
			this.setDateParams(assessmentFromDate, assessmentToDate);
			this.setFilterParams(filterClientIP, filterServerIP);

			// run the report
			try {
				// initialize the report generator
				ReportGenerator gen = new ReportGenerator( this.getRepHeader(), true);
				gen.setConnectionName(TORQUE_REPORT_CONNECTION);

				// execute the report
				long id = gen.saveFullResult(null);
				result = ReportResultHeaderPeer.retrieveByPK(id);

			} catch (GuardRepTableNotExistException e) {
				throw new GuardAssessmentException( e.getLocalizedMessage(), e);

			} catch (GuardRepGeneralException e) {
				throw new GuardAssessmentException( e.getLocalizedMessage(), e);

			} catch (GuardRepInvalidQueryException e) {
				throw new GuardAssessmentException( e.getLocalizedMessage(), e);

			} catch (GuardRepNotFoundException e) {
				throw new GuardAssessmentException( e.getLocalizedMessage(), e);
			}

				// mark the report result header as complete
			try {
				result.setAssessmentResultId( this.getResultHeader().getAssessmentResultId() );
				result.setReceivedByAll(true);
				result.save();

			} catch (Exception e) {
				// should not occur if Torque is initialized
				AdHocLogger.logException(e);
			}
		}
		return result;
	}
    */
	
	/**
	 * Sets the run time date parameters.
	 * @param reportHeader
	 * @param fromDate
	 * @param toDate
	 */
	/*
	public void setDateParams(String fromDate, String toDate) {
		List<RunTimeParameter> list = this.getRepHeader().getParameters();
		for ( RunTimeParameter param : list ) {
			if ("QUERY_FROM_DATE".equalsIgnoreCase( param.getParameterName() ) ) {
				param.setRunTimeValue(fromDate);
			} else if ("QUERY_TO_DATE".equalsIgnoreCase( param.getParameterName() ) ) {
				param.setRunTimeValue(toDate);
			}
		}
	}
	*/
	
	/**
	 * Sets the run time IP parameters.
	 * @param reportHeader
	 * @param clientIP
	 * @param serverIP
	 */
	/*
	public void setFilterParams(String clientIP, String serverIP) {
		// convert ip wildcards to sql wildcards
		String rClientIP;
		if ( Check.isEmpty(clientIP) ) {
			rClientIP = "%";
		} else {
			rClientIP = chkIp( clientIP.replace('*','%') );
		}

		// convert ip wildcards to sql wildcards
		String rServerIP;
		if ( Check.isEmpty(serverIP) ) {
			rServerIP = "%";
		} else {
			rServerIP = chkIp( serverIP.replace('*','%') );
		}

		// iterate though the parameters and set the IP ones
		List<RunTimeParameter> list = this.getRepHeader().getParameters();
		for ( RunTimeParameter param : list ) {

			// set the parameters
			if ("CLIENT_IP_FILTER".equalsIgnoreCase( param.getParameterName() ) ) {
				param.setRunTimeValue(rClientIP);
			} else if ("SERVER_IP_FILTER".equalsIgnoreCase( param.getParameterName() ) ) {
				param.setRunTimeValue(rServerIP);
			}
		}
	}
    */
	/*
	protected long createEmpyReportResult(int overallValue) {
		try {
			ReportResultHeader repResult = new ReportResultHeader();
			repResult.setReportId(this.repHeader.getReportId());
			repResult.setReceivedByAll(true);
			repResult.setOverallValue(overallValue);
			repResult.setAssessmentResultId(this.resultHeader.getAssessmentResultId());
			repResult.setExecutionDate(new Date());
			repResult.save();
			return repResult.getReportResultId();
		}
		catch (Exception te) {
			AdHocLogger.logDebug(te.toString(),AdHocLogger.LOG_ERRORS);
			AdHocLogger.logException(te);
		}
		return -1;
	}
	*/
	
	/**
	 * Records the test results.
	 * Sets the report result id to -1.
	 * @param score
	 * @param result
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(int score, String result, Datasource datasource) {
		int reportResultId = -1;
		return this.recordResult(reportResultId, score, result, datasource);
	}

	/**
	 * Records the test results.
	 * @param reportResultId
	 * @param score
	 * @param result
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(
			int reportResultId, int score, String result, Datasource datasource
	) {
		return this.recordResult(reportResultId, TestScore.findTestScore(score), result, datasource);
	}

	/**
	 * Records the test results.
	 * Sets the report result id to -1.
	 * @param score
	 * @param result
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(TestScore score, String result) {
		Datasource datasource = this.getDatasource();
		return this.recordResult(score, result, datasource);
	}

	/**
	 * Records the test results.
	 * Sets the report result id to -1.
	 * @param score
	 * @param result
	 * @param recommend
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(TestScore score, String result, String recommend) {
		Datasource datasource = this.getDatasource();
		return this.recordResult(score, result, recommend, datasource);
	}


	/**
	 * Records the test results.
	 * Sets the report result id to -1.
	 * @param score
	 * @param result
	 * @param datasource
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(TestScore score, String result, Datasource datasource) {
		int reportResultId = -1;
		return this.recordResult(reportResultId, score, result, datasource);
	}

	/**
	 * Records the test results
	 * @param reportResultId
	 * @param testScore
	 * @param result
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(
			long reportResultId, TestScore testScore, String result
	) {
		Datasource datasource = null;
		return this.recordResult(reportResultId, testScore, result, datasource);
	}

	/**
	 * Records the test results
	 * @param reportResultId
	 * @param testScore
	 * @param result
	 * @param datasource
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(
			long reportResultId, TestScore testScore, String result, Datasource datasource
	) {
		// find the recommendation
		AvailableTest availableTest = this.getAvailableTest();
		String recommend = RecommendationTextPeer.findRecommendation(availableTest, testScore);
		return this.recordResult(reportResultId, testScore, result, recommend, datasource);
	}

	/**
	 * Records the test results
	 * @param score
	 * @param result
	 * @param recommend
	 * @param datasource
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(
			TestScore score, String result, String recommend, Datasource datasource
	) {
		return this.recordResult(-1, score, result, recommend, datasource);
	}

	/**
	 * Records the test results
	 * @param reportResultId
	 * @param score
	 * @param result
	 * @param recommend
	 * @param datasource
	 * @return The populated and saved Test Result
	 */
	protected TestResult recordResult(
			long reportResultId, TestScore score, String result, String recommend, Datasource datasource
	) {

		AssessmentTest assessmentTest = this.getAssessmentTest();
		AssessmentResultHeader resultHeader = this.getResultHeader();
		try {
			if(getDetail()==null&&(score == TestScore.FAIL))
			    setDetail(fetchDetail());
			
			if (getDetail() != null && !getDetail().isEmpty() && this.getAssessmentTest().getExceptionsGroupId() > 0) {
				
				List<String> newListForDetail = null;  //checkExceptions(getDetail(), this.getAssessmentTest().getExceptionsGroupId());
				if (newListForDetail.isEmpty()) {
					score = TestScore.PASS;
					this.setDetail(newListForDetail);
					setStringDetail("N/A");
					recommend = findRecommendation(score);
					result = getPassResultText();
				}
				else {
					if (removedDetailCount > 0)
						result = result + " " + Say.what(Say.ASMT_RESULT_FAIL_ADDITIONAL_TEXT,"removedCount", (new Integer(removedDetailCount)).toString());
					this.setDetail(newListForDetail);
				}
			}	
		} catch (Exception e) {
			AdHocLogger.logException(e);
			setStringDetail(Informer.causality(e));
		}
		
		// Check whether the combination test/datasource is exempted.
		// If exempted:
		//		Set the score to Pass 
		//      Set Recommendation to Explanation from exception record
		// 		Set result Text to: Exception ....
		try {
			if (score == TestScore.FAIL) {
				TestExceptions testException = null;   //TestExceptionsPeer.getLastTestExceptionRecord(this.getAvailableTest().getTestId(),datasource.getDatasourceId());
				if (testException != null) {
				// Found Exception Record
					recommend = testException.getExplanation();
					result = Say.what(
							Say.ASMT_RESULT_PASS_EXCEPTION,
							"approver",testException.getApprover(),	
							"fromdate",testException.getFromDate().toString(),
							"todate",testException.getToDate().toString());
					score = TestScore.PASS;
				}
			}
		}
		catch(Exception e) {
			AdHocLogger.logException(e);
		}
		
		TestResult testResult = TestResultPeer.recordResult(
				assessmentTest, resultHeader, datasource,
				score, result, recommend, reportResultId, this.getStringDetail()
		);
		/*
		if (dataSourceHashKey == null) { // retrieve the datasource fields that are used to calculate the hash key
			// will be used to save the datasource data (host, instance and port) in the summary record.
			try {	
				dataSourceHashKey = GuardParametersPeer.getStringParameter("HASH_DS_KEY_FOR_VA_SUMMARY", "S");
			}
			catch(Exception e) {} // Do nothing
		}
		*/
		
		if (dataSourceHashKey != null && !dataSourceHashKey.trim().equals("")) {
			updateVaSummary(datasource, score, assessmentTest.getTestId(), assessmentTest);

			// Add Hash Key to Test Result (for reporting)
			try {
				String hashKey = null;
				hashKey = datasource.getHashKey();
				if (hashKey == null) { // Not set (probably first test for this datasource object)
					// Calculate the Hash Key
					hashKey = calculateDatasourceHashKey(datasource);
					// Set for datasource
					datasource.setHashKey(hashKey);
				}

				if (hashKey != null) {
					testResult.setDataSourceHash(hashKey);
					testResult.save();
				}
			} catch (Exception e) {
				AdHocLogger.logException(e);
			}
		}
		
		updateAdditionalInfo(testResult);
		this.testResult = testResult;
		return this.getTestResult();
	}

	/**
	 * @param testScore
	 * @return The recommendation for this test based on the score
	 */
	protected String findRecommendation(TestScore testScore) {
		AvailableTest availableTest = this.getAvailableTest();
		String recommend = RecommendationTextPeer.findRecommendation(availableTest, testScore);
		return recommend;
	}

	/**
	 * @return The result of this test.  Null if the test has not yet been executed.
	 */
	public TestResult getTestResult() {
		return this.testResult;
	}
    /*
	protected double getTotalRequest(String assessmentFromDate, String assessmentToDate, String filterClientIP,String filterServerIP) {
		double ret = 0;
		try {
			ReportHeader rh = ReportHeaderPeer.retrieveByPK(Constants.TOTAL_REQUESTS_REPORT_ID);
			if (rh != null) {
				ReportGenerator rg = new ReportGenerator(rh, true);
				rg.setConnectionName("gdm_persist");
				this.setRepParams(rh,assessmentFromDate, assessmentToDate, filterClientIP, filterServerIP);
				rg.setFetchSize(1000);
				ReportResultHeader r = rg.getFullResult(10000, null);
				ret = r.getOverallValue();
			}
		}
		catch (Exception ex) {
			// swallow the exception
		}
		return ret;
	}
	*/
	
	/**
	 * Checks that either the message or the details mentions the test name and datasource.
	 * @param msg
	 * @param detail
	 * @return The details string for logging
	 */
	protected String assembleDetails(String msg, String detail) {
		StringBuilder buf = new StringBuilder();
		String value;

		// check if we have identified the test
		value = this.getAvailableTest().getTestDesc();
		if ( !Check.contains(msg, value) && !Check.contains(detail, value) ) {
			value = Say.what(
					Say.ASMT_MSG_LOG_TEST_NAME,
					Say.ASMT_SUB_TEST, this.getAvailableTest().getDatasourceTypeName() + Say.SP + value
			);
			buf.append(value);
		}

		// add in the detail
		Stringer.newLn(buf).append(detail);
		return buf.toString();
	}

	/**
	 * Convenience method logs an error to the assessment log.
	 * If the logging fails, a message is written to the GDM_EXCEPTIONS.
	 * @param msg The message for the error.
	 */
	protected void logError(String msg) {
		String dtl = "";
		this.logError(msg, dtl);
	}

	/**
	 * Convenience method logs an error to the assessment log.
	 * If the logging fails, a message is written to the GDM_EXCEPTIONS.
	 * @param msg The message for the error.
	 * @param detail The details of the error.
	 * @author dtoland on Apr 17, 2007 at 1:21:27 PM
	 */
	protected void logError(String msg, String detail) {
		String dtl = this.assembleDetails(msg, detail);
		AssessmentResultHeader hdr = this.getResultHeader();
		// TODO - later
		//AssessmentLogPeer.logTestError(hdr, msg, dtl);
	}

	/**
	 * Convenience method logs a warning to the assessment log.
	 * If the logging fails, a message is written to the GDM_EXCEPTIONS.
	 * @param msg The message for the error.
	 */
	protected void logWarn(String msg) {
		String dtl = "";
		this.logError(msg, dtl);
	}

	/**
	 * Convenience method logs a warning to the assessment log.
	 * If the logging fails, a message is written to the GDM_EXCEPTIONS.
	 * @param msg The message for the error.
	 * @param detail The details of the error.
	 * @author dtoland on Apr 17, 2007 at 1:21:27 PM
	 */
	protected void logWarn(String msg, String detail) {
		String dtl = this.assembleDetails(msg, detail);
		AssessmentResultHeader hdr = this.getResultHeader();
		// TODO -later
		//AssessmentLogPeer.logWarn(hdr, msg, dtl);
	}
	/* TODO - later
	private void setRepParams(ReportHeader rh, String fromDate, String toDate, String filterClientIP, String filterServerIP) {
		List repParams = rh.getParameters();
		String rClientIP = filterClientIP.replace('*','%');
		String rServerIP = filterServerIP.replace('*','%');
		if (rClientIP.trim().equals(""))
			rClientIP = "%";
		if (rServerIP.trim().equals(""))
			rServerIP = "%";
		for (int i=0 ; i < repParams.size() ; i++) {
			RunTimeParameter par = (RunTimeParameter)repParams.get(i);
			if ( par.getParameterName().equalsIgnoreCase("QUERY_FROM_DATE") )
				par.setRunTimeValue(fromDate);
			if ( par.getParameterName().equalsIgnoreCase("QUERY_TO_DATE") )
				par.setRunTimeValue(toDate);
			if ( par.getParameterName().equalsIgnoreCase("CLIENT_IP_FILTER") )
				par.setRunTimeValue(rClientIP);
			if ( par.getParameterName().equalsIgnoreCase("SERVER_IP_FILTER") )
				par.setRunTimeValue(rServerIP);
		}
	}
	*/
	
	private static String chkIp(String inpIP) {
		String outIP = "";
		if (inpIP.indexOf("0") == -1 )
			return inpIP;
		StringTokenizer t = new StringTokenizer(inpIP,".");
		while (t.hasMoreTokens()) {
			String s = t.nextToken();
			if (s.equals("0")) s = "%";
			if (outIP.equals(""))
				outIP = s ;
			else outIP = outIP + "." + s;
		}
		return outIP;
	}

	/**
	 * @return the assessmentTest
	 */
	protected AssessmentTest getAssessmentTest() {
		return this.assessmentTest;
	}

	/**
	 * @return the assessmentTest.  Null if a TorqueException occurs when retrieving.
	 */
	protected AvailableTest getAvailableTest() {
		AvailableTest result = null;
		result = this.getAssessmentTest().getAvailableTest();
		return result;
	}


	/**
	 * Synchronizes the modified flag with the assessment test.
	 * @param resultHeader The result header.
	 */
	protected void setResultHeader(AssessmentResultHeader resultHeader) {
		this.resultHeader = resultHeader;
		// synchronize the modified flags
		if ( this.assessmentTest != null && this.assessmentTest.getModifiedFlag() ) {
			resultHeader.setParameterModifiedFlag(true);
		}
	}

	/**
	 * @return the resultHeader
	 */
	protected AssessmentResultHeader getResultHeader() {
		return this.resultHeader;
	}

	/**
	 * @return the testId
	 */
	protected int getTestId() {
		return this.testId;
	}

	/**
	 * Sets the assessment test and the test id fields.
	 * Also synchronizes the modified flag with the result header.
	 * @param assessmentTest
	 */
	protected void setAssessmentTest(AssessmentTest assessmentTest){
		this.testId = assessmentTest.getTestId();
		this.assessmentTest = assessmentTest;

		// synchronize the modified flags
		if ( this.resultHeader != null && assessmentTest.getModifiedFlag() ) {
			this.resultHeader.setParameterModifiedFlag(true);
		}
	}

	/**
	 * @return The CAS template id for this test.
	 */

	protected long getCasId() {
		long casId = this.getAvailableTest().getAuditConfigTemplateId();
		return casId;
	}

    
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf( this.getAvailableTest() );
	}

	public List<String> getDetail() {
		return detail;
	}

	public String getStringDetail() {
		if (this.detailStr == null) 
			this.detailStr = " ";
		if (getDetail() != null && !getDetail().isEmpty()) {
			try {
					boolean inFirstRec= true;
					String val = "";
					List<String> l = getDetail();
					for (int i=0; i< l.size(); i++) {
						val = l.get(i);
						if (inFirstRec) {
							inFirstRec = false;
							this.detailStr = "\n" + this.detailStr;
						}
						else
							this.detailStr = this.detailStr.concat("\n");
						this.detailStr = this.detailStr.concat(val);
					}
				
			}
			catch(Exception e) {
				// LOG.error(e);
				this.detailStr = "N/A";
			}
		}
		if (this.detailStr == null || this.detailStr.trim().equals(""))
			this.detailStr = "N/A";
		return this.detailStr;
	}
	
	public void setStringDetail(String detail) {
		this.detailStr = detail;
	}
	
	public void setDetail(List<String> detail) {
		this.detail = detail;
	}

	public List<String> fetchDetail() throws Exception {
		List<String> l = new ArrayList<String>();
		return l;
	}
	/*
	public List<String> checkExceptions (List<String> inDetails, int groupId) throws GuardAssessmentException {
		List<String> outDetails = new ArrayList<String>();
		outDetails.addAll(inDetails);
		removedDetailCount = 0;
		try {
			List<GroupMember> m = GroupMemberPeer.doSelectOnGrpId(groupId);
			boolean isTuple = false;
			GroupDesc g = null;
			try {
				 g = GroupDescPeer.retrieveByPK(groupId);
			}
			catch (TorqueException t) {
					String msg = "Error Checking Exceptions " + assessmentTest + "Exceptions Group Not Found";
					LOG.error(msg, t);
			}
			if ( g == null ) { // Exceptions Group Not Found
				outDetails.add(Say.what(Say.ASMT_EXCEPTION_GROUP_NOT_FOUND));
				return outDetails;
			}
			if (g.getGroupType().getTupleFlag()> 1)
				isTuple = true;
			boolean match = false;
			for (int i = 0; i<inDetails.size(); i++) {
				String detail = inDetails.get(i);
				match = false;
				for (int j = 0 ; j<m.size() && !match ; j++) {
					String pattern = m.get(j).getGroupMember();
					String detailTotest = detail;
					if (isTuple) {
						pattern = pattern.replaceAll("\\+","");
						detailTotest  = detail.replaceAll("\\+","");
					}
					if (pattern.startsWith("(R)")) {
						if (  Utils.jRegExSimpleCheck(pattern.substring(3), detailTotest, "1") ) {
							match=true;
							outDetails.remove(detail);
							removedDetailCount++;
						}
					}
					else {
						if ( detailTotest.replaceAll(" ", "").equals(pattern.replaceAll(" ", "")) ) {
							match=true;
							outDetails.remove(detail);
							removedDetailCount++;
						}
					}
				}
			}
		}
		catch (Exception e) {
			String msg = "Error Checking Exceptions " + assessmentTest ;
			LOG.error(msg, e);
			throw new GuardAssessmentException(msg, e);
		}
		return outDetails;
	}
	*/
	
	/**
	 * @param connection The connection to re-use for the execution of the test.
	 */
	protected void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	
	protected void setMConnection(MongoClient mconnection) {
		this.mconnection = mconnection;
	}
	
	public void updateVaSummary(Datasource datasource, TestScore score, int testId, AssessmentTest assessmentTest) {
		// Get the Datasource Hash key (from the datasource record) we save it in the datasource so 
		// it is calculated once per datasource for all tests.
		if (datasource == null) 
			return;
		String dataSourceHash = datasource.getHashKey();
		boolean newRec = false;
		//use current score PASS / FAIL or Error.
		// Note summary will not distinguish between different types of error score.
		int currentScore;
		if (score == TestScore.FAIL || score == TestScore.PASS)
			currentScore = score.getScoreValue();
		else
			return; // We do not keep statistics for Error (same as if the test never run (Per design).
		
		if (dataSourceHash == null) { // Not set (probably first test for this datasource obbject)
			// Calculate the Hash Key
			dataSourceHash = calculateDatasourceHashKey(datasource);
			// Set for datasource
			datasource.setHashKey(dataSourceHash);
		}
		// If hash key still null (something bad happened (and it is recorded to the LOG)
		if (dataSourceHash == null) {
			// LOG.error("Unable To create Key For Datasource: " + datasource.getName() + " Va Summary Not updated");
			return;				
		}
		// Get (or create) new VA Summary record for Hash key and test ID 
		VaSummary sumRec = null;
		//try {
			sumRec = VaSummaryPeer.getSummary(dataSourceHash, testId);
		/*
		}
		catch(TorqueException e) {
			LOG.error("Unable To Retrieve VA Summary Record for Datasource: " + datasource.getName() + " and Test ID: " + testId);
			return;							
		}
		*/
		
		Date now = new Date();
		dataSourceHashKey = "HIP";
		/*
		if (dataSourceHashKey == null) { // retrieve the datasource fields that are used to calculate the hash key
										 // will be used to save the datasource data (host, instance and port) in the summary record.
			try {	
				dataSourceHashKey = GuardParametersPeer.getStringParameter("HASH_DS_KEY_FOR_VA_SUMMARY", "S");
			}
			catch(Exception e) {} // Do nothing
		}
		*/
		
		if (dataSourceHashKey == null || dataSourceHashKey.trim().equals("")) {
			// LOG.error("Unable To Retrieve Hash Key from admin console params");
			return;							
		}
		if (sumRec == null) {
			newRec = true;
			// New record (First occurrence of test ID / datasource Hash key
			sumRec = new VaSummary();
			sumRec.setDataSourceHash(dataSourceHash);
			sumRec.setTestId(testId);
			
			// For host, instance and port if not used in the key then we do not populate to avoid
			// inconsistencies (for example host not used, only instance, and same instance running on two hosts should use the same record.
			if (dataSourceHashKey.toUpperCase().contains("H"))
				sumRec.setDbHost(datasource.getHost());
			if (dataSourceHashKey.toUpperCase().contains("I"))
				sumRec.setServiceName(datasource.getServiceName());
			if (dataSourceHashKey.toUpperCase().contains("P"))			
				sumRec.setDbPort(datasource.getPort());
			if (dataSourceHashKey.toUpperCase().contains("N"))                       
				sumRec.setDatasourceName(datasource.getName()); 
			sumRec.setFirstExecution(now);
			String dbType = null;
			try { 
				dbType = datasource.getDatasourceType().getName();
			}
			catch( Exception e) {
				// LOG.error("Unable To get DB Type for Datasource: " + datasource.getName() );
			}
			if (dbType != null)
				sumRec.setDbType(dbType);
			
			String testDesc = null;
			try {
				testDesc = assessmentTest.getAvailableTest().getTestDesc();
			}
			catch( Exception e) {
				// LOG.error("Unable To get Test Description for Test  " + assessmentTest.getTestId() );
			}
			if (testDesc != null) {
				sumRec.setTestDesc(testDesc);
			}
		}
		
		// Get values from previous run
		int previousScore = sumRec.getCurrentScore(); // If previousScore = -999 Record recently saved (no actual current score)
		int previousCumulativeFail = sumRec.getCumulativeFailAge();
		int previousCumulativePass = sumRec.getCumulativePassAge();
		Date previousExecution = sumRec.getLastExecution();
		 
		// Set new values to summary record:
		sumRec.setLastExecution(now);
		if (currentScore != TestScore.ERROR.getScoreValue()) {
			sumRec.setCurrentScore(currentScore);
			if (currentScore != previousScore)
			sumRec.setCurrentScoreSince(now);
		}
		if (currentScore == TestScore.PASS.getScoreValue()) {
			if (sumRec.getFirstPass() == null)
				sumRec.setFirstPass(now);
			sumRec.setLastPass(now);
		}
		else if (currentScore == TestScore.FAIL.getScoreValue()) {
			if (sumRec.getFirstFail() == null)
				sumRec.setFirstFail(now);
			sumRec.setLastFail(now);
		}		
		// Set cumulative Values:
		if (newRec) {
			if (currentScore == TestScore.PASS.getScoreValue()) { // Pass
				sumRec.setCumulativePassAge(1);
				sumRec.setCumulativeFailAge(0);
			}
			else { // Fail
				sumRec.setCumulativePassAge(0);
				sumRec.setCumulativeFailAge(1);				
			}
		}
		else { // Existing Record (need to Add days to cumulative pass and fail:
			long ldays = 0;
			int days = 0;
			try {
				SimpleDateFormat df =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String previousExecStr = df.format(previousExecution);
				//ldays = JdbcUtils.executeSqlForNumber("Select TO_DAYS(NOW()) - TO_DAYS('" + previousExecStr + "')");
				days = ((Long)ldays).intValue();
				
			}
			// days = number of days since last test.
			catch(Exception e) {
				// LOG.error("Unable To Calculate number of days since last VA for Datasource: " + datasource.getName() + " and Test ID: " + testId);
				return;										
			}
			
			if (previousScore == TestScore.PASS.getScoreValue()) { // Previously (last) Passed
				sumRec.setCumulativePassAge(previousCumulativePass + days); // Since the last time until NOW was in Pass
				if (currentScore == TestScore.FAIL.getScoreValue()) 
					sumRec.setCumulativeFailAge(previousCumulativeFail + 1); // Only Today in Fail
			}
			else { // Previously (last) Failed
				sumRec.setCumulativeFailAge(previousCumulativeFail + days); // Since the last time until NOW was in Fail
				if (currentScore == TestScore.PASS.getScoreValue()) 
					sumRec.setCumulativePassAge(previousCumulativePass + 1); // Only Today in Pass
			}
		}
		
		try {
			sumRec.save();
		}
		catch (Exception e) {
			// LOG.error("Unable To Save VA Summary Record for Datasource: " + datasource.getName() + " and Test ID: " + testId);
			return;										
		}
	}
	
	private String calculateDatasourceHashKey(Datasource datasource) {
		String retKey = null;
		dataSourceHashKey = "HIP";
		/*
		AdminconsoleParameter ap = AdminconsoleParameterPeer.getActiveConfiguration();
		try {
			dataSourceHashKey = GuardParametersPeer.getStringParameter("HASH_DS_KEY_FOR_VA_SUMMARY", "S");
		}
		catch(Exception e) {} //Do nothing
		*/
		
		if (dataSourceHashKey == null || dataSourceHashKey.trim().equals("")) {
			// LOG.error("Unable To Retrieve Hash Key from admin console params");
			return null;							
		}
		String sToHash = null;
		if (dataSourceHashKey.toUpperCase().contains("I")) {
			if (datasource.getServiceName() != null && !datasource.getServiceName().trim().equals("")) 
				sToHash = datasource.getServiceName();
			else {
				// LOG.error("Can not create Key For Datasource: " + datasource.getName() + " No Service or Instance Name defined.");
				return null;
			}
		}
		if (dataSourceHashKey.toUpperCase().contains("H")) {
			if (datasource.getHost() != null && !datasource.getHost().trim().equals(""))  
				sToHash = sToHash + "+" + datasource.getHost();
			else {
				// LOG.error("Can not create Key For Datasource: " + datasource.getName() + " Host not defined.");
				return null;				
			}
		}		 
		if (dataSourceHashKey.toUpperCase().contains("P")) {
			if (datasource.getPort() != 0)  
				sToHash = sToHash +  "+" + datasource.getPort();
			else {
				// LOG.error("Can not create Key For Datasource: " + datasource.getName() + " Port not defined.");
				return null;				
			}
		}
		if (dataSourceHashKey.toUpperCase().contains("N")) { 
			if (datasource.getName() != null && !datasource.getName().trim().equals(""))   
				sToHash = sToHash +  "+" + datasource.getName(); 
			else { 
				// LOG.error("Can not create Key For Datasource: " + datasource.getName() + " DataSource Name not defined."); 
				return null;                             
			} 
		}                
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] bytes = md.digest( sToHash.getBytes() );
			retKey = Utils.bytesToHexaString(bytes);
		}
		catch(Exception e) {
			// LOG.error("Can not create Key For Datasource: " + datasource.getName() + " Port not defined.");
			return null;							
		}
		return retKey;
	}
		
	private void updateAdditionalInfo(TestResult result) {
		long testResultId = -1;
		try {
			AvailableTest aTest =  AvailableTestPeer.retrieveByPK(testId);
			testResultId = result.getTestResultId();
			int testId = result.getTestId();
			if (aTest.isQueryBasedTest()) {
				SqlbasedAssessmentDefinition sb = SqlbasedAssessmentDefinitionPeer.findByTestId(testId);
				if (sb != null) {
					result.setSqlStmtSent(sb.getSqlStmt());
					result.save();
				}
			}

			// TODO - later CVE test
			/*
			if (aTest.isCveTest() ) {
				// Copy CVSS_INFO to RESULT_CVSS_INFO
				CvssInfo c = CvssInfoPeer.getCvssInfoByTestId(testId);
				if (c != null) {
					ResultCvssInfo resCvssInfo = new ResultCvssInfo();
					resCvssInfo.setTestResultId(testResultId);
					resCvssInfo.setCvssConfidentialityImpact(c.getCvssConfidentialityImpact());
					resCvssInfo.setCvssScore(c.getCvssScore());
					resCvssInfo.setCvssAuthentication(c.getCvssAuthentication());
					resCvssInfo.setCvssAccessVector(c.getCvssAccessVector());
					resCvssInfo.setCvssSource(c.getCvssSource());
					resCvssInfo.setCvssIntegrityImpact(c.getCvssIntegrityImpact());
					resCvssInfo.setCvssAvailabilityImpact(c.getCvssAvailabilityImpact());
					resCvssInfo.setCvssGeneratedOnDatetime(c.getCvssGeneratedOnDatetime());
					resCvssInfo.setCvssAccessComplexity(c.getCvssAccessComplexity());
					resCvssInfo.save();
				}
				// Copy CVSS_INFO to RESULT_CVSS_INFO
				List<CveReference> cverefs = CveReferencePeer.getCveReferencesByTestId(testId);
				if (cverefs != null && cverefs.size() > 0) {
					for (CveReference ref : cverefs) {
						ResultCveReference resCveRef = new ResultCveReference();
						resCveRef.setTestResultId(testResultId);
						resCveRef.setCveReferenceHref(ref.getCveReferenceHref());
						resCveRef.setCveReferenceSource(ref.getCveReferenceSource());
						resCveRef.setCveReferenceType(ref.getCveReferenceType());
						resCveRef.save();
					}
				}
			} // End Of If CVE TEST
			*/
		}
		catch(Exception e) {
			// LOG.error("Failed to Update Additional Information for Test Result, Result id: " + testResultId + "  " + e.toString());										
		}
	}

	
	
}
