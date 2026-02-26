/*
* �� Copyright 2002-2008, Guardium, Inc.  All rights reserved.  This material
* may not be copied, modified, altered, published, distributed, or otherwise
* displayed without the express written consent of Guardium, Inc.
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

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
//import org.apache.torque.TorqueException;

import com.guardium.assessment.i18n.Say;
import com.guardium.assessment.tests.GuardAssessmentException;
import com.guardium.assessment.tests.TestScore;
//import com.guardium.classifier.InitializationException;
//import com.guardium.classifier.ProcessLogger;
//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
import com.guardium.map.AssessmentLogMap;

//import com.guardium.data.AssessmentDatasource;
import com.guardium.data.AssessmentResultDatasource;
import com.guardium.data.AssessmentLog;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;

import com.guardium.map.AssessmentResultHeaderMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.RecommendationTextMap;
import com.guardium.map.SecurityAssessmentMap;
import com.guardium.data.SecurityAssessment;
//import com.guardium.map.SecurityAssessmentMap;
import com.guardium.data.TestResult;
import com.guardium.map.TestResultMap;
//import com.guardium.datamodel.classifier.ClsProcessRun;
//import com.guardium.datamodel.classifier.ClsProcessRunCredential;
//import com.guardium.datamodel.classifier.ClsProcessRunCredentialPeer;
//import com.guardium.datamodel.classifier.ClsProcessRunPeer;

import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DataSourceConnectException;

//import com.guardium.datamodel.logger.GdmAccessPeer;
import com.guardium.utils.GuardRepGeneralException;

import com.guardium.utils.RepDateUtils;

import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
//import com.teradata.jdbc.jdbc_4.logging.Log;
//import com.guardium.utils.SSLUtil;
//import com.mongodb.MongoClient;

/**
 * @author dario
 * This Class contains all the main methods to invoke and run an
 * an assessment test.
 * The main method: execute loops on the tests defined for the assessment,
 * instantiate the actual test class and executes each test, accumulates
 * the score and saves the result of the assessment.
 */
public class AssessmentRunner {
	/** Local static logger for class */
	//private static final transient Logger LOG = Logger.getLogger(AssessmentRunner.class);

	private SecurityAssessment securityAssessment = null;
	private final int taskId = -1;
	
	private boolean isRunCanceled = false;

	//private final ClsProcessRun clsProcessRun;
	private List <Datasource> datasources = null;
	private AssessmentResultHeader assessmentResultHeader = null;
	private boolean paramRetrievalFailed = false;
	//private final AssessmentStatistics statistics = new AssessmentStatistics();
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	
	TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
	SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
	
	//private List <AssessmentLog> assessmentLogList = null;
	
	//private List <AssessmentResultHeader> assessmentResultHeaderList = null;
	
	/**
	 * Initializing Constructor
	 * @param clsProcessRun
	 */
	public AssessmentRunner() {

	}


	/**
	 * @return The  Security Assessment for this run.
	 */
	public SecurityAssessment getSecurityAssessment() {
		/*
		if (this.securityAssessment == null) {
			this.securityAssessment = SecurityAssessmentMap.getSecurityAssessmentById(id);
		} */
		return this.securityAssessment;
	}

	public void setSecurityAssessment (SecurityAssessment v) {
		/*
		if (this.securityAssessment == null) {
			this.securityAssessment = SecurityAssessmentMap.getSecurityAssessmentById(id);
		} */
		this.securityAssessment = v;
		return;
	}
	
	/**
	 * @param assessmentTest
	 * @param resultHeader
	 * @return The test score
	 */
	protected TestScore executeTest(AssessmentTest assessmentTest, AssessmentResultHeader resultHeader) {
		Datasource datasource = null;
		return this.executeTest(assessmentTest, resultHeader, datasource);
	}

	protected TestScore executeTest(AssessmentTest assessmentTest,
			AssessmentResultHeader resultHeader, Datasource datasource) 
	{
		// TODO Auto-generated method stub
		return this.executeTest(assessmentTest, resultHeader, datasource, null, null);
	}

	

