/*
 * IBM Confidential
 * OCO Source Materials
 * © Copyright IBM Corp. 2002, 2023
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */
package com.guardium.runtest;

import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTextArea;

//import org.apache.log4j.Logger;

import com.guardium.data.AssessmentTest;
import com.guardium.data.AvailableTest;
import com.guardium.data.DataSourceConnectException;
import com.guardium.data.Datasource;
import com.guardium.data.DatasourceType;
import com.guardium.data.DatasourceVersionHistory;
import com.guardium.data.DbDriver;
import com.guardium.data.SecurityAssessment;
import com.guardium.data.SqlbasedAssessmentDefinition;
//import com.guardium.map.AssessmentLogMap;
import com.guardium.map.AssessmentResultHeaderMap;
import com.guardium.map.AvailableTestMap;
//import com.guardium.map.CveFixMap;
//import com.guardium.map.CveReferenceMap;
import com.guardium.map.CveFixMap;
import com.guardium.map.CveReferenceMap;
import com.guardium.map.DatasourceMap;
import com.guardium.map.DbDriverMap;
import com.guardium.map.DatasourceTypeMap;
import com.guardium.map.RecommendationTextMap;
import com.guardium.map.SqlbasedAssessmentDefinitionMap;
import com.guardium.map.SecurityAssessmentMap;
import com.guardium.map.TestResultMap;
//import com.guardium.runtest.AbstractJdbcTest;
//import com.guardium.utils.AdHocLogger;
//import com.guardium.utils.AdHocLogger;
import com.guardium.utils.WriteResult;
//import com.itextpdf.text.pdf.PdfDocument;
//import com.itextpdf.text.pdf.PdfPage;
import com.guardium.gui.TestProgress;
import com.guardium.gui.VATest;

/*
 * 	// loop through datasource list

 // make connection

 // select available test by datasource type

 // Run the test, take a look querybased test 

 // get result for each test

 // write a summary result, pdf file
 * 
 */

public class VATestRun {

	// Global variables
	static int AssessmentID = 20000;
	static int AssessmentTestID = 20000;

	//int run_type_one = 0;
	//int run_type_list = -1;
	
	//private static final transient Logger LOG = Logger.getLogger(AbstractJdbcTest.class);

	
	RecommendationTextMap RecommendationTextPeer = new RecommendationTextMap();
	
	/*
	public int writeMethod = 1;
	
	private  JTextArea taskOutput;
	
	public int getWriteMethod () {
		return writeMethod;
	}
	
	public void setWriteMethod (int met) {
		writeMethod = met;
		return;
	}
	*/
	
	// create security assessment for each datasource
	// if connect OK, and have available test defined.
	

	// create a assessment test
	//static List<AssessmentTest> AssessmentTestList = new ArrayList<AssessmentTest>();

