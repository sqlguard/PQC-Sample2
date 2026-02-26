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

import com.guardium.data.AvailableTest;
import com.guardium.data.RecommendationText;
import com.guardium.assessment.tests.TestScore;

public class RecommendationTextMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// create map
		RecommendationTextMap vmap = new RecommendationTextMap ();
		
		List <RecommendationText> mlist = new ArrayList<RecommendationText>();
		mlist = vmap.getList();
		
		for (RecommendationText s : mlist) {
			s.dump();
	    }
		System.out.println("recommendation text list size is " + mlist.size());
	}

	public void RecommendationTextMap () {
		if (!initFlag) {
			initMap();
			initFlag = true;
		}
	}
	
	private static boolean initFlag = false;
	
	private static List <RecommendationText> dtlist = new ArrayList<RecommendationText>();
	
	public List<RecommendationText> getList() {
		return dtlist;
	}

	public void setList(List<RecommendationText> tlist) {
		this.dtlist = tlist;
	}

	public static void initMap () {
		      
		// Put elements to the list
		RecommendationText t = new RecommendationText(1, -2, -1, -1, "Please correct the error condition and run the Assessment again.");
		dtlist.add(t);
		t = new RecommendationText(2, -2, -2, -2, "No CAS results were available for this test. Please make sure that all required Monitored Items are enabled and the CAS process has had time to collect data prior to running the Assessment.");
		dtlist.add(t);
		t = new RecommendationText(3, -2, -3, -3,"Report data was not available to evaluate this test. Please make sure that the reports have run prior to running the Assessment.");
		dtlist.add(t);
		t = new RecommendationText(4, -2, -4, -4, "This test is not valid for this version of the database. No action is required.");
		dtlist.add(t);
		t = new RecommendationText(5, -2, -5, -5, "This test is not valid for this version of the Operating System. No action is required.");
		dtlist.add(t);
		t = new RecommendationText(6, -2, -6, -6, "A special error occurred. Please check the result text for guidelines.");
		dtlist.add(t);
		t = new RecommendationText(7, -2, -7, -7, "Possibly CAS needs permission to run data collection program, or cannot locate the program.");
		dtlist.add(t);
		t = new RecommendationText(8, -2, -8, -8, "Parameter is obsolete for this DB version ");
		dtlist.add(t);
		t = new RecommendationText(9, -2, -9, -9, "Parameter is deprecated for this DB version");
		dtlist.add(t);
		t = new RecommendationText(10, -2, 10, -10, "This test is not applicable for this Database Version " );
		dtlist.add(t);
		t = new RecommendationText(11, -2, -11, -11, "Could not access either CKADBVA.CKA_OS_GROUP, CKADBVA.CKA_OS_USER or SYSIBM.SYSROLES.");
		dtlist.add(t);		
		t = new RecommendationText(13, -2, -13, -13, "Pre Test Check Failed. Test not executed");
		dtlist.add(t);
		t = new RecommendationText(14, -2, -14, -14, "CalculateScore routine not defined. Test not executed. Please make sure that this routine has been implemented.");
		dtlist.add(t);
		t = new RecommendationText(100, -1, -1,-1, "None of the tests in this assessment had the necessary data available to run to completion.  Make sure the testing environment is configured correctly, and then run the assessment again.");
		dtlist.add(t);
		t = new RecommendationText(101, -1, 0, 0.2, "Based on the tests performed under this assessment, data access of the defined database environments requires significant improvement across a number of areas. Refer to the recommendations of the individual tests to learn how you can address problems within your environment, focusing on severe issues first. Continue running repeats of this assessment with every issue you address to track improvement.");
		dtlist.add(t);
		t = new RecommendationText(102, -1, 0.2, 0.5, "Based on the tests performed under this assessment, data access of the defined database environments requires improvement. Refer to the recommendations of the individual tests to learn how you can address problems within your environment and what you should focus upon first. Once you have begun addressing these problems you should also consider scheduling this assessment as an audit task to continuously assess these environments and track improvement.");
		dtlist.add(t);
		t = new RecommendationText(103, -1, 0.5, 0.8, "Based on the tests performed under this assessment, data access of the defined database environments is nearing best practices. Refer to the recommendations of the individual tests to learn how you can achieve best-practice status. You should also consider scheduling this assessment as an audit task to continuously assess these environments and track improvement.");
		dtlist.add(t);		
		t = new RecommendationText(104, -1, 0.8, 1,"Based on the tests performed under this assessment, data access of the defined database environments conform to best practices. You have a controlled environment in terms of the tests performed. You should consider scheduling this assessment as an audit task to continuously assess these environments.");
		dtlist.add(t);
	
	}
	
	public RecommendationText getRecommendationTextByID (int id) {
		for (RecommendationText s : dtlist) {
			if (s.getRecommendationId() == id) {
				return s;
			}
	    }
		return null;
	}

	public RecommendationText getRecommendationTextByTestID (int id) {
		for (RecommendationText s : dtlist) {
			if (s.getTestId() == id) {
				return s;
			}
	    }
		return null;
	}
	
	public String findErrorRecommendation(TestScore testscore) {
		String str = "";
		return str;
	}
	
	public String findRecommendation(int availableTestID, TestScore testScore) {
		String str = "";
		return str;
	}
	
	public String findRecommendation(AvailableTest av, TestScore testScore) {
		String str = "";
		return str;		
	}
	
	/*
	 * 

mysql> select * from RECOMMENDATION_TEXT where TEST_ID  < 0;
----------------------------------------+
| RECOMMENDATION_ID | TEST_ID | FROM_SCORE | TO_SCORE | TEXT                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
+-------------------+---------+------------+----------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                 1 |      -2 |         -1 |       -1 | Please correct the error condition and run the Assessment again.                                                                                                                                                                                                                                                                                                                                                                                                        |
|                 2 |      -2 |         -2 |       -2 | No CAS results were available for this test. Please make sure that all required Monitored Items are enabled and the CAS process has had time to collect data prior to running the Assessment.                                                                                                                                                                                                                                                                           |
|                 3 |      -2 |         -3 |       -3 | Report data was not available to evaluate this test. Please make sure that the reports have run prior to running the Assessment.                                                                                                                                                                                                                                                                                                                                        |
|                 4 |      -2 |         -4 |       -4 | This test is not valid for this version of the database. No action is required.                                                                                                                                                                                                                                                                                                                                                                                         |
|                 5 |      -2 |         -5 |       -5 | This test is not valid for this version of the Operating System. No action is required.                                                                                                                                                                                                                                                                                                                                                                                 |
|                 6 |      -2 |         -6 |       -6 | A special error occurred. Please check the result text for guidelines.                                                                                                                                                                                                                                                                                                                                                                                                  |
|                 7 |      -2 |         -7 |       -7 | Possibly CAS needs permission to run data collection program, or cannot locate the program.                                                                                                                                                                                                                                                                                                                                                                             |
|                 8 |      -2 |         -8 |       -8 | Parameter is obsolete for this DB version                                                                                                                                                                                                                                                                                                                                                                                                                               |
|                 9 |      -2 |         -9 |       -9 | Parameter is deprecated for this DB version                                                                                                                                                                                                                                                                                                                                                                                                                             |
|                10 |      -2 |        -10 |      -10 | This test is not applicable for this Database Version                                                                                                                                                                                                                                                                                                                                                                                                                   |
|                11 |      -2 |        -11 |      -11 | Could not access either CKADBVA.CKA_OS_GROUP, CKADBVA.CKA_OS_USER or SYSIBM.SYSROLES.                                                                                                                                                                                                                                                                                                                                                                                   |
|                13 |      -2 |        -13 |      -13 | Pre Test Check Failed. Test not executed                                                                                                                                                                                                                                                                                                                                                                                                                                |
|                14 |      -2 |        -14 |      -14 | CalculateScore routine not defined. Test not executed. Please make sure that this routine has been implemented.                                                                                                                                                                                                                                                                                                                                                         |
|               100 |      -1 |         -1 |       -1 | None of the tests in this assessment had the necessary data available to run to completion.  Make sure the testing environment is configured correctly, and then run the assessment again.                                                                                                                                                                                                                                                                              |
|               101 |      -1 |          0 |      0.2 | Based on the tests performed under this assessment, data access of the defined database environments requires significant improvement across a number of areas. Refer to the recommendations of the individual tests to learn how you can address problems within your environment, focusing on severe issues first. Continue running repeats of this assessment with every issue you address to track improvement.                                                     |
|               102 |      -1 |        0.2 |      0.5 | Based on the tests performed under this assessment, data access of the defined database environments requires improvement. Refer to the recommendations of the individual tests to learn how you can address problems within your environment and what you should focus upon first. Once you have begun addressing these problems you should also consider scheduling this assessment as an audit task to continuously assess these environments and track improvement. |
|               103 |      -1 |        0.5 |      0.8 | Based on the tests performed under this assessment, data access of the defined database environments is nearing best practices. Refer to the recommendations of the individual tests to learn how you can achieve best-practice status. You should also consider scheduling this assessment as an audit task to continuously assess these environments and track improvement.                                                                                           |
|               104 |      -1 |        0.8 |        1 | Based on the tests performed under this assessment, data access of the defined database environments conform to best practices. You have a controlled environment in terms of the tests performed. You should consider scheduling this assessment as an audit task to continuously assess these environments.                                                                                                                                                           |
+-------------------+---------+------------+----------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
18 rows in set (0.00 sec)

mysql> 

	 */
	
	
}