	/**
	 * @param assessmentTest
	 * @param resultHeader
	 * @param datasource
	 * @param con
	 * @return The test score
	 */
	protected TestScore executeTest(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader,
			Datasource datasource, Connection con, Map cachedParams
	) {

		TestScore score;
		GenericTest genericTest = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		//System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
        String startTime = dateFormat.format(date);
        
		try {

			// get the parameters
			String fromDate = this.getSecurityAssessment().getFromDate();
			String toDate = this.getSecurityAssessment().getToDate();
			String clientFilter = this.getSecurityAssessment().getFilterClientIp();
			String serverFilter = this.getSecurityAssessment().getFilterServerIp();
			double timeFactor = 0;	// this.getTimeFactor();

			genericTest = GenericTest.getTest(assessmentTest, resultHeader, datasource);
			if (assessmentTest.isDeprecated()) {
				return genericTest.executeTest(fromDate, toDate, clientFilter, serverFilter, timeFactor);
			}
			/*
			if (datasource != null) {
				// LOG.warn("Executing test: '" + assessmentTest + "' on: '" + datasource + "'.");
			} else {
				// LOG.warn("Executing test: '" + assessmentTest + "'.");
			}
			*/
			
			GenericTest test = (GenericTest) genericTest;
			
			if (datasource.isSqltype()) {
				test.setConnection(con);
			}

			test.setCachedParameterMap(cachedParams);
			//execute the test
			// LOG.warn("Executing test: '" + assessmentTest + "' on: '" + datasource + "'.");
			score = genericTest.executeTest(fromDate, toDate, clientFilter, serverFilter, timeFactor);
			if(datasource.isDB2_ZOS() && score == score.ERROR && genericTest.configrRetreivalFailed)
				paramRetrievalFailed = true;
				
				

		} catch (Throwable t) {
			// LOG.error("Unexpected Exception: '" + genericTest + "'.", t);
			score = handleUnexpectedTestFailure(assessmentTest, resultHeader, datasource, t);
		}
		
		date = new Date();
		//System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
        String endTime = dateFormat.format(date);
        
		return score;
	}

	/**
	 * Handles when an exception occurs during the instantiation of the test and the test cannot
	 * report its own problem.
	 * @param assessmentTest
	 * @param resultHeader
	 * @param datasource
	 * @param throwable
	 * @return
	 */
	private static TestScore handleUnexpectedTestFailure(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, Datasource datasource,
			Throwable throwable
	) {
		TestScore score = TestScore.ERROR;
		String result;
		if (assessmentTest != null && !assessmentTest.usesDatasource() ) {
			result = Say.what(
					Say.ASMT_MSG_LOG_ERROR_TEST_NO_DS,
					Say.ASMT_SUB_TEST, String.valueOf(assessmentTest)
			);

		} else {
			result = Say.what(
					Say.ASMT_MSG_LOG_ERROR_TEST,
					Say.ASMT_SUB_TEST, String.valueOf(assessmentTest),
					Say.ASMT_SUB_DS, String.valueOf(datasource)
			);
		}
		
		// TODO - later
		/*
		AssessmentLogPeer.logTestError(resultHeader, assessmentTest, throwable);
		TestResultPeer.recordResult(
				assessmentTest, resultHeader, datasource, score, result, throwable
		);
		*/
		
		return score;
	}

	/**
	 * Logs a general message for the datasource exception.
	 * @param datasource
	 * @param dsce The DataSourceConnectException.
	 * @param tests The list of tests that will not be run.
	 */
	protected void logConnectionException(
			Datasource datasource, DataSourceConnectException dsce, List<AssessmentTest> tests
	) {
		String msg = dsce.getLocalizedMessage();
		String dtl =  Say.what(Say.ASMT_MSG_ERROR_NO_CONNECT);
		
		String cause = "";
		if ( dsce.getCause() != null) {
			cause = Say.SP + Say.NL + Informer.causality( dsce.getCause() );
		}

		// TODO - later
		/*
		// log the condition
		try {
			AssessmentLogPeer.logTestError( this.getAssessmentResultHeader(), msg, dtl + cause);
		} catch (GuardAssessmentException e) {
			AdHocLogger.logException(e);
		}
		*/
	}

