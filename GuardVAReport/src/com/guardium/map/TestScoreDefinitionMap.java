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

import com.guardium.assessment.tests.TestScoreDefinition;

public class TestScoreDefinitionMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		TestScoreDefinitionMap vmap = new TestScoreDefinitionMap ();
		// create Datasource, put in Datasource list
		
		List <TestScoreDefinition> mlist = new ArrayList<TestScoreDefinition>();
		mlist = vmap.getList();
		
		for (TestScoreDefinition s : mlist) {
	        System.out.println("score id: " + String.valueOf(s.getTestScore()));
	        System.out.println("score description: " + s.getDescription());
	        System.out.println("score timestamp: " + s.getTimestamp());
	    }
	}

	public TestScoreDefinitionMap () {
		if (!initFlag) {
			initMap();
			initFlag = true;
		}
	}
	
	private static boolean initFlag = false;
	
	private static List <TestScoreDefinition> dtlist = new ArrayList<TestScoreDefinition>();
	
	public List<TestScoreDefinition> getList() {
		return dtlist;
	}

	public void setList(List<TestScoreDefinition> tlist) {
		this.dtlist = tlist;
	}

	public static void initMap () {
		      
		// Put elements to the list
		Date date = new Date();

		/*
		------------+-------------------------------------------------------+---------------------+
		|          0 | Fail                                                  | 2014-06-10 03:08:45 |
		|          1 | Pass                                                  | 2014-06-10 03:08:45 |
		|         -1 | Error                                                 | 2014-06-10 03:08:45 |
		|         -2 | No CAS Data                                           | 2014-06-10 03:08:45 |
		|         -3 | No Report Data                                        | 2014-06-10 03:08:45 |
		|         -4 | Unsupported Database                                  | 2014-06-10 03:08:45 |
		|         -5 | Unsupported Operating System                          | 2014-06-10 03:08:45 |
		|         -6 | Special Error                                         | 2014-06-10 03:08:45 |
		|         -7 | CAS Data Collection Error                             | 2014-06-10 03:08:45 |
		|         -8 | Obsolete Parameter                                    | 2014-06-10 03:08:45 |
		|         -9 | Deprecated Parameter                                  | 2014-06-10 03:08:45 |
		|        -10 | Not/Applicable for the DB version                     | 2014-06-10 03:08:45 |
		|        -11 | No User or Group data                                 | 2014-06-10 03:08:45 |
		|        -12 | Modules listed for PTF not present in DB2 MEPL        | 2014-06-10 03:08:45 |
		|        -13 | Pre Test Check Failed. Test not executed              | 2014-06-10 03:08:45 |
		|        -14 | CalculateScore routine not defined. Test not executed | 2014-06-10 03:08:45 |
        */
		
		TestScoreDefinition t = new TestScoreDefinition(0, "Fail", date);
		dtlist.add(t);
		t = new TestScoreDefinition(1, "Pass", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-1, "Error", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-2, "No CAS Data", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-3, "No Report Data", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-4, "Unsupported Database", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-5, "Unsupported Operating System", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-6, "Special Error", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-7, "CAS Data Collection Error", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-8, "Obsolete Parameter", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-9, "Deprecated Parameter", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-10, "Not/Applicable for the DB version", date);
		dtlist.add(t);	
		t = new TestScoreDefinition(-11, "No User or Group data", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-12, "Modules listed for PTF not present in DB2 MEPL", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-13, "Pre Test Check Failed. Test not executed", date);
		dtlist.add(t);
		t = new TestScoreDefinition(-14, "CalculateScore routine not defined. Test not executed", date);
		dtlist.add(t);
	}
	
	public TestScoreDefinition getTestScoreDefinition (int id) {
		for (TestScoreDefinition s : dtlist) {
			if (s.getTestScore() == id) {
				return s;
			}
	    }
		return null;
	}
}
