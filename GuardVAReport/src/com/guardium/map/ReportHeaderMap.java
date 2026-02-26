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
import com.guardium.data.ReportHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.AvailableTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
import com.guardium.data.AssessmentResultDatasource;
import com.guardium.map.ReportHeaderMap;
import com.guardium.map.RecommendationTextMap;
import com.guardium.data.GroupDesc;
import com.guardium.map.GroupDescMap;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.runtest.AssessmentRunner;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Check;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class ReportHeaderMap {

	private static final transient Logger LOG = Logger.getLogger(AssessmentRunner.class);
	//private ReportHeaderMap ReportHeaderPeer = new ReportHeaderMap();
	
	DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();	
	AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
	AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
	//GroupDescMap GroupDescPeer = GroupDescMap.getGroupDescMapObject();
	
	private RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();

	private static ReportHeaderMap ReportHeaderMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private ReportHeaderMap () {
		// initMap();
	}
	
	public static synchronized ReportHeaderMap getReportHeaderMapObject() {
		if (ReportHeaderMapObject == null) {
			ReportHeaderMapObject = new ReportHeaderMap();
		}
		return ReportHeaderMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static ConcurrentHashMap hm = new ConcurrentHashMap();
	private static int currentReportHeaderId = 20000;
	
	
	public int getMapSize() {
		return hm.size();
	}
	
	public void add (ReportHeader v) {
		v.setReportId(currentReportHeaderId);
		hm.put(currentReportHeaderId, v);
		currentReportHeaderId++;
	}
	
	public void add (long id, ReportHeader v) {
		hm.put(id, v);
	}
	
	public void remove (long id) {
		hm.remove(id);
	}	

	public ReportHeader getReportHeader (long id) {
		return (ReportHeader)hm.get(id);
	}	
	
	public ReportHeader retrieveByPK (long id) {
		return (ReportHeader)hm.get(id);
	}
	
	public List<ReportHeader> getReportHeaderList(int report_id) {
		List <ReportHeader> alist = new ArrayList<ReportHeader>();
		   
		Iterator it = null;		   
		it = hm.entrySet().iterator();

		while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
		        ReportHeader t = (ReportHeader)pairs.getValue();
		        if (t.getReportId() == report_id) {
		        	alist.add(t);
		        }
		}
	    return alist;		
	}
	
	/*
	// get available test by data source type
	public List getListByTestId(int assessment_test_id) {
	   List <ReportHeader> alist = new ArrayList<ReportHeader>();
		   
	   Iterator it = null;		   
	   it = hm.entrySet().iterator();

	   while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        ReportHeader t = (ReportHeader)pairs.getValue();
	        if (t.getAssessmentId() == assessment_test_id) {
	        	alist.add(t);
	        }
	   }
	   return alist;
	}
	*/
}