	/**
	 * Loops through the assessment tests for an assessment.
	 * @return The Assessment Result Header.
	 * @throws GuardAssessmentException
	 */
	public void run() throws GuardAssessmentException {

		AssessmentResultHeader resultHeader = null;
		SecurityAssessment assessment = null;
		try {
			
			// Create the result header and add a reference to the job
			resultHeader = new AssessmentResultHeader(); 	//this.getAssessmentResultHeader();

			// log the start of the job
			//AssessmentLogPeer.logStart(resultHeader);

			// get the list of tests
			assessment = this.getSecurityAssessment();
			List<AssessmentTest> tests = assessment.getAssessmentTests();
			List<Datasource> datasources = assessment.getDatasources();
			
			/*
			if (!AdminconsoleParameterPeer.checkMeter(this.getDatasources().size(), true)) {
				int m = AdminconsoleParameterPeer.getMeter();
				String message = "Can Not Execute Security Assessment " + this.getSecurityAssessment().getAssessmentDesc() + " Process has " + 
				this.getDatasources().size() + " Data sources while the System is Licensed for " + m + " Data Sources."; 
				GuardAssessmentException e = new GuardAssessmentException(message);
				ProcessLogger.logGdmException(message, e);
				throw e;
			}
			*/
			
			// get the Datasources and loop through the datasource tests
			//List<Datasource> datasources = this.getDatasources();
			for ( Datasource datasource : datasources ) {
				if(this.isRunCanceled) return; //run has been canceled by UI
				// LOG.warn("Assessing Datasource: '" + datasource + "'.");

				// loop through the jdbc tests
				this.loopJdbcTests(resultHeader, tests, datasource);
				
				if(this.isRunCanceled) return; //run has been canceled by UI
				// loop through the CVE tests
				this.loopCveTests(resultHeader, tests, datasource);				
			}
		

		} catch (GuardAssessmentException e) {
			AssessmentLogPeer.logFatal(assessment, resultHeader, e);
			throw e;

		} catch (GuardAssessmentCancelation e) {
			// LOG.warn("Assessment was cancelled: '" + assessment + "'.", e);

		} catch (Throwable t) {
			// should not occur - if torque is functioning
			String msg = "Unexpected error running the Assessment tests: '" + assessment + "'.";
			// LOG.error(msg, t);
			AssessmentLogPeer.logFatal(assessment, resultHeader, t);
			throw new GuardAssessmentException(msg, t);

		} finally {

			// log the results
			// TODO -later
			// String details = this.statistics.getSummary();
			String details = "this is atest";
			AssessmentLogPeer.logComplete(resultHeader, details);
		}

		return;
	}
	
	public void doCancel(){
		this.isRunCanceled = true;
	}

	


