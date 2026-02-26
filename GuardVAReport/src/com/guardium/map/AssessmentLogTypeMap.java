/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.map;

//import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.guardium.data.AssessmentLogType;

public class AssessmentLogTypeMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		AssessmentLogTypeMap vmap = new AssessmentLogTypeMap ();
		// create Datasource, put in Datasource list
		
		List <AssessmentLogType> mlist = new ArrayList<AssessmentLogType>();
		mlist = vmap.getList();
		
		for (AssessmentLogType s : mlist) {
			s.dump();
	    }
		
		System.out.println("total list size is " + mlist.size());
	}

	public AssessmentLogTypeMap () {
		if (!initFlag) {
			initMap();
			initFlag = true;
		}
	}
	
	private static boolean initFlag = false;		
	
	private static List <AssessmentLogType> dtlist = new ArrayList<AssessmentLogType>();
	
	public List<AssessmentLogType> getList() {
		return dtlist;
	}

	public void setList(List<AssessmentLogType> tlist) {
		this.dtlist = tlist;
	}

	public static void initMap () {
		      
		// Put elements to the list
		Date date = new Date();
		
		/*
		mysql> select * from ASSESSMENT_LOG_TYPE;
		+------------------------+-----------------------+----------+---------------------+
		| ASSESSMENT_LOG_TYPE_ID | NAME                  | SEVERITY | TIMESTAMP           |
		+------------------------+-----------------------+----------+---------------------+
		|                      1 | Debug                 |        1 | 2014-05-30 10:45:09 |
		|                      2 | Info                  |        2 | 2014-05-30 10:45:09 |
		|                      3 | Warning               |        3 | 2014-05-30 10:45:09 |
		|                      4 | Error                 |        4 | 2014-05-30 10:45:09 |
		|                      5 | Fatal                 |        5 | 2014-05-30 10:45:09 |
		|                    201 | Assessment Start      |        3 | 2014-05-30 10:45:09 |
		|                    202 | Datasource Statistics |        2 | 2014-05-30 10:45:09 |
		|                    203 | Assessment Complete   |        3 | 2014-05-30 10:45:09 |
		|                    205 | Assessment Halted     |        5 | 2014-05-30 10:45:09 |
		|                    206 | Timeout               |        5 | 2014-05-30 10:45:09 |
		+------------------------+-----------------------+----------+---------------------+
		10 rows in set (0.02 sec)

		*/
				
		AssessmentLogType t = new AssessmentLogType(1, "Debug", 1, date);
		dtlist.add(t);
		t = new AssessmentLogType(2, "Info", 2, date);
		dtlist.add(t);
		t = new AssessmentLogType(3, "Warning", 3, date);
		dtlist.add(t);
		t = new AssessmentLogType(4, "Error", 4, date);
		dtlist.add(t);
		t = new AssessmentLogType(5, "Fatal", 5, date);
		dtlist.add(t);
		t = new AssessmentLogType(201, "Assessment Start", 3, date);
		dtlist.add(t);
		t = new AssessmentLogType(202, "Datasource Statistics", 2, date);
		dtlist.add(t);
		t = new AssessmentLogType(203, "Assessment Complete", 3, date);
		dtlist.add(t);
		t = new AssessmentLogType(205, "Assessment Halted", 5, date);
		dtlist.add(t);
		t = new AssessmentLogType(206, "Timeout", 5, date);
		dtlist.add(t);
	}
	
	public AssessmentLogType getAssessmentLogType (long id) {
		for (AssessmentLogType s : dtlist) {
			if (s.getAssessmentLogTypeId() == id) {
				return s;
			}
	    }
		return null;
	}

}
