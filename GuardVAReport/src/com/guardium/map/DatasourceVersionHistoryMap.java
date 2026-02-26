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
import com.guardium.data.DatasourceVersionHistory;
//import com.guardium.data.DatasourceVersionHistoryType;

//import com.guardium.datamodel.adminconsole.AdminconsoleParameterPeer;
//import com.guardium.map.DatasourceVersionHistoryMap;
import com.guardium.data.AssessmentResultHeader;
import com.guardium.data.AssessmentTest;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceVersionHistory;
//import com.guardium.datamodel.logger.GdmException;
import com.guardium.utils.AdHocLogger;
import com.guardium.utils.Informer;
import com.guardium.utils.Stringer;

public class DatasourceVersionHistoryMap {
	
	/** Constant for Unknown version or patch levels */
	public static final String VERSION_UNKNOWN = Say.UNKNOWN;

	/**
	 * @param value
	 * @return Whether the version or patch is unknown
	 */
	public static boolean isUnknown(String value) {
		return VERSION_UNKNOWN.equals(value);
	}
	
	
	// constructor

	private static DatasourceVersionHistoryMap DatasourceVersionHistoryMapObject;

	/** A private Constructor prevents any other class from instantiating. */

	// constructor
	private DatasourceVersionHistoryMap() {
		// initMap();
	}

	public static synchronized DatasourceVersionHistoryMap getDatasourceVersionHistoryMapObject() {
		if (DatasourceVersionHistoryMapObject == null) {
			DatasourceVersionHistoryMapObject = new DatasourceVersionHistoryMap();
		}
		return DatasourceVersionHistoryMapObject;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static ConcurrentHashMap hm = new ConcurrentHashMap();
	//private String currentDatasourceVersionHistoryId;

	public int getMapSize() {
		return hm.size();
	}

	public void add(DatasourceVersionHistory v) {
		String currentDatasourceVersionHistoryId = "";
		// create an id - need to unique, 40 chars
		
		v.setDatasourceVersionHistoryId(currentDatasourceVersionHistoryId);
		hm.put(currentDatasourceVersionHistoryId, v);
	}

	public void add(String id, DatasourceVersionHistory v) {
		hm.put(id, v);
	}

	public void remove(String id) {
		hm.remove(id);
	}

	public DatasourceVersionHistory getDatasourceVersionHistory(String id) {
		DatasourceVersionHistory s = (DatasourceVersionHistory) hm.get(id);
		return s;
	}

	// get datasource version history by datasource id
	public List getListByDatasourceId(int did) {
		List<DatasourceVersionHistory> alist = new ArrayList<DatasourceVersionHistory>();

		Iterator it = null;
		it = hm.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			// System.out.println(pairs.getKey() + " = " + pairs.getValue());
			DatasourceVersionHistory t = (DatasourceVersionHistory) pairs.getValue();
			if (t.getDatasourceId() == did) {
				alist.add(t);
			}
		}
		return alist;
	}
}

