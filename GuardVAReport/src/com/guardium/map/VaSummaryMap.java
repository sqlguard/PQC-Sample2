/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import com.guardium.assessment.i18n.Say;
import com.guardium.data.VaSummary;
//import com.guardium.data.VaSummaryType;

//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
//import com.guardium.map.VaSummaryMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
import com.guardium.data.VaSummary;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class VaSummaryMap {
	// constructor

	private static VaSummaryMap VaSummaryMapObject;

	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private VaSummaryMap() {
		// initMap();
	}

	public static synchronized VaSummaryMap getVaSummaryMapObject() {
		if (VaSummaryMapObject == null) {
			VaSummaryMapObject = new VaSummaryMap();
		}
		return VaSummaryMapObject;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static ConcurrentHashMap hm = new ConcurrentHashMap();
	private static long currentVaSummaryId = 20000;

	public int getMapSize() {
		return hm.size();
	}

	public void add(VaSummary v) {
		v.setVaSummaryId(currentVaSummaryId);
		hm.put(currentVaSummaryId, v);
		currentVaSummaryId++;
	}

	public void add(long id, VaSummary v) {
		hm.put(id, v);
	}

	public void remove(long id) {
		hm.remove(id);
	}

	public VaSummary getVaSummary(long id) {
		VaSummary s = (VaSummary) hm.get(id);
		return s;
	}

	// get available test by data source type
	public List getListByTestId(int test_id) {
		List<VaSummary> alist = new ArrayList<VaSummary>();

		Iterator it = null;
		it = hm.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// System.out.println(pairs.getKey() + " = " + pairs.getValue());
			VaSummary t = (VaSummary) pairs.getValue();
			if (t.getTestId() == test_id) {
				alist.add(t);
			}
		}
		return alist;
	}


	public VaSummary getSummary(String dataSourceHash, int testId) {
		
		Iterator it = null;
		it = hm.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// System.out.println(pairs.getKey() + " = " + pairs.getValue());
			VaSummary t = (VaSummary) pairs.getValue();
			if ((t.getTestId() == testId) && (t.getDataSourceHash().equals(dataSourceHash))) {
				return t;
			}
		}	
		
		return null;
	}
}

