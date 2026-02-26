/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import com.guardium.assessment.i18n.Say;
import com.guardium.data.AssessmentLog;
import com.guardium.data.AssessmentLogType;

//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
import com.guardium.map.AssessmentTestMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class AssessmentTestMap {
    
	private static AssessmentTestMap AssessmentTestMapObject;
	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private AssessmentTestMap () {
		//initMap();
	}
	
	public static synchronized AssessmentTestMap getAssessmentTestMapObject() {
		if (AssessmentTestMapObject == null) {
			AssessmentTestMapObject = new AssessmentTestMap();
		}
		return AssessmentTestMapObject;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static int currentAssessmentTestId = 20000;
	
	private static ConcurrentHashMap hm = new ConcurrentHashMap();

	/*
	private static List <AssessmentLog> dtlist = new ArrayList<AssessmentLog>();
	
	public List<AssessmentLog> getList() {
		return dtlist;
	}

	public void setList(List<AssessmentLog> tlist) {
		this.dtlist = tlist;
	}
	*/
	public static void add (AssessmentTest v) {
		v.setAssessmentTestId(currentAssessmentTestId);
		hm.put(currentAssessmentTestId, v);
		currentAssessmentTestId++;
		return;
	}	
	
	public static void remove (long id) {
		hm.remove(id);
		return;
	}
	
	public AssessmentTest getAssessmentTest (long id) {
		return (AssessmentTest)hm.get(id);
	}
	
}

