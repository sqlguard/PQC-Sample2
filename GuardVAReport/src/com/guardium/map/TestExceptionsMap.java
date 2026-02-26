/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
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
import com.guardium.data.TestExceptions;
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

public class TestExceptionsMap {

	private static final transient Logger LOG = Logger.getLogger(AssessmentRunner.class);
	//private TestResultMap TestResultPeer = new TestResultMap();
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();	
	AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
	AssessmentTestMap AssessmentTestPeer = AssessmentTestMap.getAssessmentTestMapObject();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	
	// constructor
    
	private static TestExceptionsMap TestExceptionsMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private TestExceptionsMap () {
		// initMap();
	}
	
	public static synchronized TestExceptionsMap getTestExceptionsMapObject() {
		if (TestExceptionsMapObject == null) {
			TestExceptionsMapObject = new TestExceptionsMap();
		}
		return TestExceptionsMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static ConcurrentHashMap hm = new ConcurrentHashMap();
	private static long currentTestExceptionsId = 20000;
	
	
	public int getMapSize() {
		return hm.size();
	}
	
	public void add (TestExceptions v) {
		v.setTestExceptionsId(currentTestExceptionsId);
		hm.put(currentTestExceptionsId, v);
		currentTestExceptionsId++;
	}
	
	public void add (long id, TestExceptions v) {
		hm.put(id, v);
	}
	
	public void remove (long id) {
		hm.remove(id);
	}	

	public TestExceptions getTestExceptions (long id) {
		TestExceptions s = (TestExceptions)hm.get(id);
		return s;
	}	
	
	// get available test by data source type
	public List getListByTestId(int test_id) {
	   List <TestExceptions> alist = new ArrayList<TestExceptions>();
		   
	   Iterator it = null;		   
	   it = hm.entrySet().iterator();

	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        TestExceptions t = (TestExceptions)pairs.getValue();
	        if (t.getTestId() == test_id) {
	        	alist.add(t);
	        }
	   }
	   return alist;
	}


}