	/**
	 * Loop through the jdbc tests for a datasource.
	 * @param resultHeader
	 * @param tests
	 * @param datasource
	 * @throws GuardAssessmentException
	 * @throws GuardAssessmentCancelation
	 */
	private void loopJdbcTests(
			AssessmentResultHeader resultHeader, List<AssessmentTest> tests, Datasource datasource
	) throws GuardAssessmentException, GuardAssessmentCancelation {

		// LOG.warn("Looping through JDBC Tests");
		int concount = 0;
		int count = 0;
		Connection con = null;
		int testCount = 0;
		
		DataSourceConnectException dsce = null;
		Map cachedParams = null;
		paramRetrievalFailed = false;
		try {

			// loop through the JDBC tests and pass the connection to each
			for (AssessmentTest test : tests) {
				
				if(this.isRunCanceled) return; //run has been canceled by UI
				
				
				// only jdbc tests
				if ( !test.isJdbcTest() && !test.isQueryBasedTest()) {
					continue;
				}

				// check for a matching datasource type
				if ( !this.isTypeMatch(test, datasource) ) { continue; }

				/*
				if ( LOG.isInfoEnabled() ) {
					LOG.info("Evaluating Test: " +test);
				}
				*/
				
				if(cachedParams == null)
					cachedParams = new HashMap();
				// Check the connection, then run the test
				TestScore score;
				if(paramRetrievalFailed)
				{
					score = TestScore.ERROR;
					String result = "Failed to retrieve system parameters - see jobqueue.log for details.";
					// TODO - later
					/*
					AssessmentLogPeer.logTestError(resultHeader, test, datasource);
					TestResultPeer.recordResult(test, resultHeader, datasource, score, result, dsce);
					resultHeader.updateStatsByType(test, score);
					*/ 
				}
				else
				{
					if (datasource.isSqltype()) {
						if ( Check.isEmpty(con) ) 
						{
							if (dsce != null) {
								// already got a connect exception, just record an error
								score = this.recordConnectionFailureResult(test, resultHeader, datasource, dsce);	
							} 
							else 
							{
								// Get the connection here because a test may close it out from under us.
								try {
									if (count > 1) {
										recordReconnection(resultHeader, test, datasource, count);
									}
									con = datasource.getConnection();
									count++;
									score = this.executeTest(test, resultHeader, datasource,  con, cachedParams);
								} catch (DataSourceConnectException e) {
									// preserve the exception for logging in the individual tests.
									// LOG.warn("exception " + e.getMessage());
									dsce = e;
									this.logConnectionException(datasource, dsce, tests);
									score = this.recordConnectionFailureResult(test, resultHeader, datasource, dsce);
								}
							}
	
						} 
						else 
						{
							// connection is good to go, run the test
							score = this.executeTest(test, resultHeader, datasource,  con, cachedParams);
						}
					}
					
				}
				testCount++;
				System.out.println("Harden Test " + testCount + " - ID " + test.getTestId() + " completed");
				// TODO - later
				/*
				// clean up for the datasource and get out
				this.updateResultDatasourceVersion(resultHeader, datasource, con, mcon);
				this.updateStatistics(resultHeader, score, datasource);
				resultHeader.updateStatsByType(test, score);
				*/
			}

		} finally {
			// disconnect
			// LOG.error("jdbc 2 concount="+concount);
			con = Check.disposal(con);
		}
	}

	
	
	
	/**
	 * Loop through the jdbc tests for a datasource.
	 * @param resultHeader
	 * @param tests
	 * @param datasource
	 * @throws GuardAssessmentException
	 * @throws GuardAssessmentCancelation
	 */
	private void loopCveTests(
			AssessmentResultHeader resultHeader, List<AssessmentTest> tests, Datasource datasource
	) throws GuardAssessmentException, GuardAssessmentCancelation {

		// LOG.info("Looping through CVE Tests");
		int count = 0;
		Connection con = null;
		int testCount = 0;
		DataSourceConnectException dsce = null;
		try {

			// loop through the JDBC tests and pass the connection to each
			for (AssessmentTest test : tests) {
				if(this.isRunCanceled) return; //run has been canceled by UI
				
				// only jdbc tests
				if ( !test.isCveTest()) {
					continue;
				}

				// check for a matching datasource type
				if ( !this.isTypeMatch(test, datasource) ) { continue; }

				/*
				if ( LOG.isInfoEnabled() ) {
					LOG.info("Evaluating Test: " +test);
				}
				*/
				
				// Check the connection, then run the test
				TestScore score;
				if (datasource.isSqltype()) {
					if ( Check.isEmpty(con) ) {
						if (dsce != null) {
							// already got a connect exception, just record an error
							score = this.recordConnectionFailureResult(test, resultHeader, datasource, dsce);

						} else {
							// Get the connection here because a test may close it out from under us.
							try {
								if (count > 1) {
									recordReconnection(resultHeader, test, datasource, count);
								}
								con = datasource.getConnection();
								count++;
								score = this.executeTest(test, resultHeader, datasource);

							} catch (DataSourceConnectException e) {
								// preserve the exception for logging in the individual tests.
								dsce = e;
								this.logConnectionException(datasource, dsce, tests);
								score = this.recordConnectionFailureResult(test, resultHeader, datasource, dsce);
							}
						}

					} else {
						// connection is good to go, run the test
						score = this.executeTest(test, resultHeader, datasource);
					}
				}
				testCount++;
				System.out.println("CVE Test " + testCount + " - ID " + test.getTestId()+ " completed");
				
				// TODO - later
				/*
				// clean up for the datasource and get out
				this.updateResultDatasourceVersion(resultHeader, datasource, con, mcon);
				this.updateStatistics(resultHeader, score, datasource);
				resultHeader.updateStatsByType(test, score);
				*/
				
			}

		} finally {
			// disconnect
			con = Check.disposal(con);
		}
	}
	

	/**
	 * updates the Assessment Result Datasource version information.
	 * @param resultHeader
	 * @param datasource
	 * @param con
	 */
	protected void updateResultDatasourceVersion(
			AssessmentResultHeader resultHeader, Datasource datasource, Connection con	) {
		// update the version information
		try {
			// TODO -later 
			//AssessmentResultDatasource resultDs = resultHeader.findAssessmentResultDatasource(datasource);
			//resultDs.updateVersion(datasource, con);
			//resultDs.save();

		} catch (Exception e) {
			// Should not happen if torque is functional
			String msg = "Updating assessment result datasource version history";
			AssessmentLogPeer.logWarn(resultHeader, msg, e);
			AdHocLogger.logException(e);
		}
	}

