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
import java.util.Set;
import java.util.TreeMap;
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
//import com.guardium.map.SecurityAssessmentMap;
import com.guardium.map.RecommendationTextMap;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.runtest.AssessmentRunner;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class SecurityAssessmentMap {

	private static final transient Logger LOG = Logger.getLogger(AssessmentRunner.class);
	//private AssessmentResultHeaderMap AssessmentResultHeaderPeer = new AssessmentResultHeaderMap();

	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();	
	AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	

	//public DatasourceMap DatasourcePeer = new DatasourceMap();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	//private GroupDescMap GroupDescPeer = new GroupDescMap();

	// constructor	
	private static   SecurityAssessmentMap SecurityAssessmentMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	private SecurityAssessmentMap () {
		// initMap();
	}
	
	public static synchronized SecurityAssessmentMap getSecurityAssessmentMapObject() {
		if (SecurityAssessmentMapObject == null) {
			SecurityAssessmentMapObject = new SecurityAssessmentMap();
		}
		return SecurityAssessmentMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static ConcurrentHashMap hm = new ConcurrentHashMap();
	private static int currentSecurityAssessmentId = 20001;
	
	
	public int getMapSize() {
		return hm.size();
	}
	
	public void add (SecurityAssessment v) {
		v.setAssessmentId(currentSecurityAssessmentId);
		hm.put(currentSecurityAssessmentId, v);
		currentSecurityAssessmentId++;
	}
	
	public void add (long id, SecurityAssessment v) {
		hm.put(id, v);
	}
	
	public void remove (long id) {
		hm.remove(id);
	}	

	public void removeAll () {
		hm.clear();
	}	
	
	public SecurityAssessment getSecurityAssessment (long id) {
		return (SecurityAssessment)hm.get(id);
	}	
	
	// get available test by data source type
	public List getListByTestId(int assessment_test_id) {
	   List <SecurityAssessment> alist = new ArrayList<SecurityAssessment>();
		   
	   Iterator it = null;		   
	   it = hm.entrySet().iterator();

	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        SecurityAssessment t = (SecurityAssessment)pairs.getValue();
	        if (t.getAssessmentId() == assessment_test_id) {
	        	alist.add(t);
	        }
	   }
	   return alist;
	}

	public List<SecurityAssessment> getAllList() {
				
		Map<Integer, SecurityAssessment> sortedMap = new TreeMap<Integer,SecurityAssessment >(hm);
		
		List <SecurityAssessment> alist = new ArrayList<SecurityAssessment>();
		Iterator it = null;		   
		it = sortedMap.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
		    SecurityAssessment t = (SecurityAssessment)pairs.getValue();
		    alist.add(t);
		}
		return alist;

	}
}