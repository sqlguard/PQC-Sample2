/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import com.guardium.assessment.i18n.Say;
import com.guardium.assessment.tests.TestScore;
import com.guardium.data.AssessmentLog;
import com.guardium.data.AssessmentLogType;
import com.guardium.data.TestResult;

//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
import com.guardium.map.AssessmentLogMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.AvailableTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
import com.guardium.data.AssessmentResultDatasource;
import com.guardium.map.AssessmentResultHeaderMap;
import com.guardium.map.RecommendationTextMap;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.runtest.AssessmentRunner;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;
import com.guardium.utils.WriteResult;

public class TestResultMap {

	private static final transient Logger LOG = Logger.getLogger(AssessmentRunner.class);
	//private TestResultMap TestResultPeer = new TestResultMap();
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
	AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
	AssessmentTestMap AssessmentTestPeer = AssessmentTestMap.getAssessmentTestMapObject();
	GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	
    
	private static TestResultMap TestResultMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private TestResultMap () {
		// initMap();
	}
	
	public static synchronized TestResultMap getTestResultMapObject() {
		if (TestResultMapObject == null) {
			TestResultMapObject = new TestResultMap();
		}
		return TestResultMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static int currentTestResultId = 20000;
	
	// create a list
	private static List <TestResult> trlist = new ArrayList<TestResult>();
	
	public int getMapSize() {
		return trlist.size();
	}
	
	public void cleanMap () {
		trlist.clear();
		return;
	}
	
	public List<TestResult> getList() {
		return trlist;
	}

	public void setList(List<TestResult> tlist) {
		this.trlist = tlist;
	}

	public static void add (TestResult t) {
		t.setTestResultId(currentTestResultId);
		trlist.add(t);
		currentTestResultId++;
		return;
	}
	
	
	public void add (int id, TestResult v) {
		v.setTestResultId(id);
		trlist.add(v);
	}

	public TestResult getTestResult (int id) {
		return (TestResult)trlist.get(id);
	}	
	
	// get available test by data source type
	public List getListByTestId(int test_id) {
	   List <TestResult> alist = new ArrayList<TestResult>();

	   for (TestResult t: trlist) {
			if (t.getTestId() == test_id) {
	        	alist.add(t);
			}
	   }	   
	   return alist;
	}

	// static
	public TestResult recordResult(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, Datasource datasource,
			TestScore score, String result, Throwable throwable
	) {

		AvailableTest availableTest;
		String recommend = null;

		availableTest = assessmentTest.getAvailableTest();
		recommend = RecommendationTextPeer.findRecommendation(availableTest, score);

		//LOG.error(recommend, e);
		//AdHocLogger.logException(e);


		int reportResultId = -1;
		String dtl = Informer.thrownMessage(throwable);

		return recordResult(
				assessmentTest, resultHeader, datasource,
				score, result + Say.NL + Say.SP + dtl, recommend, reportResultId
		);
	}

	/**
	 * Records the test results
	 * @param assessmentTest
	 * @param resultHeader
	 * @param datasource
	 * @param score
	 * @param result
	 * @param recommend
	 * @param reportResultId
	 * @return The populated and saved Test Result
	 */
	// static
	public TestResult recordResult(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, Datasource datasource,
			TestScore score, String result, String recommend, long reportResultId
	){
		return recordResult( assessmentTest, resultHeader, datasource,
				score, result, recommend, reportResultId, null);
	}
    // static
	public TestResult recordResult(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, Datasource datasource,
			TestScore score, String result, String recommend, long reportResultId, String detail
	) {
		/*
		LOG.info(score + Say.CLN + assessmentTest + Say.NL + result );

		if ( Check.isEmpty(result) ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.info("No Result Text: " + assessmentTest);
			}
		}
		*/
		
		TestResult testResult = new TestResult();
		try {
			AvailableTest availableTest = assessmentTest.getAvailableTest();

			// set the relationship references
			testResult.setAssessmentResultHeader(resultHeader);
			testResult.setTestId( assessmentTest.getTestId() );
			testResult.setAssessmentTestId( assessmentTest.getAssessmentTestId() );
			testResult.setReportResultId(reportResultId);

			// set the de-normalized test and score information
			testResult.setTestDesc( availableTest.getTestDesc() );
			testResult.setCategoryName( availableTest.getCategoryName() );
			testResult.setSeverity( availableTest.getSeverity() );
			testResult.setTestScore( score );
			testResult.setScoreDesc( score.getDescription() );
			testResult.setResultText(result);
			testResult.setRecommendationText(recommend);
			testResult.setShortDescription(availableTest.getShortDescription());
			testResult.setExternalReference(availableTest.getExternalReference());
			testResult.setStig_ref(availableTest.getStig_ref());
			testResult.setStig_severity(availableTest.getStig_severity());
			testResult.setStig_iacontrols(availableTest.getStig_iacontrols());
			testResult.setStig_srg(availableTest.getStig_srg());
			if(detail!=null) {
				testResult.setDetail(detail);
			}

			try {
				int exceptionGroupId = assessmentTest.getExceptionsGroupId();
				if ( exceptionGroupId > 0 ) {
					GroupDesc g = GroupDescPeer.retrieveByPK(exceptionGroupId);
					if (g!= null)
						testResult.setExceptionGroupDescription(g.getGroupDescription());
				}
			}
			catch (Throwable t) {
				AdHocLogger.logException(t);
			}
			
			// TODO - later
			/*
			// set the reference to the Result Datasource, if any
			if (availableTest.usesDatasource() ) {
				AssessmentResultDatasource snapshot = resultHeader.findAssessmentResultDatasource(datasource);
				testResult.setAssessmentResultDatasourceId( snapshot.getAssessmentResultDatasourceId() );
			}
			*/
			
			// add the threshold prompt to the test result
			String prompt = availableTest.getThresholdPrompt();
			if ( !Check.isEmpty(prompt) ) {
				testResult.setThresholdString(
						prompt + " used in this assessment: " + assessmentTest.getThresholdValue()
				);
			}

			// check the modified flag - sets it if it needs it, and clears the assessment test if needed
			checkModifiedFlag(assessmentTest, resultHeader, testResult);

			// set up datasource infor
			testResult.setDatasourceDesc(datasource.getDescription());
			testResult.setDatasourceType(datasource.getTypeName());
			testResult.setDatasourceVersion(datasource.getVersionLevel());
			
			//testResult.setTestStartTime("");
			//testResult.setTestEndTime("");
			
			// should not save here
			// will save later
			//testResult.save();

		} catch (Exception e) {
			// should not happen if torque has been working
			String msg =
				"Could NOT record the results for: " + score + Say.CLN + assessmentTest + Say.NL + result
			;
			LOG.error(msg, e);
			AdHocLogger.logException(e);
		}
		return testResult;
	}

	/**
	 * Synchronizes the modified flag based on the existance of audit tasks
	 */
	// static
	private void checkModifiedFlag(
			AssessmentTest assessmentTest, AssessmentResultHeader resultHeader, TestResult testResult
	) throws Exception {

		if (assessmentTest.getModifiedFlag() ) {
			testResult.setParameterModifiedFlag(true);
			//boolean hasAudits = AssessmentResultHeaderPeer.existsAuditTaskResultForAssessment(assessmentTest);
			boolean hasAudits = false;
			if (resultHeader.getTaskId() >= 1 || !hasAudits) {
				assessmentTest.setModifiedFlag(false);
				assessmentTest.save();
			}
		}
	}


	 /**
     * override retrieve by pk to use criteria for inv databases - must be done in all results classes
     *
     * @param pk the primary key
     * @throws TorqueException Any exceptions caught during processing will be
     *         rethrown wrapped into a TorqueException.
     */
	// static
    public TestResult retrieveByPK(int pk)
     {
    	return getTestResult (pk);

    }
    
	public List getTestDescList() {
		List <String> alist = new ArrayList<String>();
		
		for (TestResult t: trlist) {
		    String tmp = t.getTestDesc();
		    alist.add(tmp);			
		}	 
		
		return alist;
	}
    
	public List<TestResult> getTestResultList_old () {
		// resequence the list to have full result at the front
		List <TestResult> qfulllist = new ArrayList<TestResult>();
		List <TestResult> qpartlist = new ArrayList<TestResult>();
		
		List <TestResult> cfulllist = new ArrayList<TestResult>();
		//List <TestResult> cpartlist = new ArrayList<TestResult>();
		
		// sort by querybase and cve, full and partial
		for (TestResult tr: trlist) {

			int testid = tr.getTestId();
		    if (testid >= 2000 && testid <=3000) {
		    	// querybased tests
		        if( Arrays.asList(2086,2111,2113,2114,2115,2198,2201,2251,2258,2259, 
		          2273,2276,2278,2279,2280,2281,2282,2284,2286,2308,
		          2013,2011,2009,2004,2194,2289,2296,2298,2301,2313,
		          2053,2052,2051,2050,2049,
		          2022,2021,2015,2016,2311,2312,2373,2378,2381,2453,
		          2062,2063,2067,2068,2069,2070,2072,2075,2079,2081,
		          2214,2215,2216,2217,2219,2220,2222,2224,2227,2229,
		          2048,2046,2036,2035,2032,2034,2029,2026,2023,2024
		        ).contains(testid) ) {
			        qfulllist.add(tr);
		        }
		        else {
		        	qpartlist.add(tr);
		        }
		    }
		    else {
		    	// cve tests
		    	cfulllist.add(tr);
		    }
		}

		List <TestResult> newlist = new ArrayList<TestResult>();
		
		// query fulllist - sort by fail, pass
		List <TestResult> passlist = new ArrayList<TestResult>();
		List <TestResult> faillist = new ArrayList<TestResult>();
		List <TestResult> otherlist = new ArrayList<TestResult>();
		
		for (TestResult tr: qfulllist) {
            if (tr.getTestScore() == TestScore.PASS) {
            	passlist.add(tr);
            }
            else if (tr.getTestScore() == TestScore.FAIL) {
            	faillist.add(tr);
            }
            else {
            	otherlist.add(tr);
            }
		}
		
		newlist.addAll(faillist);
		newlist.addAll(otherlist);
		newlist.addAll(passlist);
		
		// query partial list - sort by fail, pass
		passlist = new ArrayList<TestResult>();
		faillist = new ArrayList<TestResult>();
		otherlist = new ArrayList<TestResult>();
		
		for (TestResult tr: qpartlist) {
            if (tr.getTestScore() == TestScore.PASS) {
            	passlist.add(tr);
            }
            else if (tr.getTestScore() == TestScore.FAIL) {
            	faillist.add(tr);
            }
            else {
            	otherlist.add(tr);
            }
		}
		
		newlist.addAll(faillist);
		newlist.addAll(otherlist);
		newlist.addAll(passlist);		
		
		// cve test - sort by fail, pass
		passlist = new ArrayList<TestResult>();
		faillist = new ArrayList<TestResult>();
		otherlist = new ArrayList<TestResult>();
		
		for (TestResult tr: cfulllist) {
            if (tr.getTestScore() == TestScore.PASS) {
            	passlist.add(tr);
            }
            else if (tr.getTestScore() == TestScore.FAIL) {
            	faillist.add(tr);
            }
            else {
            	otherlist.add(tr);
            }
		}
		
		newlist.addAll(faillist);
		newlist.addAll(otherlist);
		newlist.addAll(passlist);

		return newlist;
	}
	
	
    public List<TestResult> getTestResultList () {
    	
    	// do modify first, to set result text to recommendation for the ERROR case.
    	modifyResultList ();
    	
        // resequence the list to have full result at the front
        ArrayList <TestResult> qfulllist = new ArrayList<TestResult>();
        ArrayList <TestResult> qpartlist = new ArrayList<TestResult>();

        ArrayList <TestResult> cfulllist = new ArrayList<TestResult>();

        //List <TestResult> cpartlist = new ArrayList<TestResult>();

        // sort by querybase and cve, full and partial
        for (TestResult tr: trlist) {

                int testid = tr.getTestId();
            if (testid >= 2000 && testid <=3000) {
                // querybased tests
                if( Arrays.asList(2086,2111,2113,2114,2115,2198,2201,2251,2258,2259,
                  2273,2276,2278,2279,2280,2281,2282,2284,2286,2308,
                  2013,2011,2009,2004,2194,2289,2296,2298,2301,2313,
                  2053,2052,2051,2050,2049,
                  2022,2021,2015,2016,2311,2312,2373,2378,2381,2453,
                  2062,2063,2067,2068,2069,2070,2072,2075,2079,2081,
                  2214,2215,2216,2217,2219,2220,2222,2224,2227,2229,
                  2048,2046,2036,2035,2032,2034,2029,2026,2023,2024
                ).contains(testid) ) {
                        qfulllist.add(tr);
                }
                else {
                        qpartlist.add(tr);
                }
            }
            else {
                // cve tests
                cfulllist.add(tr);
            }
        }

        // we got three list, need to sort by pass, fail. other error
        // and fail, and other error will sort again by category

        ArrayList<ArrayList<TestResult>> array = new ArrayList<ArrayList<TestResult>>();
        array.add(qfulllist);
        array.add(qpartlist);
        array.add(cfulllist);

        List <TestResult> newlist = new ArrayList<TestResult>();

        for (ArrayList al: array) {
        	ArrayList <TestResult> ar = (ArrayList <TestResult>)al;

            // sort by fail, pass
            ArrayList <TestResult> passlist = new ArrayList<TestResult>();
            ArrayList <TestResult> faillist = new ArrayList<TestResult>();
            ArrayList <TestResult> otherlist = new ArrayList<TestResult>();
            //ArrayList <TestResult> newpasslist = new ArrayList<TestResult>();
            //ArrayList <TestResult> newfaillist = new ArrayList<TestResult>();
            //ArrayList <TestResult> newotherlist = new ArrayList<TestResult>();

            for (TestResult ts: ar) {
            	if (ts.getTestScore() == TestScore.PASS) {
            		passlist.add(ts);
                }
                else if (ts.getTestScore() == TestScore.FAIL) {
                    faillist.add(ts);
                }
                else {
                    otherlist.add(ts);
                }
            }

                    /*
                    asmt.severity.label.critical=Critical
                    asmt.severity.label.major=Major
                    asmt.severity.label.minor=Minor
                    asmt.severity.label.caution=Cautionary
                    asmt.severity.label.info=Informational
    
                    asmt.severity.label.critical.abbrev=Critical
                    asmt.severity.label.major.abbrev=Major
                    asmt.severity.label.minor.abbrev=Minor
                    asmt.severity.label.caution.abbrev=Caution
                    asmt.severity.label.info.abbrev=Info
    
                    asmt.datasource.severity.label.high=High
                    asmt.datasource.severity.label.medium=Medium
                    asmt.datasource.severity.label.low=Low
                    asmt.datasource.severity.label.info=Info
                    asmt.datasource.severity.label.none=None
                     */


            ArrayList<ArrayList<TestResult>> array2 = new ArrayList<ArrayList<TestResult>>();
            array2.add(faillist);
            array2.add(otherlist);
            array2.add(passlist);

            for (ArrayList al2: array2) {
            	ArrayList <TestResult> ar2 = (ArrayList <TestResult>)al2;


            	if (ar2.size() > 0) {
            		/*
                      Test Result Severity: CRITICAL
                      Test Result Severity: MAJOR
                      Test Result Severity: CAUTION
            		 */
            		ArrayList critlist = new ArrayList<TestResult>();
            		ArrayList majorlist = new ArrayList<TestResult>();
            		ArrayList minorlist = new ArrayList<TestResult>();
            		ArrayList cautionlist = new ArrayList<TestResult>();
            		ArrayList infolist = new ArrayList<TestResult>();

            		for (TestResult f: ar2) {
            			// sort by category
            			if (f.getSeverity().equalsIgnoreCase("CRITICAL")) {
            				critlist.add(f);
            			}
            			else if (f.getSeverity().equalsIgnoreCase("MAJOR")) {
            				majorlist.add(f);
            			}
            			else if (f.getSeverity().equalsIgnoreCase("MINOR")) {
            				minorlist.add(f);
            			}
            			else if (f.getSeverity().equalsIgnoreCase("CAUTION")) {
            				cautionlist.add(f);
            			}
            			else if (f.getSeverity().equalsIgnoreCase("INFO")) {
            				infolist.add(f);
            			}
            		}
            		
            		// reformat the faillist
            		//faillist = new ArrayList<TestResult>();
            		newlist.addAll(critlist);
            		newlist.addAll(majorlist);
            		newlist.addAll(minorlist);
            		newlist.addAll(cautionlist);
            		newlist.addAll(infolist);
            	}
            } // for
            	
        }

        return newlist;
    }
                        

	
	
	public void dumpMap(Writer wr) {
		List <TestResult> fulllist = new ArrayList<TestResult>();
		fulllist = getTestResultList ();
		
		// dump the list
		for (TestResult t: fulllist) {
		    List<String> sr = t.dump();
		    WriteResult.writeToLogFile(sr, wr);
		}	 
		
		return;
	}
	
	public void dumpSummary () {
		int [] anArray = getSummary();
		
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
		
		// write result
		WriteResult.writeOutput ("tests PASS: " + passCount);		
		WriteResult.writeOutput ("tests FAIL: " + failCount);
		WriteResult.writeOutput ("tests ERROR: " + errorCount);		
		WriteResult.writeOutput ("tests NO_CAS_DATA:	" + noCasCount);		
		WriteResult.writeOutput ("tests NO_REPORT_DATA: " + noReportCount);	
		WriteResult.writeOutput ("tests UNSUPPORTED_DB_VERSION: " +  unSupportDBCount);		
		WriteResult.writeOutput ("tests UNSUPPORTED_OS_VERSION: " + unSupportOSCount);		
		WriteResult.writeOutput ("tests SPECIAL_ERROR: " + specialErrorCount);
		WriteResult.writeOutput ("tests CAS_DATA_COLLECTION_ERROR: " + casCollectErrorCount);		
		WriteResult.writeOutput ("tests OBSOLETE_PARAMETER: " + obsParaCount);		
		WriteResult.writeOutput ("tests DEPRECATED_PARAMETER: " + depParaCount);
		WriteResult.writeOutput ("tests CVE_NOT_REPORTED: " + cveNotCount);		
		WriteResult.writeOutput ("tests NO_USER_DATA: " + noUserDataCount);		
		WriteResult.writeOutput ("tests MODULES_NOT_PRESENT: " + moduleNotCount);
		WriteResult.writeOutput ("tests PRE_TEST_CHECK_FAILED: " + PreTestFailedCount);
		WriteResult.writeOutput ("tests EXECUTION_TEST_ROUTINE_CHECK_FAILED: " + ExecTestFailedCount);

		WriteResult.writeOutput ("Total tests: " + trlist.size());
		
	}
	public int [] getSummary () {
		
		int[] anArray;

	    // allocates memory for 16 integers
	    anArray = new int[16];
	     
		int passCount = 0;
		int failCount = 0;
		int errorCount = 0;
		int noCasCount = 0;
		int noReportCount = 0;
		int unSupportDBCount = 0;
		int unSupportOSCount = 0;
		int specialErrorCount = 0;
		int casCollectErrorCount = 0;
		int obsParaCount = 0;
		int depParaCount = 0;
		int cveNotCount = 0;
		int noUserDataCount = 0;
		int moduleNotCount = 0;
		int PreTestFailedCount = 0;
		int ExecTestFailedCount = 0;

	/** The Test Passed. */
	//	PASS(1),
	/** The Test Failed. */
	//	FAIL(0),
	/** Unspecified Error. */
	//	ERROR(-1),
	//  NO_CAS_DATA(-2),
	/** Report data is not available to evaluate this test and needs to be run. */
	//  NO_REPORT_DATA(-3),
	/** The database version does not support this test */
	//  UNSUPPORTED_DB_VERSION(-4),
	/** The OS version does not support this test */
	//  UNSUPPORTED_OS_VERSION(-5),
	/** Unknown */
	//  SPECIAL_ERROR(-6),
	/** Error in collecting CAS data **/
	//  CAS_DATA_COLLECTION_ERROR(-7),
	/** Obsolete Database Version **/
	//  OBSOLETE_PARAMETER(-8),
	/** Deprecated Database Version **/
	//  DEPRECATED_PARAMETER(-9),
	/** CVE not reported for the database version **/
	//  CVE_NOT_REPORTED(-10),
	/** user data not set in GDMMONITOR.OS_USER and GDMMONITOR.OS_GROUP tables **/
	//  NO_USER_DATA(-11),
	/** Modules listed for PTF not present in DB2 MEPL **/
	//  MODULES_NOT_PRESENT(-12),
	/** The Pre Test Check Failed Test Not Executed. */ 
	//  PRE_TEST_CHECK_FAILED(-13),
	/** The calculateScore routine not defined in the test */
	//  EXECUTION_TEST_ROUTINE_CHECK_FAILED(-14);
	/** The Test Score Definition ID of this Test Score. */


		for (TestResult t: trlist) {
			TestScore score = t.getTestScore();			
		    
		    
			switch (score) {

			case PASS:
				passCount++;
				break;
			case FAIL:
				failCount++;
				break;
			case ERROR:
				errorCount++;
				break;				
			case NO_CAS_DATA:				
				noCasCount ++;
				break;
			case NO_REPORT_DATA:
				noReportCount++;
				break;
			case UNSUPPORTED_DB_VERSION:
				unSupportDBCount++;
				break;
			case UNSUPPORTED_OS_VERSION:
				unSupportOSCount++;
				break;
			case SPECIAL_ERROR:
				specialErrorCount++;
				break;
			case CAS_DATA_COLLECTION_ERROR:
				casCollectErrorCount++;
				break;
			case OBSOLETE_PARAMETER:
				obsParaCount++;
				break;
			case DEPRECATED_PARAMETER:
				depParaCount++;
				break; 
			case CVE_NOT_REPORTED:
				cveNotCount++;
				break;
			case NO_USER_DATA:
				noUserDataCount++;
				break;
			case MODULES_NOT_PRESENT:
				moduleNotCount++;
				break;
			case PRE_TEST_CHECK_FAILED:
				PreTestFailedCount++;
				break;
			case EXECUTION_TEST_ROUTINE_CHECK_FAILED:
				ExecTestFailedCount++;
				break;
			}
		    
		}		
		
		anArray[0] = passCount;
		anArray[1] = failCount;
		anArray[2] = errorCount;
		anArray[3] = noCasCount;
		anArray[4] = noReportCount;	
		anArray[5] = unSupportDBCount;
		anArray[6] = unSupportOSCount;
		anArray[7] = specialErrorCount;		
		anArray[8] = casCollectErrorCount;
		anArray[9] = obsParaCount;
		anArray[10] = depParaCount;
		anArray[11] = cveNotCount;
		anArray[12] = noUserDataCount;
		anArray[13] = moduleNotCount;
		anArray[14] = PreTestFailedCount;
		anArray[15] = ExecTestFailedCount;
				
        return anArray;
	}
	
	public int [] getSummary (int ttype) {
		
		// ttype 4 - query based test
		// ttype 6 - cve test
		int[] anArray;
		
		
	    // allocates memory for 18 integers
		// 0- 15 are for different reason count
		// 16 for total other error count
		// 17 for total count
	    anArray = new int[18];
	     
		int passCount = 0;
		int failCount = 0;
		int errorCount = 0;
		int noCasCount = 0;
		int noReportCount = 0;
		int unSupportDBCount = 0;
		int unSupportOSCount = 0;
		int specialErrorCount = 0;
		int casCollectErrorCount = 0;
		int obsParaCount = 0;
		int depParaCount = 0;
		int cveNotCount = 0;
		int noUserDataCount = 0;
		int moduleNotCount = 0;
		int PreTestFailedCount = 0;
		int ExecTestFailedCount = 0;

		int totalCount  = 0;
		int totalOtherCount = 0;
		
	/** The Test Passed. */
	//	PASS(1),
	/** The Test Failed. */
	//	FAIL(0),
	/** Unspecified Error. */
	//	ERROR(-1),
	//  NO_CAS_DATA(-2),
	/** Report data is not available to evaluate this test and needs to be run. */
	//  NO_REPORT_DATA(-3),
	/** The database version does not support this test */
	//  UNSUPPORTED_DB_VERSION(-4),
	/** The OS version does not support this test */
	//  UNSUPPORTED_OS_VERSION(-5),
	/** Unknown */
	//  SPECIAL_ERROR(-6),
	/** Error in collecting CAS data **/
	//  CAS_DATA_COLLECTION_ERROR(-7),
	/** Obsolete Database Version **/
	//  OBSOLETE_PARAMETER(-8),
	/** Deprecated Database Version **/
	//  DEPRECATED_PARAMETER(-9),
	/** CVE not reported for the database version **/
	//  CVE_NOT_REPORTED(-10),
	/** user data not set in GDMMONITOR.OS_USER and GDMMONITOR.OS_GROUP tables **/
	//  NO_USER_DATA(-11),
	/** Modules listed for PTF not present in DB2 MEPL **/
	//  MODULES_NOT_PRESENT(-12),
	/** The Pre Test Check Failed Test Not Executed. */ 
	//  PRE_TEST_CHECK_FAILED(-13),
	/** The calculateScore routine not defined in the test */
	//  EXECUTION_TEST_ROUTINE_CHECK_FAILED(-14);
	/** The Test Score Definition ID of this Test Score. */


		for (TestResult t: trlist) {
			if (ttype == 4) {
				if (t.getTestId() < 2000 || t.getTestId() > 3000) {
					continue;
				}
			}
			else if (ttype == 6) {
				if (t.getTestId() < 1000 || t.getTestId() > 2000) {
					continue;
				}
			}

			totalCount++;
			
			TestScore score = t.getTestScore();			
		    
		    
			switch (score) {

			case PASS:
				passCount++;
				break;
			case FAIL:
				failCount++;
				break;
			case ERROR:
				errorCount++;
				totalOtherCount++;
				break;				
			case NO_CAS_DATA:				
				noCasCount ++;
				totalOtherCount++;
				break;
			case NO_REPORT_DATA:
				noReportCount++;
				totalOtherCount++;
				break;
			case UNSUPPORTED_DB_VERSION:
				unSupportDBCount++;
				totalOtherCount++;
				break;
			case UNSUPPORTED_OS_VERSION:
				unSupportOSCount++;
				totalOtherCount++;
				break;
			case SPECIAL_ERROR:
				specialErrorCount++;
				totalOtherCount++;
				break;
			case CAS_DATA_COLLECTION_ERROR:
				casCollectErrorCount++;
				totalOtherCount++;
				break;
			case OBSOLETE_PARAMETER:
				obsParaCount++;
				totalOtherCount++;
				break;
			case DEPRECATED_PARAMETER:
				depParaCount++;
				totalOtherCount++;
				break; 
			case CVE_NOT_REPORTED:
				cveNotCount++;
				totalOtherCount++;
				break;
			case NO_USER_DATA:
				noUserDataCount++;
				totalOtherCount++;
				break;
			case MODULES_NOT_PRESENT:
				moduleNotCount++;
				totalOtherCount++;
				break;
			case PRE_TEST_CHECK_FAILED:
				PreTestFailedCount++;
				totalOtherCount++;
				break;
			case EXECUTION_TEST_ROUTINE_CHECK_FAILED:
				ExecTestFailedCount++;
				totalOtherCount++;
				break;
			}
		    
		}		
		
		anArray[0] = passCount;
		anArray[1] = failCount;
		anArray[2] = errorCount;
		anArray[3] = noCasCount;
		anArray[4] = noReportCount;	
		anArray[5] = unSupportDBCount;
		anArray[6] = unSupportOSCount;
		anArray[7] = specialErrorCount;		
		anArray[8] = casCollectErrorCount;
		anArray[9] = obsParaCount;
		anArray[10] = depParaCount;
		anArray[11] = cveNotCount;
		anArray[12] = noUserDataCount;
		anArray[13] = moduleNotCount;
		anArray[14] = PreTestFailedCount;
		anArray[15] = ExecTestFailedCount;
		
		// total other error count
		anArray[16] = totalOtherCount;
		
		// total count
		anArray[17] = totalCount;		
        return anArray;
	}
	
	public void modifyResultList () {
		
		for (TestResult t: trlist) {

			TestScore score = t.getTestScore();			
		    
			if (score != TestScore.PASS &&
					score != TestScore.FAIL && 
					score != TestScore.UNSUPPORTED_DB_VERSION &&
					score != TestScore.UNSUPPORTED_OS_VERSION)  {
				t.setRecommendationText(t.getResultText());
			}
		}
	}
	
}