	/**
	 * @param resultHeader
	 * @param tests
	 * @param timeFactor
	 * @throws GuardAssessmentException
	 * @throws GuardAssessmentCancelation
	 */


	/**
	 * @param resultHeader
	 * @param test
	 * @param datasource
	 * @param count
	 */
	// static
	public  void recordReconnection(
			AssessmentResultHeader resultHeader, AssessmentTest test, Datasource datasource, int count
	) {
		String msg = "Connect (" + count + ") to: '" + datasource + "' for test: '" + test + "'.";
		AssessmentLogPeer.logWarn(resultHeader, msg);
	}

	/**
	 * @param assessmentTest
	 * @param resultHeader
	 * @param datasource
	 * @param dsce
	 * @return The error score
	 */
	protected TestScore recordConnectionFailureResult(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader,
			Datasource datasource, DataSourceConnectException dsce
	) {
		TestScore score = TestScore.ERROR;
		String result = Say.what(
				Say.ASMT_MSG_CONNECT_FAIL,
				//Say.ASMT_SUB_USER, datasource.getUserName(),
				Say.ASMT_SUB_DS, String.valueOf(datasource)
		);
		AssessmentLogPeer.logTestError(resultHeader, assessmentTest, dsce);
		TestResultPeer.recordResult(assessmentTest, resultHeader, datasource, score, result, dsce);
		return score;
	}


	/**
	 * Updates the run statistics and updates the pass percentage and overall recommendataion.
	 * @param resultHeader
	 * @param score
	 * @throws GuardAssessmentException
	 */
	protected void updateStatistics(
			AssessmentResultHeader resultHeader, TestScore score
	) throws GuardAssessmentException {
		Datasource datasource = null;
		this.updateStatistics(resultHeader, score, datasource);
	}

	/**
	 * Updates the run statistics and updates the pass percentage and overall recommendation.
	 * @param resultHeader
	 * @param score
	 * @param datasource
	 * @throws GuardAssessmentException
	 */
	protected void updateStatistics(
			AssessmentResultHeader resultHeader, TestScore score, Datasource datasource
	) throws GuardAssessmentException {
		// TODO - later
		//updateStatistics(resultHeader, score, datasource, 0);
	}

	/**
	 * Updates the running percentage for the test run.
	 * @param resultHeader
	 * @param score
	 * @param datasource
	 * @param minutes If set to zero the Run record will be touched to update the timestamp.
	 *  This is so certain tests that cannot periodically update the run record can set the timestamp
	 *  far into the future so that the process will not time out. They would pass a non-zero value.
	 * @throws GuardAssessmentException
	 */
	// TODO -later
	/*
	protected void updateStatistics(
			AssessmentResultHeader resultHeader, TestScore score, Datasource datasource, int minutes
	) throws GuardAssessmentException {
		// update the timestamp on the process run record

		if(minutes == 0) {
			try {
				run.touch();

			} catch (Exception e) {
				// should not occur - if torque is functioning
				String msg = "Unable to touch the Process Run record.";
				// LOG.error(msg, e);
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), resultHeader, e);
				throw new GuardAssessmentException(msg, e);
			}
		}

		// update the statistics
		if (datasource == null) {
			this.statistics.updateStatistics(score);
		} else {
			this.statistics.updateStatistics(score, datasource);
		}

		// set the running score on the result header except if it is an error
		if ( TestScore.PASS.equals(score) || TestScore.FAIL.equals(score) ) {
			double passPct = this.statistics.getPassPercentage();
			resultHeader.setOverallScore(passPct);
			String recommend = RecommendationTextPeer.findAssessmentRecommendation(passPct);
			resultHeader.setRecommendationText(recommend);

			try {
				resultHeader.save();
			} catch (Exception e) {
				// should not occur - if torque is functioning
				String msg = "Unable to save the Assessment Result Header.";
				// LOG.error(msg, e);
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), resultHeader, e);
				throw new GuardAssessmentException(msg, e);
			}
		}
	}
	*/
	