	/*
	public void writeResult (String str) {
		// write to textfield
		//String tmpstr = str + "\n";
		//taskOutput.append(String.format(tmpstr));
		

		if (writeMethod == 1) {
			// system output
			System.out.println(str);
		}
		else if (writeMethod == 2) {
			// write to textfield
			String tmpstr = str + "\n";
			taskOutput.append(String.format(tmpstr));	
		}
		else if (writeMethod == 3) {
			// pdf file
			
		}
		return;
	}
	*/
	
	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		run();
	}
	*/
	private TestProgress testProg;
	

	public void run (JTextArea output, TestProgress tk) {
		System.out.println("run text area start");
		testProg = tk;
		
	    //setWriteMethod(2);
		//taskOutput = output;
		
		WriteResult.setWriteMethod(2);
		WriteResult.setJTextArea(output);
		WriteResult.writeOutput("Start to run the VA test");
		
		run(-1);
	}
	
	public void run (JTextArea output) {
		System.out.println("run text area start");
	    //setWriteMethod(2);
		//taskOutput = output;
		
		WriteResult.setWriteMethod(2);
		WriteResult.setJTextArea(output);
		WriteResult.writeOutput("Start to run the VA test");
		
		run(-1);
	}
	
	public void run (int idx) {
		
		// create Datasource, put in Datasource list
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
        
		// create datasources
		//DatasourceMap.initMap();
		
		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		WriteResult.writeOutput("DS list size is " + dsize);

		if (dsize <= 0) {
			WriteResult.writeOutput("No datasource defined. Can not run the VA test");
			return;
		}

		if (idx == -1) {
			for (Datasource ds : datasourceList) {
				run (ds);
			}
			
		}
		else {
			Datasource ds = datasourceList.get(idx);
			run(ds);
		}
		
	}
	
	public void run (Datasource ds) {
		System.out.println("real run start");
		// Global variable
		AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();		
		SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionPeer = SqlbasedAssessmentDefinitionMap.getSqlbasedAssessmentDefinitionMapObject();
		SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
		CveFixMap CveFixPeer = CveFixMap.getCveFixMapObject();
		CveReferenceMap CveReferencePeer = CveReferenceMap.getCveReferenceMapObject();

		TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
		//AssessmentLogMap AssessmentLogPeer = AssessmentLogMap.getAssessmentLogMapObject();
		AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
		/*
		// create Datasource, put in Datasource list
		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();
        
		// create datasources
		//DatasourceMap.initMap();
		
		List<Datasource> datasourceList = new ArrayList<Datasource>();
		datasourceList = DatasourcePeer.getList();

		int dsize = datasourceList.size();
		WriteResult.writeOutput("DS list size is " + dsize);

		if (dsize <= 0) {
			WriteResult.writeOutput("No datasource defined. Can not run the VA test");
			return;
		}
		*/
		
		// init DbDriver map
		//DbDriverMap driverMap = new DbDriverMap();
		DbDriverMap DbDriverPeer = DbDriverMap.getDbDriverMapObject();
		// init DatasourceType map
		DatasourceTypeMap dsTypeMap = new DatasourceTypeMap();

		// init recommendationText map
		//RecommendationTextMap recommTextMap = new RecommendationTextMap();

		List<AvailableTest> QueryAvailableTestList = new ArrayList<AvailableTest>();
		List<AvailableTest> CveAvailableTestList   = new ArrayList<AvailableTest>();
		List<SecurityAssessment> SecurityAssessmentList = new ArrayList<SecurityAssessment>();
		
		//for (Datasource ds : datasourceList) {

			//WriteResult.writeOutput("\n\nDatasource info:");
			System.out.println("\n\nDatasource info:");
			ds.dump();
			
			
			//System.out.println("datasouce id: " + ds.getDatasourceId());
			//System.out.println("datasouce db driver id: " + ds.getDbDriverId());
			// ds.dump();

			// set db driver
			DbDriver sdriver = DbDriverPeer.getDbDriverById(ds.getDbDriverId());
			ds.setDbDriver(sdriver);

			// set datasource type
			DatasourceType dst = dsTypeMap.getDatasourceType(ds
					.getDatasourceTypeId());
			ds.setDatasourceType(dst);
			

			
			// get QUERY TEST for this datasource type
			QueryAvailableTestList = AvailableTestPeer.getTestListByDsType(AvailableTestMap.QUERY_TEST_TYPE, ds.getDatasourceTypeId());
			if (QueryAvailableTestList.size() <= 0) {
				WriteResult.writeOutput("No VA query test defined for this datasource");
				//continue;
			} else {
				// set sql definition
				for (AvailableTest avt : QueryAvailableTestList) {
					avt.setClassName("com.guardium.runtest.QueryBasedTest");
					// set datasource type
					avt.setDatasourceType(dst);
					
					// for testing
					// avt.setApplicableToVersion(11.5);
					
					// System.out.println("test id is " + avt.getTestId());
					SqlbasedAssessmentDefinition sd = SqlbasedAssessmentDefinitionPeer
							.getSqlbasedAssessmentDefinitionById(avt
									.getTestId());
					if (sd != null) {
						avt.setSqlDefinition(sd);
						sd.setAvailableTest(avt);
					}

				}
			}
			

			// get CVE TEST for this datasource type
			CveAvailableTestList = AvailableTestPeer.getTestListByDsType(AvailableTestMap.CVE_TEST_TYPE, ds.getDatasourceTypeId());
			if (CveAvailableTestList.size() <= 0) {
				System.out.println("No VA CVE test defined for this datasource");
				// continue;
			} else {
				// set cve fix
				for (AvailableTest avt : CveAvailableTestList) {
					avt.setClassName("com.guardium.runtest.CVETest");
					// set datasource type
					avt.setDatasourceType(dst);

					// set cve fix record
					avt.setCveFixs(CveFixPeer.getListByTestId(avt.getTestId()));
				}
			}		

			// try to connect
			Connection con;
			try {
				System.out.println("before getConnection...");
				con = ds.getConnection();
				String tmp = con.getCatalog();
				//WriteResult.writeOutput("connection OK get catalog " + tmp);
				System.out.println("connection OK get catalog " + tmp);
				
				// get version
				// List <DatasourceVersionHistory> vhistory = new ArrayList<DatasourceVersionHistory> ();
				DatasourceVersionHistory dhistory;
				dhistory = ds.findVersionHistory(con);
				if (dhistory != null) {
					ds.setVersionLevel(dhistory.getVersionLevel());
					ds.setPatchLevel(dhistory.getPatchLevel());
					//ds.setMajorVersion(majorVersion);
					
				}

			} catch (DataSourceConnectException | SQLException e) {
				// DataSourceConnectException dsce =
				// datasource.expandConnectionException(e);
				// throw dsce;
				WriteResult.writeOutput("connection failed " + e.getMessage());
				//continue;
			}

			// define the test

			// create security assessment for each datasource
			// one for query tests, one for CVE tests
			// if connect OK, and have available test defined.
			String secass_desc = "";
			SecurityAssessment secass = null; 
			List<AssessmentTest> dTestList = new ArrayList<AssessmentTest>();
			Iterator itr = null;
			int testType = 1;  // 1 for quest test, 2 for CVE test
			// query tests
			if ( QueryAvailableTestList.size() > 0) {
				secass_desc = "Test_Query_" + ds.getDbType() + "_" + String.valueOf(AssessmentID);
				secass = new SecurityAssessment(AssessmentID,
					secass_desc, testType, "", "", false, "", "");
				//SecurityAssessmentList.add(secass);
				//secass.save();
			
				// create a assessment test
				dTestList = new ArrayList<AssessmentTest>();

				// create assessment test for each test
				itr = QueryAvailableTestList.iterator();

				while (itr.hasNext()) {
					AvailableTest tv = (AvailableTest) itr.next();
					// System.out.println("id " + tv.getTestId() +
					// " datasource type id " + tv.getDatasource_type_id());

					AssessmentTest asstest = new AssessmentTest(AssessmentTestID,
						AssessmentID, tv.getTestId(), false, 0, "MAJOR", -1,
						false);
					//AssessmentTest asstest = new AssessmentTest();
				
				
					dTestList.add(asstest);
					AssessmentTestID++;
				}
			
				// set datasource
				// set available test
				secass.setAssessmentTests(dTestList);
				secass.addDatasources(ds);
				secass.save();
				AssessmentID++;
			}

			// CVE tests
			
			if ( CveAvailableTestList.size() > 0) {
				testType = 2;
				secass_desc = "Test_CVE_"  + ds.getDbType() + "_" + String.valueOf(AssessmentID);
				secass = new SecurityAssessment(AssessmentID,
					secass_desc, testType, "", "", false, "", "");
				//SecurityAssessmentList.add(secass);
				//secass.save();
			
				// create a assessment test
				dTestList = new ArrayList<AssessmentTest>();

				// create assessment test for each test
				itr = CveAvailableTestList.iterator();

				while (itr.hasNext()) {
					AvailableTest tv = (AvailableTest) itr.next();
					// System.out.println("id " + tv.getTestId() +
					// " datasource type id " + tv.getDatasource_type_id());

					AssessmentTest asstest = new AssessmentTest(AssessmentTestID,
						AssessmentID, tv.getTestId(), false, 0, "MAJOR", -1,
						false);
					//AssessmentTest asstest = new AssessmentTest();
					dTestList.add(asstest);
					AssessmentTestID++;
				}
			
				// set datasource
				// set available test
				secass.setAssessmentTests(dTestList);
				secass.addDatasources(ds);
				secass.save();
				AssessmentID++;
			}


			// run the assessment test
			//real_run();
			
		//}
        
		/*
		WriteResult.writeOutput("Run the assessment tests;");
		// Run the tests
		SecurityAssessmentList = SecurityAssessmentPeer.getAllList();
		
		int sstSize = SecurityAssessmentList.size();
		WriteResult.writeOutput("Run the assessment tests size is " + sstSize);
		int incr = 100/sstSize;
		
		//OuterClass.InnerClass innerObject = outerObject.new InnerClass();
		                                                                                                                                                                                                                                                        
		
		for (SecurityAssessment sst : SecurityAssessmentList) {  
			
			WriteResult.writeOutput("\n\n");
			sst.dump();

			TestResultPeer.cleanMap();
			
			// run the tests
			AssessmentRunner assrun = new AssessmentRunner();
			assrun.setSecurityAssessment(sst);
			
			//
			try {
				assrun.run();
			} catch (Exception e) {
				AdHocLogger.logException(e);
				LOG.error(e);
				// setStringDetail( Informer.causality(e) );
			}
			
			// test result
			WriteResult.writeOutput("TestResult map size is "
					+ TestResultPeer.getMapSize());
			if (TestResultPeer.getMapSize() > 0) {
				
				//TestResultPeer.dumpMap();
				
				WriteResult.writeOutput("\nTestResult summary is ");
				TestResultPeer.dumpSummary ();
			}

		}

		
		WriteResult.writeOutput("SecurityAssessment list size is "
				+ SecurityAssessmentList.size());

		//AssessmentLogPeer.dumpMap();
		//taskOutput.append(String.format("AssessmentLog map size is "
		//		+ AssessmentLogPeergetMapSize()));		
		
		// result header
		WriteResult.writeOutput("Assessment Result Header map size is "
				+ AssessmentResultHeaderPeer.getMapSize());
		//AssessmentResultHeaderPeer.dumpMap();
		
		
		
		WriteResult.writeOutput("DS list size is " + datasourceList.size());
		*/

	}
	
    public void real_run () {
 		SecurityAssessmentMap SecurityAssessmentPeer = SecurityAssessmentMap.getSecurityAssessmentMapObject();
 		TestResultMap TestResultPeer = TestResultMap.getTestResultMapObject();
 		AssessmentResultHeaderMap AssessmentResultHeaderPeer = AssessmentResultHeaderMap.getAssessmentResultHeaderMapObject();
 		
 		// create Datasource, put in Datasource list
 		DatasourceMap DatasourcePeer = DatasourceMap.getDatasourceMapObject();

         List<SecurityAssessment> SecurityAssessmentList = new ArrayList<SecurityAssessment>();
 		
         //progress += 10;
         //setProgress(progress);
         
 		WriteResult.writeOutput("Run the assessment tests;");
 		// Run the tests
 		SecurityAssessmentList = SecurityAssessmentPeer.getAllList();
 		
 		int sstSize = SecurityAssessmentList.size();
 		WriteResult.writeOutput("Run the assessment tests size is " + sstSize);
 		int incr = 90/sstSize;
 		String outputDir = VATest.VAOutputDir + "result";
 		String outputFile = "";
 		
 		if (VATest.VACurrentOS.equals("window")) {
 			outputDir = outputDir + "\\";
 		}
 		else {
 			outputDir = outputDir + "/";
 		}
 		
 		//OuterClass.InnerClass innerObject = outerObject.new InnerClass()                                                                                                                                                                                               
 		// GUI - list SecurityAssessment name
 		for (SecurityAssessment sst : SecurityAssessmentList) {  
 			
 			WriteResult.writeOutput("\n\n");
 			sst.dump();
 			
 			// create output result file for each sst
 			outputFile = outputDir + sst.getAssessmentDesc();
             Writer outputwrite = WriteResult.createLogFile(outputFile);
             
 			TestResultPeer.cleanMap();
 			
 			// run the tests
 			AssessmentRunner assrun = new AssessmentRunner();
 			assrun.setSecurityAssessment(sst);
 			
 			//
 			try {
 				assrun.run();
 			} catch (Exception e) {
 				//AdHocLogger.logException(e);
 				//LOG.error(e);
 				// setStringDetail( Informer.causality(e) );
 			}
 			
 			// test result
 			WriteResult.writeOutput("TestResult map size is "
 					+ TestResultPeer.getMapSize());
 			WriteResult.writeOutput("");
 			if (TestResultPeer.getMapSize() > 0) {
 				
 				// GUI - use this list to display the result list
 				List <String> alist = new ArrayList<String>();
 				alist = TestResultPeer.getTestDescList();
 				
 				// GUI - generate pdf file for each test result
 				TestResultPeer.dumpMap(outputwrite);
 				
 				// GUI - when click the item in the list, it will display pdf result
 				
 				
 				// GUI - generate pdf file for test summary
 				WriteResult.writeOutput("\n\nTestResult summary for QueryBased Tests: ");
 				// 4 is for querybased test
 				int t[] = TestResultPeer.getSummary ();
 				sst.setTestSummary(t);
 				
 				WriteResult.writeOutput("");
     			sst.dumpResult(outputwrite);
     			

     			WriteResult.writeOutput("\n\nTestResult summary for CVE Tests: ");
     			// 6 is for cve test
     			t = TestResultPeer.getSummary (6);
 				sst.setTestSummary(t);
 				
 				WriteResult.writeOutput("");
     			sst.dumpResult(outputwrite);

     			
     			WriteResult.closeLogFile(outputwrite);
 			}
 			
 			//progress += incr;
            // setProgress(Math.min(progress, 100));    			
 		}

 		//setProgress(100);
 		
 		WriteResult.writeOutput("SecurityAssessment list size is "
 				+ SecurityAssessmentList.size());

 		//AssessmentLogPeer.dumpMap();
 		//taskOutput.append(String.format("AssessmentLog map size is "
 		//		+ AssessmentLogPeergetMapSize()));		
 		
 		// result header
 		WriteResult.writeOutput("Assessment Result Header map size is "
 				+ AssessmentResultHeaderPeer.getMapSize());
 		//AssessmentResultHeaderPeer.dumpMap();
 		
 		
 		
 		//WriteResult.writeOutput("DS list size is " + datasourceList.size());

 		/*
 		// dump result after run
 		for (SecurityAssessment sst : SecurityAssessmentList) {  
 			
 			WriteResult.writeOutput("\n\n");
 			//sst.dumpResult();
 			int [] t = sst.getTestSummary();
 			final BarChartResult demo = new BarChartResult("Oracle x64 Query Based Test Result");
 		}
         */
 		
 		
         //CreatePdfFile cf = new CreatePdfFile();
         //cf.create();
         
         
         
         /*
         System.out.println("doInBackground before while");
         while (progress < 100) {
             //Sleep for up to one second.
             try {
                 Thread.sleep(random.nextInt(1000));
             } catch (InterruptedException ignore) {}
             //Make random progress.
             progress += random.nextInt(10);
             setProgress(Math.min(progress, 100));
         }
         */

         return;
     }    
     

	private static void initData() {
		// get user defined Datasource list
		//AvailableTestMap AvailableTestPeer = AvailableTestMap.getAvailableTestMapObject();
		
		//SqlbasedAssessmentDefinitionMap SqlbasedAssessmentDefinitionPeer = SqlbasedAssessmentDefinitionMap.getSqlbasedAssessmentDefinitionMapObject();

		return;
	}

	private void loadData() {

		// DbDriver

		// DatasourceType

		// sqlbased_assessement_definition

		// recommendation text

		// TestScoreDefinition

		//

	}

	protected SecurityAssessment secassment;

	protected SecurityAssessment getSecurityAssessment() {
		return secassment;
	}

	protected void setSecurityAssessment(SecurityAssessment v) {
		secassment = v;
		return;
	}

	/*
	private void writePdf () {
		
		String parsedText = "abc.def.ghi.xyz";
		String[] splitText = parsedText.split("\\."); 
	    List <String> newList = new ArrayList<String>(Arrays.asList(splitText));


	    PdfDocument doc = null;
	    PdfPage page = null;
	    try{
	        doc = new PdfDocument();
	        page = new PdfPage();

	        doc.addPage(page);
	        PDFont font = PDType1Font.HELVETICA;
	        PDPageContentStream content = new PDPageContentStream(doc,page);

	        content.beginText();
	        content.setFont(font, 12);
	        content.moveTextPositionByAmount(100, 700);
	        String text = "";
	        for(int i=0; i<newList.size();i++)
	        {
	            if(sentence.isEmpty()) continue;
	            content.drawString(newList.get(i) + "\n");
	            content.moveTextPositionByAmount(0, LINE_HEIGHT); 
	        }

	        content.endText();
	        content.close();
	        doc.save("nameoffile.pdf");
	        doc.close();
	        System.out.print("Pages" + pdDoc.getNumberOfPages());


	}
	*/
}