	/**
	 * @param assessmentTest
	 * @param datasource
	 * @return Whether the Datasource type is runnable for the available test.
	 */
	boolean isTypeMatch(AssessmentTest assessmentTest, Datasource datasource) {
		boolean result = true;
		if ( assessmentTest.usesDatasource() ) {
			try {
				DatasourceType testType = assessmentTest.getDatasourceType();
				DatasourceType dsType = datasource.getDatasourceType();
				result = dsType.equals(testType);
			} catch (Throwable t) {
				// can't have a datasource without a type.
				AdHocLogger.logException(t);
			}
		}
		return result;
	}

	/**
	 * Logs the cancellation
	 * @throws GuardAssessmentException
	 * @throws GuardAssessmentCancelation
	 */
	/*
	private void cancelAssessment(AssessmentResultHeader resultHeader)
	throws GuardAssessmentCancelation {
        int run = 1000;
		AssessmentLogPeer.logCancel(resultHeader);
		run.cancel();
		throw new GuardAssessmentCancelation( String.valueOf(run) );
	}
	*/
	
	/**
	 * @return The Assessment Result Header
	 * @throws GuardAssessmentException
	 */
	protected AssessmentResultHeader getAssessmentResultHeader() throws GuardAssessmentException {

		if (this.assessmentResultHeader == null) {
			AssessmentResultHeader header = new AssessmentResultHeader();
			try {
				//ClsProcessRun run = this.getClsProcessRun();
				//if ( run.getTaskId() > 0) {
				//	header.setTaskId(run.getTaskId());
				//} else {
					header.setTaskId(this.taskId);
				//}
				header.setExecutionDate( new Date() );
				header.setReceivedByAll( this.taskId == -1 );
				header.setOverallScore(-1);

				String date;
				date = this.getSecurityAssessment().getFromDate();
				header.setFromDate( RepDateUtils.getRealDate(date) );

				date = this.getSecurityAssessment().getToDate();
				header.setToDate( RepDateUtils.getRealDate(date) );

				// add the Security Assessment and its associated de-normalized fields
				header.setSecurityAssessment( this.getSecurityAssessment() );
				header.setAssessmentDesc( this.getSecurityAssessment().getAssessmentDesc() );
				header.setFilterClientIp( this.getSecurityAssessment().getFilterClientIp() );
				header.setFilterServerIp( this.getSecurityAssessment().getFilterServerIp() );

				header.save();
				this.assessmentResultHeader = header;

				/* TODO - later
				// add the report result header to the job
				run.setReportResultId( header.getAssessmentResultId() );
				run.setTimestamp( new Date() );
				run.save();
				*/
			} catch (GuardRepGeneralException e) {
				String msg = "Unexpected error parsing the dates while creating the assessment result header.";
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), header, e);
				throw new GuardAssessmentException(msg, e);

			} catch (ParseException e) {
				String msg = "Unexpected error parsing the dates while creating the assessment result header.";
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), header, e);
				throw new GuardAssessmentException(msg, e);

			} catch (Exception e) {
				String msg = "Unexpected error saving the assessment result header.";
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), header, e);
				throw new GuardAssessmentException(msg, e);

			} catch (Throwable t) {
				String msg = "Unexpected error creating the assessment result header.";
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), header, t);
				throw new GuardAssessmentException(msg, t);
			}
		}

		return this.assessmentResultHeader;
	}

	private Double timeFactor = null;

	/**
	 * @return The time factor.
	 * @throws GuardAssessmentException
	 */
	private double getTimeFactor()
	throws GuardAssessmentException {

		if (this.timeFactor == null) {
			try {
				String fromDateStr = this.getSecurityAssessment().getFromDate();
				String toDateStr = this.getSecurityAssessment().getToDate();
				Date from = RepDateUtils.getRealDate(fromDateStr);
				Date to = RepDateUtils.getRealDate(toDateStr);

				Calendar cal1 = new GregorianCalendar();
				cal1.setTime(from);
				double l1 = cal1.getTimeInMillis();

				Calendar cal2 = new GregorianCalendar();
				cal2.setTime(to);
				double l2 = cal2.getTimeInMillis();
				double factor = (l2 - l1) / 86400000;
				this.timeFactor = factor;

			} catch (GuardRepGeneralException e) {
				// should not occur - validated by ui
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), this.getAssessmentResultHeader(), e);
				throw new GuardAssessmentException("Wrong number of tokens for the from or to date", e);

			} catch (ParseException e) {
				// should not occur - validated by ui
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), this.getAssessmentResultHeader(), e);
				throw new GuardAssessmentException("Could not parse the from or to date.", e);

			} catch (Throwable t) {
				String msg = "Unexpected error creating the time factor.";
				AssessmentLogPeer.logFatal( this.getSecurityAssessment(), this.getAssessmentResultHeader(), t);
				throw new GuardAssessmentException(msg, t);
			}
		}
		return this.timeFactor;
	}

	/**
	 * Queues up a Listener Process for running.
	 * @param securityAssessment The Process to run.
	 * @param auditTaskId The id of the Audit Task that triggered the Process run.
	 * @param auditTaskDescription The description of the audit task.
	 * @return The Run status of the process.
	 * @throws InitializationException
	 */
	/*
	public static ClsProcessRun submit(
			SecurityAssessment securityAssessment, long auditTaskId, String auditTaskDescription
	) throws InitializationException {

		ClsProcessRunPeer.JobType type = ClsProcessRunPeer.JobType.ASSESSMENT;
		int entityId = securityAssessment.getAssessmentId();
		String entityDesc = securityAssessment.getAssessmentDesc();

		Collection<Datasource> datasources;
		datasources = securityAssessment.listDatasources();

		return ClsProcessRunPeer.submit(
				type, entityId, entityDesc, auditTaskId, auditTaskDescription, datasources
		);
	}
	*/
	
	/**
	 * Logs a message that tells if all datasources of a datasource type are going to be assessed.
	 * @param rhId Assessment Result ID	 * @param dss List of Datasources
	 * @throws TorqueException
	 * @throws SQLException
	 */
	// static
	/*
	private static void correlate(AssessmentResultHeader resultHeader, Collection<Datasource> dss)
	throws  SQLException{
	    
		//if ( LOG.isInfoEnabled() ) {
		//	LOG.info("Correlating the list of datasources with all known datasources: " + Say.NL + dss);
		//}


		// get a map of Datasource IPs by Datasource Type name
		Map<String,Set<String>> gs = DatasourcePeer.groupIpsByType(dss);
		for(Iterator<String> ite = gs.keySet().iterator(); ite.hasNext();)
		{
			StringBuilder sb = new StringBuilder();
			String dbType = ite.next();
			Set<String> set = gs.get(dbType);
			List<String> ips = GdmAccessPeer.getAllIpsByServerType(dbType);
			if(ips==null||ips.isEmpty()) {
				continue;
			}

			for(String ip:ips){
				if(!set.contains(ip))
				{
					if(sb.length()>0) {
						sb.append(", ");
					}
					sb.append(ip);
					if ( LOG.isDebugEnabled() ) {
						LOG.debug("Added: ip");
					}
				}
			}
			AssessmentLogPeer.logCorrelation(resultHeader, dbType, sb.toString() );
		}
	}
	*/
	
	/**
	 * @return a list of the datasources for the process with the id an password set from the credentials.
	 * @throws GuardAssessmentException
	 */
	public List<Datasource> getDatasources() throws GuardAssessmentException {

		if (this.datasources==null) {
			this.datasources = new ArrayList<Datasource>();
			this.datasources.addAll( this.getSecurityAssessment().getDatasources() );

			//AssessmentResultHeader resultHdr = this.getAssessmentResultHeader();
			//try {
				/*
				// Get the credentials for any datasource without stored user and password
				List<ClsProcessRunCredential> credentials = new ArrayList<ClsProcessRunCredential>();
				credentials.addAll( this.getClsProcessRun().getClsProcessRunCredentials() );
				ClsProcessRunCredentialPeer.matchDatasourceCredentials(this.datasources, credentials);
				*/
				
				// add the datasources to the assessment result datasources snapshot
				//resultHdr.addAssessmentResultDatasource(this.datasources);

				// TODO - Later
				// log any detected datasources that are not listed in this assessment
				// correlate(resultHdr, this.datasources);
            /*
			} catch (GuardAssessmentException e) {
				throw e;
	
			} catch (SQLException e) {
				// should not occur - static sql
				String msg = "Could not execute sql to retrieve detected database hosts";
				throw new GuardAssessmentException(msg, e);
            */
			//} catch (Throwable t) {
			//	String msg = "Unexpected error retrieving the datasources.";
			//	AssessmentLogPeer.logFatal( this.getSecurityAssessment(), resultHdr, t);
			//	throw new GuardAssessmentException(msg, t);
			//}
		}
		return this.datasources;
	}

}


